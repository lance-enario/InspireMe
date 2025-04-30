package com.application.inspireme

import android.graphics.Color
import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.application.inspireme.api.FirebaseManager
import com.application.inspireme.api.QuoteService
import com.application.inspireme.model.Quote
import com.google.android.flexbox.FlexboxLayout
import android.content.Context
import com.application.inspireme.data.UserProfileCache
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreateFragment : Fragment(R.layout.fragment_create) {

    private lateinit var selectedTagsContainer: FlexboxLayout
    private lateinit var allTagsContainer: FlexboxLayout
    private lateinit var addTagsButton: Button
    private lateinit var postButton: Button
    private lateinit var quoteEditText: EditText
    private val selectedTags = mutableSetOf<String>()
    private val unselectedColor = Color.parseColor("#606060")
    private val selectedColor = Color.parseColor("#FFFFFF")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectedTagsContainer = view.findViewById(R.id.selected_tags_container)
        allTagsContainer = view.findViewById(R.id.all_tags_container)
        addTagsButton = view.findViewById(R.id.add_tags_button)
        postButton = view.findViewById(R.id.post_button)
        quoteEditText = view.findViewById(R.id.quoteEditText)
        quoteEditText.filters = arrayOf(InputFilter.LengthFilter(5000))

        addTagsButton.setOnClickListener {
            toggleTagVisibility()
        }

        postButton.setOnClickListener {
            handlePostButtonClick()
        }

        fetchTags()
    }

    private fun handlePostButtonClick() {
    val quoteText = quoteEditText.text.toString().trim()

    if (quoteText.isEmpty()) {
        Toast.makeText(requireContext(), "Please enter a quote first", Toast.LENGTH_SHORT).show()
        return
    }

    val userAuthPrefs = requireContext().getSharedPreferences("UserAuth", Context.MODE_PRIVATE)
    val userId = userAuthPrefs.getString("userId", null)
    
    if (userId == null) {
        Toast.makeText(requireContext(), "You must be logged in to post quotes", Toast.LENGTH_SHORT).show()
        return
    }


    val username = UserProfileCache.username
    
    val quote = Quote(
        id = "", 
        quote = quoteText,
        author = username,
        authorId = userId,
        tags = selectedTags.toList(),
        timestamp = System.currentTimeMillis()
    )

    FirebaseManager.saveQuote(quote) { success, quoteId ->
        if (success && quoteId != null) {
            val userQuote = quote.copy(id = quoteId)
            
            FirebaseManager.saveUserQuote(userId, userQuote) { userSuccess, _ ->
                if (userSuccess) {
                    Toast.makeText(requireContext(), "Quote posted successfully!", Toast.LENGTH_SHORT).show()

                    quoteEditText.text.clear()
                    selectedTags.clear()
                    selectedTagsContainer.removeAllViews()

                    for (i in 0 until allTagsContainer.childCount) {
                        (allTagsContainer.getChildAt(i) as? TextView)?.let { tagView ->
                            tagView.setTextColor(unselectedColor)
                            tagView.background = ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.tag_background
                            )
                        }
                    }
                    
                    if (allTagsContainer.visibility == View.VISIBLE) {
                        toggleTagVisibility()
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to associate quote with your profile", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(requireContext(), "Failed to post quote", Toast.LENGTH_SHORT).show()
        }
    }
}

    private fun updateAllTagsSelection() {
        for (i in 0 until allTagsContainer.childCount) {
            val view = allTagsContainer.getChildAt(i) as? TextView
            view?.let {
                it.setTextColor(unselectedColor)
                it.background = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.tag_background
                )
            }
        }
    }

    private fun toggleTagVisibility() {
        if (allTagsContainer.visibility == View.VISIBLE) {
            allTagsContainer.visibility = View.GONE
            addTagsButton.text = "+ Add Tags"
        } else {
            allTagsContainer.visibility = View.VISIBLE
            addTagsButton.text = "Hide Tags"
        }
    }

    private fun fetchTags() {
        val call = QuoteService.quoteApi.getTags()
        call.enqueue(object : Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                if (response.isSuccessful) {
                    val tags = response.body()
                    if (tags != null) {
                        setupTagViews(tags)
                    } else {
                        android.util.Log.d("CreateFragment", "No tags found")
                    }
                } else {
                    android.util.Log.d("CreateFragment", "Failed to fetch tags: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {
                android.util.Log.d("CreateFragment", "Error: ${t.message}")
            }
        })
    }

    private fun setupTagViews(tags: List<String>) {
        tags.forEach { tag ->
            // Create tag view for the all tags container
            val tagView = createTagView(tag, false)
            tagView.setOnClickListener {
                toggleTagSelection(tagView, tag)
            }
            allTagsContainer.addView(tagView)
        }
    }

    private fun createTagView(tag: String, isSelected: Boolean): TextView {
        val tagView = LayoutInflater.from(context).inflate(
            R.layout.tag_item,
            allTagsContainer,
            false
        ) as TextView

        tagView.text = tag
        tagView.setBackgroundResource(
            if (isSelected) R.drawable.tag_background_selected else R.drawable.tag_background
        )
        tagView.setTextColor(if (isSelected) selectedColor else unselectedColor)

        val padding = resources.getDimensionPixelSize(R.dimen.tag_padding)
        tagView.setPadding(padding, padding/2, padding, padding/2)

        val layoutParams = FlexboxLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val margin = resources.getDimensionPixelSize(R.dimen.tag_margin)
        layoutParams.setMargins(margin, margin, margin, margin)
        tagView.layoutParams = layoutParams

        return tagView
    }

    private fun toggleTagSelection(tagView: TextView, tag: String) {
        if (selectedTags.contains(tag)) {
            // Deselect tag
            selectedTags.remove(tag)
            tagView.setTextColor(unselectedColor)
            tagView.background = ContextCompat.getDrawable(requireContext(), R.drawable.tag_background)
            updateSelectedTagsView()
        } else {
            // Select tag
            selectedTags.add(tag)
            tagView.setTextColor(selectedColor)
            tagView.background = ContextCompat.getDrawable(requireContext(), R.drawable.tag_background_selected)
            updateSelectedTagsView()
        }
    }

    private fun updateSelectedTagsView() {
        selectedTagsContainer.removeAllViews()
        selectedTags.forEach { tag ->
            val selectedTagView = createTagView(tag, true)
            selectedTagView.setOnClickListener {
                // Remove tag when clicked
                selectedTags.remove(tag)
                updateSelectedTagsView()
                // Also update the corresponding tag in all tags container
                updateTagInAllTagsContainer(tag, false)
            }
            selectedTagsContainer.addView(selectedTagView)
        }
    }

    private fun updateTagInAllTagsContainer(tag: String, isSelected: Boolean) {
        for (i in 0 until allTagsContainer.childCount) {
            val view = allTagsContainer.getChildAt(i) as? TextView
            if (view?.text == tag) {
                view.setTextColor(if (isSelected) selectedColor else unselectedColor)
                view.background = ContextCompat.getDrawable(
                    requireContext(),
                    if (isSelected) R.drawable.tag_background_selected else R.drawable.tag_background
                )
                break
            }
        }
    }
}