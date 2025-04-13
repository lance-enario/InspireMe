import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.application.inspireme.R
import com.application.inspireme.api.QuoteService
import com.application.inspireme.model.QuoteResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment(R.layout.fragment_home) {
    
    private lateinit var quoteTextView: TextView
    private lateinit var authorTextView: TextView
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        quoteTextView = view.findViewById(R.id.quote_text)
        authorTextView = view.findViewById(R.id.quote_author)

        fetchRandomQuote()
        
        return view
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
                    Toast.makeText(context, "Failed to load quote: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
            
            override fun onFailure(call: Call<QuoteResponse>, t: Throwable) {
                Log.e("QuoteAPI", "Network error: ${t.message}")
                Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}