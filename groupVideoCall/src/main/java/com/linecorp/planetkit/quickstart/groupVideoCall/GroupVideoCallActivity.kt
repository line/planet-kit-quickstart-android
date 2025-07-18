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
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.linecorp.planetkit.PlanetKit
import com.linecorp.planetkit.PlanetKitConferenceResult
import com.linecorp.planetkit.PlanetKitInitialMyVideoState
import com.linecorp.planetkit.PlanetKitMediaType
import com.linecorp.planetkit.PlanetKitStartFailReason
import com.linecorp.planetkit.PlanetKitVideoPauseReason
import com.linecorp.planetkit.quickstart.common.Constants
import com.linecorp.planetkit.quickstart.groupVideoCall.databinding.GroupVideoCallActivityBinding
import com.linecorp.planetkit.session.PlanetKitDisconnectedParam
import com.linecorp.planetkit.session.conference.ConferenceListener
import com.linecorp.planetkit.session.conference.PlanetKitConference
import com.linecorp.planetkit.session.conference.PlanetKitConferenceParam
import com.linecorp.planetkit.session.conference.PlanetKitConferencePeerListUpdatedParam

class GroupVideoCallActivity: AppCompatActivity() {
    private lateinit var binding: GroupVideoCallActivityBinding
    private lateinit var adapter: ParticipantListAdapter
    private var conference: PlanetKitConference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = GroupVideoCallActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val roomId = intent.getStringExtra(KEY_ROOM_ID)
        val accessToken = intent.getStringExtra(KEY_ACCESS_TOKEN)
        Log.d(TAG, "Room Id=$roomId")
        Log.d(TAG, "accessToken=$accessToken")

        val result = joinConference(roomId!!, accessToken!!)
        if (result.reason != PlanetKitStartFailReason.NONE) {
            showDialog(this@GroupVideoCallActivity, "Start fail with ${result.reason}", "OK") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            return
        }

        conference = result.conference
        binding.peerList.layoutManager = LinearLayoutManager(this)
        adapter = ParticipantListAdapter { selectedItem ->
            if (selectedItem == null) {
                binding.simplePeerView.clearUser()
            }
            else {
                binding.simplePeerView.setUser(selectedItem)
            }

        }
        binding.peerList.adapter = adapter
        binding.btnLeave.setOnClickListener {
            conference!!.leaveConference()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Do nothing to disable the back button
            }
        })
    }

    override fun onStop() {
        super.onStop()
        conference?.pauseMyVideo(PlanetKitVideoPauseReason.BY_USER)
    }

    override fun onStart() {
        super.onStart()
        conference?.resumeMyVideo()
    }

    private val conferenceListener = object: ConferenceListener {
        override fun onConnected(
            conference: PlanetKitConference, isVideoHwCodecEnabled: Boolean,
            isVideoShareModeSupported: Boolean,
        ) {
            Log.i(TAG, "onConnected")

            // Starting from Android 14, specifying a foreground service type is required to use the microphone in the background.
            // NotificationService requirements have been implemented based on the targeting version.
            // https://developer.android.com/about/versions/14/changes/fgs-types-required#microphone
            GroupVideoNotificationService.showOngoingSession(this@GroupVideoCallActivity)
            binding.simpleMyView.setMe()
        }

        @SuppressLint("SetTextI18n")
        override fun onDisconnected(conference: PlanetKitConference, param: PlanetKitDisconnectedParam) {
            val message = "Disconnected (${param.reason})"
            Log.i(TAG, message)
            GroupVideoNotificationService.clear(this@GroupVideoCallActivity)

            showDialog(this@GroupVideoCallActivity, "Disconnected with ${param.reason}", "OK") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
        }

        @SuppressLint("SetTextI18n")
        override fun onPeerListUpdated(param: PlanetKitConferencePeerListUpdatedParam) {
            Log.i(TAG, "onPeerListUpdated: ${param.totalPeerCnt}")
            adapter.updateUsers(param.addedPeers, param.removedPeers)
        }
    }

    private fun joinConference(roomId: String, accessToken: String): PlanetKitConferenceResult {
        val param = PlanetKitConferenceParam.Builder()
            .roomId(roomId)
            .myId(Constants.USER_ID)
            .myServiceId(Constants.SERVICE_ID)
            .roomServiceId(Constants.SERVICE_ID)
            .accessToken(accessToken)
            .mediaType(PlanetKitMediaType.AUDIOVIDEO)
            .setInitialMyVideoState(PlanetKitInitialMyVideoState.RESUME)
            .build()
        return PlanetKit.joinConference(param, conferenceListener)
    }

    fun showDialog(context: Context, message: String,buttonName: String,
                   clickListener: DialogInterface.OnClickListener): AlertDialog {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(message)
            .setPositiveButton(buttonName, clickListener)
        val dialog: AlertDialog = builder.create()
        dialog.show()
        return dialog
    }

    companion object {
        const val TAG = "GroupVideoCallActivity"
        const val KEY_ROOM_ID = "KEY_ROOM_ID"
        const val KEY_ACCESS_TOKEN = "KEY_ACCESS_TOKEN"
    }
}