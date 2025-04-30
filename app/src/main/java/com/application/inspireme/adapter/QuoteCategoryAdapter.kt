package com.application.inspireme

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.application.inspireme.model.Quote

class QuoteCategoryAdapter(
    private val quotes: List<Quote>,
    private val likeStatusMap: Map<String, Boolean>,
    private val onLikeClicked: (Quote, Boolean) -> Unit
) : RecyclerView.Adapter<QuoteCategoryAdapter.QuoteViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_quote_card, parent, false)
        return QuoteViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: QuoteViewHolder, position: Int) {
        val quote = quotes[position]
        val isLiked = likeStatusMap[quote.id] ?: false
        holder.bind(quote, isLiked)
    }
    
    override fun getItemCount(): Int = quotes.size
    
    inner class QuoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val quoteTextView: TextView = itemView.findViewById(R.id.quote_text)
        private val authorTextView: TextView = itemView.findViewById(R.id.quote_author)
        private val likeButton: ImageButton = itemView.findViewById(R.id.like_button)
        
        fun bind(quote: Quote, isLiked: Boolean) {
            quoteTextView.text = "\"${quote.quote}\""
            authorTextView.text = "- ${quote.author}"
            
            // Set like button icon based on like status
            likeButton.setImageResource(
                if (isLiked) R.drawable.ic_liked 
                else R.drawable.ic_like
            )
            
            // Set like button click listener
            likeButton.setOnClickListener {
                // Toggle like status
                val newLikeStatus = !isLiked
                
                // Update UI immediately
                likeButton.setImageResource(
                    if (newLikeStatus) R.drawable.ic_liked 
                    else R.drawable.ic_like
                )
                
                // Trigger callback
                onLikeClicked(quote, newLikeStatus)
            }
        }
    }
}