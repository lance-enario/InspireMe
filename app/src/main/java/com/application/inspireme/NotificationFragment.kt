package com.application.inspireme

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.application.inspireme.api.FirebaseManager
import com.application.inspireme.model.UserNotification
import com.google.firebase.auth.FirebaseAuth

class NotificationFragment : Fragment() {
    private lateinit var notificationsRecyclerView: RecyclerView
    private lateinit var emptyStateView: View
    private lateinit var markAllReadButton: Button
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notification, container, false)
        
        notificationsRecyclerView = view.findViewById(R.id.notifications_recycler_view)
        emptyStateView = view.findViewById(R.id.empty_state_container)
        markAllReadButton = view.findViewById(R.id.mark_all_read_button)
        
        notificationsRecyclerView.layoutManager = LinearLayoutManager(context)

        markAllReadButton.setOnClickListener {
            markAllNotificationsAsRead()
        }

        loadNotifications()
        
        return view
    }
    
    override fun onResume() {
        super.onResume()
        // Reload notifications when returning to this fragment
        loadNotifications()
    }
    
    private fun loadNotifications() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        
        if (userId == null) {
            showEmptyState("You need to be logged in to view notifications")
            return
        }
        
        FirebaseManager.getNotifications(userId, { notifications ->
            if (notifications.isEmpty()) {
                showEmptyState("No notifications yet")
                markAllReadButton.visibility = View.GONE
            } else {
                emptyStateView.visibility = View.GONE
                notificationsRecyclerView.visibility = View.VISIBLE
                markAllReadButton.visibility = View.VISIBLE

                val adapter = NotificationAdapter(notifications) { notification ->
                    markNotificationAsRead(notification)
                }
                notificationsRecyclerView.adapter = adapter
            }
        }, { error ->
            showEmptyState("Failed to load notifications: $error")
        })
    }
    
    private fun showEmptyState(message: String) {
        emptyStateView.visibility = View.VISIBLE
        notificationsRecyclerView.visibility = View.GONE
        emptyStateView.findViewById<TextView>(R.id.empty_state_message).text = message
    }
    
    private fun markNotificationAsRead(notification: UserNotification) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        
        if (!notification.read) {
            FirebaseManager.markNotificationAsRead(userId, notification.id) { success ->
                if (success) {
                    loadNotifications()
                }
            }
        }
    }
    
    private fun markAllNotificationsAsRead() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        
        FirebaseManager.markAllNotificationsAsRead(userId) { success ->
            if (success) {
                loadNotifications()
            }
        }
    }
}