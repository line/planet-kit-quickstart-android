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

package com.linecorp.planetkit.quickstart.groupAudioCall

import android.app.Notification
import android.content.Intent
import android.content.Context
import android.os.Build
import com.linecorp.planetkit.quickstart.common.NotificationService

class GroupAudioNotificationService : NotificationService() {

    companion object {
        @JvmStatic
        fun showOngoingSession(context: Context) {
            startServiceWithAction(context, ACTION_CONNECT_SESSION)
        }

        @JvmStatic
        fun clear(context: Context) {
            startServiceWithAction(context, ACTION_CLEAR)
        }

        private fun startServiceWithAction(context: Context, action: String) {
            val intent = Intent(context, GroupAudioNotificationService::class.java).apply {
                this.action = action
            }

            val shouldStartForeground = action == ACTION_CONNECT_SESSION && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            if (shouldStartForeground) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    override val notificationId: Int = 300
    override val channelId: String = "GROUP_AUDIO_ONGOING_CHANNEL_ID"
    override val channelName: String = "Group Audio Call in Progress"

    override fun createNotification(): Notification {
        return buildNotification(GroupAudioMainActivity::class.java, R.mipmap.ic_dev_launcher)
    }
}
