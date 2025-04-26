package com.application.inspireme.api

import com.application.inspireme.model.QuoteResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface QuoteApi {
    @GET("quotes/random")
    fun getRandomQuote(): Call<QuoteResponse>
    
    // Optional: Add more API endpoints if needed
    @GET("quotes")
    fun getQuoteByTag(@Query("tag") tag: String): Call<QuoteResponse>
}