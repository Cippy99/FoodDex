package com.example.fooddex

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessageAdapter(private val messageList: MutableList<Message>, val context: Context) : RecyclerView.Adapter<ViewHolder>() {

    val MESSAGE_SENT = 1
    val MESSAGE_RECEIVED = 2


    inner class SentViewHolder(itemView: View): ViewHolder(itemView) {
        val tvMessage = itemView.findViewById<TextView>(R.id.tvMessage)
        val tvTime = itemView.findViewById<TextView>(R.id.tvTime)
    }

    inner class ReceivedViewHolder(itemView: View): ViewHolder(itemView) {
        val tvMessage = itemView.findViewById<TextView>(R.id.tvMessage)
        val tvSenderName = itemView.findViewById<TextView>(R.id.tvSenderName)
        val tvTime = itemView.findViewById<TextView>(R.id.tvTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d("CHAT", "Creating VH")
        val view: View
        return if(viewType == MESSAGE_SENT){
            view = LayoutInflater.from(parent.context).inflate(R.layout.message_sent_item, parent, false)
            SentViewHolder(view)
        } else{
            view = LayoutInflater.from(parent.context).inflate(R.layout.message_received_item, parent, false)
            ReceivedViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messageList[position]
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        when(holder.javaClass){
            SentViewHolder::class.java -> {
                val viewHolder = holder as SentViewHolder
                viewHolder.tvMessage.text = message.text
                viewHolder.tvTime.text = sdf.format(Date(message.timestamp))
            }
            ReceivedViewHolder::class.java -> {
                val viewHolder = holder as ReceivedViewHolder
                viewHolder.tvMessage.text = message.text
                viewHolder.tvSenderName.text = message.senderName
                viewHolder.tvTime.text = sdf.format(Date(message.timestamp))
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = messageList[position]
        return if (Firebase.auth.currentUser!!.uid == message.senderId) {
            MESSAGE_SENT
        } else {
            MESSAGE_RECEIVED
        }
    }
}