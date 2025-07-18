// Copyright 2024 LINE Plus Corporation
//
// LINE Plus Corporation licenses this file to you under the Apache License,
// version 2.0 (the "License"); you may not use this file except in compliance
// with the License. You may obtain a copy of the License at:
//
//   https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
// License for the specific language governing permissions and limitations
// under the License.

package com.linecorp.planetkit.quickstart.groupVideoCall

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.linecorp.planetkit.PlanetKit
import com.linecorp.planetkit.quickstart.groupVideoCall.databinding.SimpleMyViewBinding
import com.linecorp.planetkit.ui.PlanetKitMyView
import com.linecorp.planetkit.video.PlanetKitVideoStatus

class SimpleMyView @JvmOverloads constructor (
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
): FrameLayout(context, attrs, defStyle) {
    private val binding: SimpleMyViewBinding by lazy { SimpleMyViewBinding.inflate(LayoutInflater.from(context), this, true) }
    companion object {
        const val TAG = "SimpleMyView"
    }

    @Synchronized fun setMe() {
        Log.d(TAG, "$this setMe")
        showNoVideoView()
        PlanetKit.getConference()?.let {
            binding.myView.setMe(it, myViewListener)
        }
    }

    private val myViewListener = object : PlanetKitMyView.MyViewListener {
        override fun onInitialized(userData: Any?) {
            Log.d(TAG, "onInitialized")
            val myVideoStatus = binding.myView.myVideoStatus
            Log.d(TAG, "onInitialized videoState $myVideoStatus")
            when(myVideoStatus.videoState) {
                PlanetKitVideoStatus.VideoState.DISABLED -> showNoVideoView()
                PlanetKitVideoStatus.VideoState.ENABLED -> showResumedView()
                else -> {}
            }
        }

        override fun onRenderFirstFrame() {
            Log.d(TAG, "$this onRenderFirstFrame")
            binding.videoResume.isVisible = false
        }

        override fun onVideoStatusUpdated(videoStatus: PlanetKitVideoStatus) {
            Log.d(TAG, "$this onVideoStatus status=$videoStatus")
            when(videoStatus.videoState) {
                PlanetKitVideoStatus.VideoState.DISABLED -> showNoVideoView()
                PlanetKitVideoStatus.VideoState.ENABLED -> showResumedView()
                else -> {}
            }
        }
    }

    private fun showNoVideoView() {
        Log.d(TAG, "$this showNoVideoView")
        binding.videoResume.isVisible = false
        binding.noVideo.isVisible = true
    }

    private fun showResumedView() {
        Log.d(TAG, "$this showResumedView")
        binding.videoResume.isVisible = true
        binding.noVideo.isVisible = false
        binding.myView.resetFirstFrameRendered()
    }
}