import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.application.inspireme.R
import com.application.inspireme.SettingsPageActivity
import com.application.inspireme.api.FirebaseManager
import com.application.inspireme.api.QuoteService
import com.application.inspireme.model.QuoteResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment(R.layout.fragment_home) {
    
    private lateinit var quoteTextView: TextView
    private lateinit var authorTextView: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ImageButton>(R.id.button_settings).setOnClickListener {
            val intent = Intent(requireContext(), SettingsPageActivity::class.java)
            intent.putExtra("previous_fragment", "HomeFragment")
            startActivity(intent)
        }

    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        quoteTextView = view.findViewById(R.id.quote_text)
        authorTextView = view.findViewById(R.id.quote_author)

        // disabled for now because it calls the API every time the fragment is created and stores the quote in the database
        // which is very bad HAHAHAHAAH
        // fetchRandomQuote()
        
        return view
    }

     private fun displayQuote(quoteResponse: QuoteResponse) {
        // Update the UI with the quote data
        quoteTextView.text = "\"${quoteResponse.quote}\""
        authorTextView.text = "- ${quoteResponse.author}"
        
        // Optional: Add animation for smoother transitions
        quoteTextView.alpha = 0f
        authorTextView.alpha = 0f
        
        quoteTextView.animate().alpha(1f).setDuration(500).start()
        authorTextView.animate().alpha(1f).setDuration(500).start()
    }


    private fun fetchRandomQuote() {
        QuoteService.quoteApi.getRandomQuote().enqueue(object : Callback<QuoteResponse> {
            override fun onResponse(call: Call<QuoteResponse>, response: Response<QuoteResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val quoteResponse = response.body()!!
                    displayQuote(quoteResponse)
                    
                    // Optionally save to Firebase
                    FirebaseManager.saveApiQuote(quoteResponse) { success, _ ->
                        if (!success) {
                            Log.e("HomeFragment", "Failed to save quote to database")
                        }
                    }
                } else {
                    // Handle error
                }
            }   
            
            override fun onFailure(call: Call<QuoteResponse>, t: Throwable) {
                Log.e("QuoteAPI", "Network error: ${t.message}")
                Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}