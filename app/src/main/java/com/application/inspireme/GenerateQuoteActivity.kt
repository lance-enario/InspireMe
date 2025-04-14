package com.application.inspireme

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.application.inspireme.api.QuoteService
import com.application.inspireme.model.QuoteResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GenerateQuoteActivity : AppCompatActivity() {
    private lateinit var quoteTextView: TextView
    private lateinit var authorTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generate_quote)

        quoteTextView = findViewById(R.id.quote_text)
        authorTextView = findViewById(R.id.author_text)

        fetchRandomQuote()

        val onBack = findViewById<ImageView>(R.id.back_icon_left)
        onBack.setOnClickListener {
            onBackClick()
        }

        // Handle generate quote button click
        val generateQuoteButton = findViewById<Button>(R.id.generate_quote_button)
        generateQuoteButton.setOnClickListener {
            fetchRandomQuote()
        }
    }


    private fun fetchRandomQuote() {
        QuoteService.quoteApi.getRandomQuote().enqueue(object : Callback<QuoteResponse> {
            override fun onResponse(call: Call<QuoteResponse>, response: Response<QuoteResponse>) {
                if (response.isSuccessful) {
                    val quoteResponse = response.body()
                    quoteResponse?.let {
                        quoteTextView.text = "\"${it.quote}\""
                        authorTextView.text = "— ${it.author}"
                    }
                } else {
                    Log.e("QuoteAPI", "Error: ${response.code()}")
                    Toast.makeText(this@GenerateQuoteActivity, "Failed to load quote: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<QuoteResponse>, t: Throwable) {
                Log.e("QuoteAPI", "Network error: ${t.message}")
                Toast.makeText(this@GenerateQuoteActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun onBackClick() {
        val intent = Intent(this, NavigationBarActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}