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

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.linecorp.planetkit.PlanetKit
import com.linecorp.planetkit.PlanetKitVideoResolution
import com.linecorp.planetkit.quickstart.groupVideoCall.databinding.SimplePeerViewBinding
import com.linecorp.planetkit.session.conference.subgroup.PlanetKitConferencePeer
import com.linecorp.planetkit.ui.PlanetKitPeerView
import com.linecorp.planetkit.video.PlanetKitVideoStatus
import com.linecorp.planetkit.video.PlanetKitVideoStatus.VideoState

@SuppressLint("ViewConstructor")
class SimplePeerView @JvmOverloads constructor (
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
): FrameLayout(context, attrs, defStyle) {
    private val binding: SimplePeerViewBinding by lazy { SimplePeerViewBinding.inflate(LayoutInflater.from(context), this, true) }
    private var conferencePeer: PlanetKitConferencePeer? = null

    companion object {
        private const val TAG = "SimplePeerView"
    }

    @Synchronized fun setUser(newConferencePeer: PlanetKitConferencePeer, userData: Any? = null) {
        Log.d(TAG, "setUser=${newConferencePeer.userId}")
        PlanetKit.getConference()?.let {
            conferencePeer = newConferencePeer
            //binding.peerView.scaleType = ServiceLocator.preference.VIDEO_CONTENT_MODE
            //binding.peerView.fillColor(ServiceLocator.preference.VIDEO_BACKGROUND_COLOR)

            if(!binding.peerView.setPeer(it, newConferencePeer, peerViewListener, userData)) {
                Log.e(TAG, "PeerView.setUser failed for $newConferencePeer ")
            }
        }
    }

    private val peerViewListener = object : PlanetKitPeerView.PeerViewListener {
        override fun onInitialized(peer: PlanetKitConferencePeer, userData: Any?) {
            val result = peer.getVideoStatus(peer.currentVideoSubgroupName)
            Log.d(TAG, "onInitialized with videoResult=${result.videoStatus.videoState}")
            updateVideo(result.videoStatus)
        }

        override fun onRenderFirstFrame(peer: PlanetKitConferencePeer) {
            Log.d(TAG, "onRenderFirstFrame peer=$peer")
            binding.videoResume.isVisible = false
            binding.noVideo.isVisible = false
            binding.videoPause.isVisible = false
        }

        override fun onDisconnected(peer: PlanetKitConferencePeer) {
            Log.d(TAG, "onDisconnected $peer")
            clearUser()
        }

        override fun onVideoUpdated(peer: PlanetKitConferencePeer,
            videoStatus: PlanetKitVideoStatus,
            subgroupName: String?
        ) {
            updateVideo(videoStatus)
        }
    }

    private fun updateVideo(videoStatus: PlanetKitVideoStatus) {
        when (videoStatus.videoState) {
            VideoState.DISABLED -> showNoVideoView()
            VideoState.ENABLED -> {
                startVideoImpl(binding.peerView)
            }
            VideoState.PAUSED -> showPausedView()
        }
    }

    private fun startVideoImpl(peerView: PlanetKitPeerView) {
        Log.d(TAG, "startVideoImpl")
        showResumedView()
        if (!peerView.startVideo(PlanetKitVideoResolution.RECOMMENDED, null, callback = { param ->
                if (param.isSuccessful) {
                    Log.d(TAG, "startVideo response param=$param")
                }
                else Log.w(TAG, "startVideo response param=$param")
            }, resolutionCallback = { param ->
                if (param.isSuccessful) Log.d(TAG, "startVideo resolution param=$param")
                else Log.w(TAG, "startVideo resolution param=$param")
            })) {
        }
    }

    private fun showNoVideoView() {
        Log.d(TAG, "showNoVideoView")
        binding.noVideo.isVisible = true
    }

    private fun showPausedView() {
        Log.d(TAG, "showPauseVideoView")
        binding.videoPause.isVisible = true
    }

    private fun showResumedView() {
        Log.d(TAG, "showResumedView")
        binding.videoResume.isVisible = true
    }

    @Synchronized fun clearUser() {
        Log.d(TAG, "clearUser")
        showNoVideoView()
        binding.peerView.clearPeer()
    }
}