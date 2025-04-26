package com.application.inspireme.api

import com.application.inspireme.model.Quote
import com.application.inspireme.model.QuoteResponse
import com.application.inspireme.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.tasks.await

object FirebaseManager {
    private val database = FirebaseDatabase.getInstance()
    private val usersRef = database.getReference("users")
    private val quotesRef = database.getReference("quotes")

    fun saveUser(userId: String, user: User, onComplete: (Boolean) -> Unit) {
        usersRef.child(userId).setValue(user)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }
    
    fun updateUserProfile(userId: String, updates: Map<String, Any>, onComplete: (Boolean) -> Unit) {
        usersRef.child(userId).updateChildren(updates)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }
    
    fun getUserData(userId: String, onSuccess: (User) -> Unit, onFailure: (String) -> Unit) {
        usersRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        onSuccess(user)
                    } else {
                        onFailure("Failed to parse user data")
                    }
                } else {
                    onFailure("User not found")
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                onFailure(error.message)
            }
        })
    }
    
    // Quote Operations
    fun saveQuote(quote: Quote, onComplete: (Boolean, String?) -> Unit) {
        val quoteId = quotesRef.push().key ?: return onComplete(false, "Failed to generate ID")
        
        // Set the ID in the quote object
        val updatedQuote = quote.copy(id = quoteId)
        
        quotesRef.child(quoteId).setValue(updatedQuote)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful, if (task.isSuccessful) quoteId else null)
            }
    }
    
    fun saveApiQuote(quoteResponse: QuoteResponse, onComplete: (Boolean, String?) -> Unit) {
        // Convert API QuoteResponse to our Quote model
        val quote = Quote(
            id = "api_${quoteResponse.id}",
            quote = quoteResponse.quote,
            author = quoteResponse.author,
            length = quoteResponse.length,
            tags = quoteResponse.tags
        )
        
        quotesRef.child(quote.id).setValue(quote)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful, if (task.isSuccessful) quote.id else null)
            }
    }
    
    fun getAllQuotes(onSuccess: (List<Quote>) -> Unit, onFailure: (String) -> Unit) {
        quotesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val quotesList = mutableListOf<Quote>()
                for (quoteSnapshot in snapshot.children) {
                    val quote = quoteSnapshot.getValue(Quote::class.java)
                    if (quote != null) {
                        quotesList.add(quote)
                    }
                }
                onSuccess(quotesList)
            }
            
            override fun onCancelled(error: DatabaseError) {
                onFailure(error.message)
            }
        })
    }
    
    fun getQuoteById(quoteId: String, onSuccess: (Quote) -> Unit, onFailure: (String) -> Unit) {
        quotesRef.child(quoteId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val quote = snapshot.getValue(Quote::class.java)
                    if (quote != null) {
                        onSuccess(quote)
                    } else {
                        onFailure("Failed to parse quote data")
                    }
                } else {
                    onFailure("Quote not found")
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                onFailure(error.message)
            }
        })
    }
    
    // User-specific quotes (if you implement user-saved quotes later)
    fun saveUserQuote(userId: String, quote: Quote, onComplete: (Boolean, String?) -> Unit) {
        val userQuotesRef = database.getReference("userQuotes/$userId")
        val quoteId = userQuotesRef.push().key ?: return onComplete(false, "Failed to generate ID")
        
        // Set the ID in the quote object
        val updatedQuote = quote.copy(id = quoteId)
        
        userQuotesRef.child(quoteId).setValue(updatedQuote)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful, if (task.isSuccessful) quoteId else null)
            }
    }
}