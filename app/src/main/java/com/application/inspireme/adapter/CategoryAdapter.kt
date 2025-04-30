package com.application.inspireme

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.application.inspireme.model.Quote
import com.application.inspireme.CategoryFragment.CategoryData;

class CategoryAdapter(
    var categoryDataList: List<CategoryData>,
    private val onLikeClicked: (Quote, Boolean) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {
    
    // Map to keep track of like status for each quote
    private val likeStatusMap = mutableMapOf<String, Boolean>()
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val categoryData = categoryDataList[position]
        holder.bind(categoryData)
    }
    
    override fun getItemCount(): Int = categoryDataList.size
    
    fun updateData(newList: List<CategoryData>) {
        categoryDataList = newList
        notifyDataSetChanged()
    }
    
    fun updateLikeStatus(quoteId: String, isLiked: Boolean) {
        likeStatusMap[quoteId] = isLiked
        notifyDataSetChanged()
    }
    
    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryNameTextView: TextView = itemView.findViewById(R.id.category_name)
        private val quotesRecyclerView: RecyclerView = itemView.findViewById(R.id.quotes_recycler_view)
        
        fun bind(categoryData: CategoryData) {
            // Set the category name with first letter capitalized
            categoryNameTextView.text = categoryData.categoryName.capitalize()
            
            // Set up the nested RecyclerView for quotes
            quotesRecyclerView.layoutManager = LinearLayoutManager(
                itemView.context,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            
            // Create and set adapter for quotes
            val quoteAdapter = QuoteCategoryAdapter(
                categoryData.quotes,
                likeStatusMap
            ) { quote, isLiked ->
                onLikeClicked(quote, isLiked)
            }
            
            quotesRecyclerView.adapter = quoteAdapter
        }
    }
}