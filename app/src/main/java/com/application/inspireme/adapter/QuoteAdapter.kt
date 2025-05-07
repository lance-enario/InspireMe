package com.application.inspireme.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.application.inspireme.R
import com.application.inspireme.model.Quote

class QuoteAdapter(
    private val context: Context,
    private var quotes: List<Quote>,
    private val onQuoteClick: (Quote) -> Unit
) : RecyclerView.Adapter<QuoteAdapter.QuoteViewHolder>() {

    class QuoteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val quoteText: TextView = view.findViewById(R.id.quote_text)
        val authorText: TextView = view.findViewById(R.id.author_text)
        val tagsText: TextView = view.findViewById(R.id.tags_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuoteViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_quote, parent, false)
        return QuoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuoteViewHolder, position: Int) {
        val quote = quotes[position]
        
        holder.quoteText.text = quote.quote
        holder.authorText.text = "- ${quote.author}"
        holder.tagsText.text = quote.tags.joinToString(", ", prefix = "#")
        
        holder.itemView.setOnClickListener {
            onQuoteClick(quote)
        }
    }

    override fun getItemCount() = quotes.size

    fun updateQuotes(newQuotes: List<Quote>) {
        quotes = newQuotes
        notifyDataSetChanged()
    }
}