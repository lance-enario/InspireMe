package com.application.inspireme.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object QuoteService {
    private const val BASE_URL = "https://quoteslate.vercel.app/api/"
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val quoteApi: QuoteApi = retrofit.create(QuoteApi::class.java)
}