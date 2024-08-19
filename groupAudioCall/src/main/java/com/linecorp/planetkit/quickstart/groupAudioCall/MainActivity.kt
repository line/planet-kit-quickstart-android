package com.linecorp.planetkit.quickstart.groupAudioCall

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.linecorp.planetkit.PlanetKit
import com.linecorp.planetkit.PlanetKitConferenceResult
import com.linecorp.planetkit.PlanetKitStartFailReason
import com.linecorp.planetkit.audio.PlanetKitAudioRoute
import com.linecorp.planetkit.quickstart.groupAudioCall.databinding.ActivityMainBinding
import com.linecorp.planetkit.session.PlanetKitDisconnectedParam
import com.linecorp.planetkit.session.conference.ConferenceListener
import com.linecorp.planetkit.session.conference.PlanetKitConference
import com.linecorp.planetkit.session.conference.PlanetKitConferenceParam
import com.linecorp.planetkit.session.conference.PlanetKitConferencePeerListUpdatedParam

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var mConference: PlanetKitConference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.conferenceInput.userId.setText(Constants.USER_ID)
        binding.conferenceInput.accessToken.setText(Constants.ACCESS_TOKEN)


        binding.conferenceInput.btnConnect.setOnClickListener {
            val requirePermissions = Permissions.checkAllRequirePermissions(this)
            if (requirePermissions.isNotEmpty()) {
                Permissions.requestPermissions(this, requirePermissions.toTypedArray())
                return@setOnClickListener
            }

            val roomId = binding.conferenceInput.roomId.text.toString()
            val result = joinConference(roomId)
            if (result.reason == PlanetKitStartFailReason.NONE) {
                mConference = result.conference
            }
            else {
                val message = "Failed: joinConference ${result.reason}"
                Log.e(Constants.LOG_TAG, message)
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        }

        binding.conferenceConnected.btnDisconnect.setOnClickListener {
            mConference?.leaveConference()
        }
    }

    private fun joinConference(roomId: String): PlanetKitConferenceResult {
        val accessToken = binding.conferenceInput.accessToken.text.toString()
        val userId = binding.conferenceInput.userId.text.toString()
        Log.d(Constants.LOG_TAG, "accessToken=$accessToken")

        val param = PlanetKitConferenceParam.Builder()
                .myId(userId)
                .roomId(roomId)
                .myServiceId(Constants.SERVICE_ID)
                .roomServiceId(Constants.SERVICE_ID)
                .accessToken(accessToken)
                .build()
        return PlanetKit.joinConference(param, conferenceListener)
    }

    private val conferenceListener = object: ConferenceListener {
        override fun onConnected(
                conference: PlanetKitConference, isVideoHwCodecEnabled: Boolean,
                isVideoShareModeSupported: Boolean,
        ) {
            Log.i(Constants.LOG_TAG, "onConnected")
            updateConnectedUI(conference)

            // Starting from Android 14, specifying a foreground service type is required to use the microphone in the background.
            // NotificationService requirements have been implemented based on the targeting version.
            // https://developer.android.com/about/versions/14/changes/fgs-types-required#microphone
            NotificationService.showOngoingSession(this@MainActivity)
        }

        @SuppressLint("SetTextI18n")
        override fun onDisconnected(conference: PlanetKitConference, param: PlanetKitDisconnectedParam) {
            val message = "Disconnected (${param.reason})"
            Log.i(Constants.LOG_TAG, message)
            switchToConnectedUI(false)
            binding.connectionState.text = message
            mConference = null
            NotificationService.clear(this@MainActivity)
        }

        @SuppressLint("SetTextI18n")
        override fun onPeerListUpdated(param: PlanetKitConferencePeerListUpdatedParam) {
            Log.i(Constants.LOG_TAG, "onPeerListUpdated: ${param.totalPeerCnt}")
            binding.conferenceConnected.tvParticipantCount.text = "${param.totalPeerCnt + 1}"
        }
    }

    private fun updateConnectedUI(conference: PlanetKitConference) {
        switchToConnectedUI(true)
        binding.conferenceConnected.tvRoomId.text = conference.roomId
        binding.conferenceConnected.tvParticipantCount.text = "1"
        binding.connectionState.text = "Connected"

        updateAudioRouteUI(conference)
    }

    private fun updateAudioRouteUI(conference: PlanetKitConference) {
        val connectLayout = binding.conferenceConnected
        val audioSwitch = conference.getAudioSwitch()

        val availableRoutes = audioSwitch.availableAudioRoutes
        connectLayout.routeAudioBtnBt.isEnabled = availableRoutes.contains(PlanetKitAudioRoute.BLUETOOTH) == true
        connectLayout.routeAudioBtnPlugged.isEnabled = availableRoutes.contains(PlanetKitAudioRoute.PLUGGED) == true
        connectLayout.routeAudioBtnHandset.isEnabled = availableRoutes.contains(PlanetKitAudioRoute.HANDSET) == true
        connectLayout.routeAudioBtnSpeaker.isEnabled = availableRoutes.contains(PlanetKitAudioRoute.SPEAKER) == true

        when (val audioRoute = audioSwitch.selectedAudioRoute) {
            PlanetKitAudioRoute.BLUETOOTH -> connectLayout.routeAudioBtnBt.isChecked = true
            PlanetKitAudioRoute.PLUGGED -> connectLayout.routeAudioBtnPlugged.isChecked = true
            PlanetKitAudioRoute.HANDSET -> connectLayout.routeAudioBtnHandset.isChecked = true
            PlanetKitAudioRoute.SPEAKER -> connectLayout.routeAudioBtnSpeaker.isChecked = true
            else -> {
                Log.e(this.javaClass.simpleName, "onViewCreated: PlanetKitAudioRoute.$audioRoute used")
            }
        }

        conference.setOnAudioRouteChangeListener { audioRoute ->
            when(audioRoute) {
                PlanetKitAudioRoute.BLUETOOTH -> connectLayout.routeAudioBtnBt.isChecked = true
                PlanetKitAudioRoute.PLUGGED -> connectLayout.routeAudioBtnPlugged.isChecked = true
                PlanetKitAudioRoute.HANDSET -> connectLayout.routeAudioBtnHandset.isChecked = true
                PlanetKitAudioRoute.SPEAKER -> connectLayout.routeAudioBtnSpeaker.isChecked = true
                else -> {
                    Log.e(this.javaClass.simpleName, "onViewCreated: PlanetKitAudioRoute.$audioRoute used")
                }
            }
        }

        connectLayout.audioRouteSelectList.setOnCheckedChangeListener { _, _ ->
            if (connectLayout.routeAudioBtnBt.isChecked) {
                audioSwitch.setAudioRoute(PlanetKitAudioRoute.BLUETOOTH)
            }
            else if (connectLayout.routeAudioBtnPlugged.isChecked) {
                audioSwitch.setAudioRoute(PlanetKitAudioRoute.PLUGGED)
            }
            else if (connectLayout.routeAudioBtnHandset.isChecked) {
                audioSwitch.setAudioRoute(PlanetKitAudioRoute.HANDSET)
            }
            else if (connectLayout.routeAudioBtnSpeaker.isChecked) {
                audioSwitch.setAudioRoute(PlanetKitAudioRoute.SPEAKER)
            }
        }
    }

    private fun switchToConnectedUI(isConnected: Boolean) {
        if (isConnected) {
            binding.conferenceInput.root.visibility = View.GONE
            binding.conferenceConnected.root.visibility = View.VISIBLE
        }
        else {
            binding.conferenceInput.root.visibility = View.VISIBLE
            binding.conferenceConnected.root.visibility = View.GONE
        }
    }
}