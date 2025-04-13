package com.application.inspireme.api

import com.application.inspireme.model.QuoteResponse
import retrofit2.Call
import retrofit2.http.GET

interface QuoteApi {
    @GET("quotes/random")
    fun getRandomQuote(): Call<QuoteResponse>
}