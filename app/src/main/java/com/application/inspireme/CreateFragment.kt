package com.application.inspireme

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.set
import androidx.fragment.app.Fragment
import yuku.ambilwarna.AmbilWarnaDialog

class CreateFragment : Fragment(R.layout.fragment_create) {

    private var selectedColor: Int = Color.WHITE // Default color

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val editText = view.findViewById<EditText>(R.id.editText)
        val previewText = view.findViewById<EditText>(R.id.previewText)
        val imagePreview = view.findViewById<ImageView>(R.id.imagePreview)
        val previewContainer = view.findViewById<ConstraintLayout>(R.id.previewContainer)
        val pickColorButton = view.findViewById<Button>(R.id.pickColorButton)
        val fontStyleSpinner = view.findViewById<Spinner>(R.id.fontStyleSpinner)
        val fontSizeSpinner = view.findViewById<Spinner>(R.id.fontSizeSpinner)
        val textAlignmentSpinner = view.findViewById<Spinner>(R.id.textAlignmentSpinner)
        val textStyleSpinner = view.findViewById<Spinner>(R.id.textStyleSpinner)

        // Sync editText changes with previewText
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                previewText.text = Editable.Factory.getInstance().newEditable(s)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Handle "Pick a Color" button
        pickColorButton.setOnClickListener {
            openColorPicker { color ->
                selectedColor = color
                imagePreview.setBackgroundColor(selectedColor)
                previewContainer.setBackgroundColor(selectedColor)
            }
        }

        // Text Style Spinner
        textStyleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val spannable = SpannableStringBuilder(editText.text)
                spannable.clearSpans()

                when (position) {
                    1 -> spannable[0, spannable.length] = StyleSpan(Typeface.BOLD) // Bold
                    2 -> spannable[0, spannable.length] = StyleSpan(Typeface.ITALIC) // Italic
                    3 -> spannable[0, spannable.length] = UnderlineSpan() // Underline
                }

                editText.text = spannable
                previewText.text = spannable
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Font Style Spinner
        fontStyleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val typeface = when (position) {
                    1 -> Typeface.SANS_SERIF
                    2 -> Typeface.SERIF
                    3 -> Typeface.MONOSPACE
                    else -> Typeface.DEFAULT
                }
                editText.typeface = typeface
                previewText.typeface = typeface
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Font Size Spinner
        fontSizeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val size = parent?.getItemAtPosition(position).toString().toFloat()
                editText.textSize = size
                previewText.textSize = size
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Text Alignment Spinner
        textAlignmentSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val gravity = when (position) {
                    1 -> Gravity.CENTER
                    2 -> Gravity.END
                    else -> Gravity.START
                }
                editText.gravity = gravity
                previewText.gravity = gravity
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun openColorPicker(onColorPicked: (Int) -> Unit) {
        val colorPicker = AmbilWarnaDialog(requireContext(), selectedColor, object : AmbilWarnaDialog.OnAmbilWarnaListener {
            override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                onColorPicked(color)
            }

            override fun onCancel(dialog: AmbilWarnaDialog?) {}
        })
        colorPicker.show()
    }
}