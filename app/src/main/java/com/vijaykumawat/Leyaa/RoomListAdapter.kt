package com.vijaykumawat.Leyaa

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

interface OnRoomClickListener {
    fun onRoomClick(roomId: String, position: Int)
}

class RoomListAdapter(
    private val listener: OnRoomClickListener
) : ListAdapter<RoomModel, RoomListAdapter.RoomHolder>(RoomDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.room_title, parent, false)
        return RoomHolder(view)
    }

    override fun onBindViewHolder(holder: RoomHolder, position: Int) {
        val room = getItem(position)
        holder.bind(room, position)
    }

    inner class RoomHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val roomTitle: TextView = itemView.findViewById(R.id.room_title)

        fun bind(room: RoomModel, position: Int) {
            roomTitle.text = room.title
            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    listener.onRoomClick(room.id, adapterPosition)
                }
            }
        }
    }
}

class RoomDiffCallback : DiffUtil.ItemCallback<RoomModel>() {
    override fun areItemsTheSame(oldItem: RoomModel, newItem: RoomModel): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: RoomModel, newItem: RoomModel): Boolean {
        return oldItem == newItem
    }
}
