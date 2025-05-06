package com.application.inspireme.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.application.inspireme.R
import com.application.inspireme.data.UserProfileCache
import com.application.inspireme.model.User
import de.hdodenhof.circleimageview.CircleImageView

class FollowerAdapter(
    private val context: Context,
    private var followers: List<User>,
    private val onItemClick: (User) -> Unit
) : RecyclerView.Adapter<FollowerAdapter.FollowerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_follower, parent, false)
        return FollowerViewHolder(view)
    }

    override fun onBindViewHolder(holder: FollowerViewHolder, position: Int) {
        val follower = followers[position]
        holder.bind(follower)
    }

    override fun getItemCount(): Int = followers.size

    fun updateFollowers(newFollowers: List<User>) {
        followers = newFollowers
        notifyDataSetChanged()
    }

    inner class FollowerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImageView: CircleImageView = itemView.findViewById(R.id.imageView_follower_profile)
        private val usernameTextView: TextView = itemView.findViewById(R.id.textView_follower_username)

        fun bind(user: User) {
            usernameTextView.text = user.username
            val profileImageResId = UserProfileCache.profileImages[user.profileId] ?: R.drawable.profile // Fallback
            profileImageView.setImageResource(profileImageResId)

            itemView.setOnClickListener {
                onItemClick(user)
            }
        }
    }
}
