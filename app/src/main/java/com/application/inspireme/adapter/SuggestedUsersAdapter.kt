package com.application.inspireme.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.application.inspireme.R
import com.application.inspireme.listeners.OnUserProfileClickListener
import com.application.inspireme.model.User
import de.hdodenhof.circleimageview.CircleImageView

class SuggestedUsersAdapter(
    private val users: List<User>,
    private val onFollowClicked: (User, Boolean) -> Unit,
    private val onUserProfileClickListener: OnUserProfileClickListener
) : RecyclerView.Adapter<SuggestedUsersAdapter.UserViewHolder>() {

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImage: CircleImageView = view.findViewById(R.id.user_profile_image)
        val usernameText: TextView = view.findViewById(R.id.username_text)
        val followButton: Button = view.findViewById(R.id.follow_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_suggested_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        
        // Set profile image
        holder.profileImage.setImageResource(R.drawable.profile)
        
        // Set username
        holder.usernameText.text = user.username
        
        // Set follow button click listener
        holder.followButton.setOnClickListener {
            onFollowClicked(user, true)
        }

        // Add click listeners for profile navigation
        holder.profileImage.setOnClickListener {
            onUserProfileClickListener.onUserProfileClicked(user.id)
        }
        holder.usernameText.setOnClickListener {
            onUserProfileClickListener.onUserProfileClicked(user.id)
        }
    }

    override fun getItemCount() = users.size
}