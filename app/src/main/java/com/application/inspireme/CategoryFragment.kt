package com.application.inspireme

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.application.inspireme.api.FirebaseManager
import com.application.inspireme.model.Quote
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.atomic.AtomicInteger

class CategoryFragment : Fragment() {
    private lateinit var categoriesRecyclerView: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter
    
    private val categories = listOf(
        "motivation", "inspiration", "life", "wisdom", "love", 
        "success", "leadership", "happiness", "change", "perseverance",
        "mindfulness", "growth", "courage", "gratitude", "resilience", 
        "friendship", "creativity", "humility", "forgiveness", "patience", 
        "integrity", "self-reflection", "empathy", "purpose", "justice", 
        "harmony", "knowledge", "hope", "anger", "fear", "general"
    )
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_category, container, false)
        
        categoriesRecyclerView = view.findViewById(R.id.categories_recycler_view)
        categoriesRecyclerView.layoutManager = LinearLayoutManager(context)
        
        // Initialize adapter with empty lists for each category
        val categoryDataList = categories.map { CategoryData(it, emptyList()) }
        categoryAdapter = CategoryAdapter(categoryDataList) { quote, isLiked ->
            handleQuoteLike(quote, isLiked)
        }
        
        categoriesRecyclerView.adapter = categoryAdapter
        
        // Load quotes for each category
        loadQuotesByCategories()
        
        return view
    }
    
    private fun loadQuotesByCategories() {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    
    if (userId == null) {
        Toast.makeText(context, "You need to be logged in to view categories", Toast.LENGTH_SHORT).show()
        return
    }
    
    // Create an empty mutable list to represent the initial state
    val initialData = categories.map { CategoryData(it, emptyList()) }
    categoryAdapter.updateData(initialData)
    
    // Use an atomic counter to track when all categories have been loaded
    val pendingCategories = AtomicInteger(categories.size)
    
    // For each category, fetch quotes
    categories.forEachIndexed { index, category ->
        FirebaseManager.getQuotesByTag(category, 
            onSuccess = { quotes ->
                activity?.runOnUiThread {
                    // Create a copy of the current data list
                    val currentList = categoryAdapter.categoryDataList.toMutableList()
                    
                    // Update the quotes for this category
                    currentList[index] = CategoryData(category, quotes)
                    
                    // Set new list in adapter
                    categoryAdapter.updateData(currentList)
                    
                    // Check like status for each quote in a batch
                    checkLikeStatusForQuotes(userId, quotes)
                    
                    // Decrease counter
                    pendingCategories.decrementAndGet()
                }
            },
            onFailure = { error ->
                activity?.runOnUiThread {
                    // Just decrease counter but don't update adapter with empty data
                    pendingCategories.decrementAndGet()
                    
                    // Only show toast for the first error to avoid multiple messages
                    if (pendingCategories.get() == categories.size - 1) {
                        Toast.makeText(context, "Error loading some categories", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }
}

// Helper method to batch check like status
private fun checkLikeStatusForQuotes(userId: String, quotes: List<Quote>) {
    for (quote in quotes) {
        FirebaseManager.checkIfQuoteLiked(userId, quote.id) { isLiked ->
            activity?.runOnUiThread {
                categoryAdapter.updateLikeStatus(quote.id, isLiked)
            }
        }
    }
}

private fun handleQuoteLike(quote: Quote, isLiked: Boolean) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    
    if (isLiked) {
        FirebaseManager.likeQuote(userId, quote) { success ->
            if (!success) {
                activity?.runOnUiThread {
                    Toast.makeText(context, "Failed to like quote", Toast.LENGTH_SHORT).show()
                    // Revert the UI change
                    categoryAdapter.updateLikeStatus(quote.id, false)
                }
            }
        }
    } else {
        FirebaseManager.unlikeQuote(userId, quote.id) { success ->
            if (!success) {
                activity?.runOnUiThread {
                    Toast.makeText(context, "Failed to unlike quote", Toast.LENGTH_SHORT).show()
                    // Revert the UI change
                    categoryAdapter.updateLikeStatus(quote.id, true)
                }
            }
        }
    }
}

// Data class to hold category name and its quotes
data class CategoryData(
    val categoryName: String,
    val quotes: List<Quote>
)}