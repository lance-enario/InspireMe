package com.application.inspireme.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.application.inspireme.R
import com.application.inspireme.model.Quote
import de.hdodenhof.circleimageview.CircleImageView

class QuoteFeedAdapter(
    private val context: Context,
    private var quotes: List<Quote>,
    private val onLikeClicked: (Quote, Boolean) -> Unit
) : RecyclerView.Adapter<QuoteFeedAdapter.QuoteViewHolder>() {

    // Map to track like status
    private val likeStatusMap = mutableMapOf<String, Boolean>()
    
    class QuoteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userProfileImage: CircleImageView = view.findViewById(R.id.user_profile_image)
        val usernameText: TextView = view.findViewById(R.id.username_text)
        val quoteText: TextView = view.findViewById(R.id.quote_text)
        val authorText: TextView = view.findViewById(R.id.quote_author)
        val likeButton: ImageButton = view.findViewById(R.id.like_button)
        val tagsText: TextView = view.findViewById(R.id.tags_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuoteViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_quote_feed, parent, false)
        return QuoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuoteViewHolder, position: Int) {
    val quote = quotes[position]
    val isLiked = likeStatusMap[quote.id] ?: false
    
    // Set user profile image
    holder.userProfileImage.setImageResource(R.drawable.profile)
    
    // Set username
    holder.usernameText.text = quote.author
    
    // Set quote text
    holder.quoteText.text = "${quote.quote}"
    
    // Set author text
    holder.authorText.text = "— ${quote.author}"
    
    // If this is a discovery quote, show an indicator
    if (quote.isDiscovery && holder.itemView.context.resources != null) {
        holder.tagsText?.visibility = View.VISIBLE
    } else {
        holder.tagsText?.visibility = View.GONE
    }
    
    // Set tags if available
    if (quote.tags.isNotEmpty()) {
        holder.tagsText.visibility = View.VISIBLE
        holder.tagsText.text = quote.tags.joinToString(" ") { "#$it" }
    } else {
        holder.tagsText.visibility = View.GONE
    }
        
        // Set like button
        holder.likeButton.setImageResource(
            if (isLiked) R.drawable.ic_liked 
            else R.drawable.ic_like
        )
        
        // Set like button click listener
        holder.likeButton.setOnClickListener {
            val newLikeStatus = !isLiked
            likeStatusMap[quote.id] = newLikeStatus
            holder.likeButton.setImageResource(
                if (newLikeStatus) R.drawable.ic_liked 
                else R.drawable.ic_like
            )
            onLikeClicked(quote, newLikeStatus)
        }
    }

    override fun getItemCount() = quotes.size
    
    fun updateLikeStatus(quoteId: String, isLiked: Boolean) {
        likeStatusMap[quoteId] = isLiked
        notifyDataSetChanged()
    }
}