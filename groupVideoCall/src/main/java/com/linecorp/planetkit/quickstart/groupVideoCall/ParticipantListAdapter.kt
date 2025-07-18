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

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckedTextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.linecorp.planetkit.session.conference.subgroup.PlanetKitConferencePeer

class ParticipantListAdapter(
    private val onClick: (PlanetKitConferencePeer?) -> Unit
) : RecyclerView.Adapter<ParticipantListAdapter.RadioViewHolder>() {

    private var selectedUser: PlanetKitConferencePeer? = null
    internal val differ = AsyncListDiffer(this, object : DiffUtil.ItemCallback<PlanetKitConferencePeer>() {
        override fun areItemsTheSame(oldItem: PlanetKitConferencePeer, newItem: PlanetKitConferencePeer): Boolean {
            return oldItem.userId == newItem.userId
        }
        override fun areContentsTheSame(oldItem: PlanetKitConferencePeer, newItem: PlanetKitConferencePeer): Boolean {
            return oldItem.userId == newItem.userId
        }
    })

    fun updateUsers(addedPeers: List<PlanetKitConferencePeer>,removedPeers: List<PlanetKitConferencePeer>) {
        val currentUsers = differ.currentList
        val remainingUsers = currentUsers.filter { current ->
            removedPeers.none { removed -> removed.userId == current.userId }
        }

        val updatedUsers = remainingUsers.toMutableList().apply {
            for (added in addedPeers) {
                if (none { it.userId == added.userId }) {
                    add(added)
                }
            }
        }
        val leaveSelectedUser = !findUserInList(selectedUser, removedPeers)
        if (leaveSelectedUser) selectedUser = null

        submitUserList(updatedUsers)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RadioViewHolder {
        val radioButton = LayoutInflater.from(parent.context).inflate(
            android.R.layout.simple_list_item_single_choice,
            parent,
            false
        ) as CheckedTextView
        return RadioViewHolder(radioButton)
    }

    override fun onBindViewHolder(holder: RadioViewHolder, position: Int) {
        val user = differ.currentList[position]
        holder.bind(user.userId, user == selectedUser)
        holder.itemView.setOnClickListener {
            val previousSelected = selectedUser
            selectedUser = user
            onClick(user)
            notifyItemChanged(position)
            differ.currentList.indexOf(previousSelected).takeIf { it >= 0 }?.let {
                notifyItemChanged(it)
            }
        }
    }

    override fun getItemCount(): Int = differ.currentList.size

    class RadioViewHolder(private val view: CheckedTextView) : RecyclerView.ViewHolder(view) {
        fun bind(text: String, isSelected: Boolean) {
            view.text = text
            view.isChecked = isSelected
        }
    }

    private fun submitUserList(newList: List<PlanetKitConferencePeer>) {
        differ.submitList(newList) {
            if (selectedUser == null && newList.isNotEmpty()) {
                selectedUser = newList[0]
                notifyItemChanged(newList.indexOf(selectedUser))
                onClick(selectedUser!!)
            }
        }
    }

    private fun findUserInList(user: PlanetKitConferencePeer?, list: List<PlanetKitConferencePeer>): Boolean {
        if (user == null) return false
        return list.any { it.userId == user.userId }
    }
}
