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
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.linecorp.planetkit.quickstart.common.Constants
import com.linecorp.planetkit.quickstart.common.Permissions
import com.linecorp.planetkit.quickstart.groupVideoCall.databinding.GroupVideoMainActivityBinding

class GroupVideoMainActivity : AppCompatActivity() {
    private lateinit var binding: GroupVideoMainActivityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = GroupVideoMainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.conferenceInput.userId.setText(Constants.USER_ID)
        binding.conferenceInput.accessToken.setText(Constants.ACCESS_TOKEN)
        binding.conferenceInput.btnConnect.setOnClickListener {
            val requirePermissions = Permissions.checkVideoCallRequirePermissions(this)
            if (requirePermissions.isNotEmpty()) {
                Permissions.requestPermissions(this, requirePermissions.toTypedArray())
                return@setOnClickListener
            }
            val roomId = binding.conferenceInput.roomId.text.toString()
            val accessToken = binding.conferenceInput.accessToken.text.toString()
            startGroupVideoCall(roomId, accessToken)
        }
    }

    private fun startGroupVideoCall(roomId: String, accessToken: String) {
        val intent = Intent(this, GroupVideoCallActivity::class.java)
        intent.putExtra(GroupVideoCallActivity.KEY_ROOM_ID, roomId)
        intent.putExtra(GroupVideoCallActivity.KEY_ACCESS_TOKEN, accessToken)
        startActivity(intent)
    }
}