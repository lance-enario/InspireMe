package com.application.inspireme

import android.graphics.Typeface
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.application.inspireme.model.UserNotification

class   NotificationAdapter(
    private val notifications: List<UserNotification>,
    private val onNotificationClicked: (UserNotification) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.bind(notification)
    }
    
    override fun getItemCount(): Int = notifications.size
    
    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.notification_title)
        private val messageTextView: TextView = itemView.findViewById(R.id.notification_message)
        private val timeTextView: TextView = itemView.findViewById(R.id.notification_time)
        private val cardView: CardView = itemView.findViewById(R.id.notification_card)
        
        fun bind(notification: UserNotification) {
            // Set notification details
            titleTextView.text = notification.title
            messageTextView.text = notification.message
            
            // Format the time as relative time (e.g., "2 hours ago")
            val timeAgo = DateUtils.getRelativeTimeSpanString(
                notification.timestamp,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
            )
            timeTextView.text = timeAgo
            
            // Visual indication for unread notifications
            if (!notification.read) {
                titleTextView.setTypeface(null, Typeface.BOLD)
                cardView.setCardBackgroundColor(itemView.context.getColor(R.color.light_green))
            } else {
                titleTextView.setTypeface(null, Typeface.NORMAL)
                cardView.setCardBackgroundColor(itemView.context.getColor(R.color.white))
            }
            
            // Set click listener
            itemView.setOnClickListener {
                onNotificationClicked(notification)
            }
        }
    }
}