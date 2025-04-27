package com.application.inspireme

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.set
import androidx.fragment.app.Fragment
import com.yalantis.ucrop.UCrop
import yuku.ambilwarna.AmbilWarnaDialog
import java.io.File

class CreateFragment : Fragment(R.layout.fragment_create) {

    private var selectedColor: Int = Color.WHITE // Default color

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                val imageUri = result.data?.data
                if (imageUri != null) {
                    launchImageCropper(imageUri)
                } else {
                    Toast.makeText(requireContext(), "Failed to pick image", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private val cropImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK && result.data != null) {
                val resultUri = UCrop.getOutput(result.data!!)
                if (resultUri != null) {
                    updatePreviewImage(resultUri)
                }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val editText = view.findViewById<EditText>(R.id.editText)
        val previewText = view.findViewById<EditText>(R.id.previewText)
        val imagePreview = view.findViewById<ImageView>(R.id.imagePreview)
        val previewImage = view.findViewById<ImageView>(R.id.previewImage)
        val pickColorButton = view.findViewById<Button>(R.id.pickColorButton)
        val uploadImageButton = view.findViewById<Button>(R.id.uploadImageButton)
        val fontStyleSpinner = view.findViewById<Spinner>(R.id.fontStyleSpinner)
        val fontSizeSpinner = view.findViewById<Spinner>(R.id.fontSizeSpinner)
        val textAlignmentSpinner = view.findViewById<Spinner>(R.id.textAlignmentSpinner)
        val textColorPickerButton = view.findViewById<Button>(R.id.textColorPickerButton)
        val postButton = view.findViewById<Button>(R.id.postButton)
        val defaultImagePreviewBackground = imagePreview.background
        val defaultPreviewImageBackground = previewImage.background

        postButton.setOnClickListener {
            val textContent = editText.text.toString()
            val textColor = editText.currentTextColor
            val textSize = editText.textSize
            val textAlignment = editText.gravity
            val fontStyle = editText.typeface
            val backgroundDrawable = previewImage.background
            val imageDrawable = imagePreview.drawable

            if (textContent.isBlank()) {
                Toast.makeText(requireContext(), "Text cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Example: Process the post data
            // Save the post, send it to a server, or navigate to another screen
            Toast.makeText(requireContext(), "Post created successfully!", Toast.LENGTH_SHORT).show()

            // Optionally, clear the fields after posting
            editText.text.clear()
            previewText.text.clear()
            imagePreview.setImageDrawable(null)
            previewImage.setImageDrawable(null)
            imagePreview.background = defaultImagePreviewBackground
            previewImage.background = defaultPreviewImageBackground
        }

        textColorPickerButton.setOnClickListener {
            openColorPicker { color ->
                editText.setTextColor(color)
                previewText.setTextColor(color)
            }
        }


        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                previewText.text = Editable.Factory.getInstance().newEditable(s)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        pickColorButton.setOnClickListener {
            openColorPicker { color ->
                selectedColor = color
                imagePreview.setBackgroundColor(selectedColor)
                previewImage.setBackgroundColor(selectedColor)
            }
        }

        uploadImageButton.setOnClickListener {
            openImagePicker()
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

        fontSizeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val size = parent?.getItemAtPosition(position).toString().toFloat()
                editText.textSize = size
                previewText.textSize = size
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

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


    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun launchImageCropper(sourceUri: Uri) {
        val destinationUri = Uri.fromFile(File(requireContext().cacheDir, "cropped_image.jpg"))
        val uCropOptions = UCrop.Options().apply {
            setCompressionQuality(90)
            setHideBottomControls(false)
            setFreeStyleCropEnabled(false) // Lock aspect ratio
            setShowCropGrid(true)
            setShowCropFrame(true)
            setToolbarTitle("Crop Image")
        }

        val uCropIntent = UCrop.of(sourceUri, destinationUri)
            .withAspectRatio(1.91f, 1f) // Lock to 1.91:1 aspect ratio
            .withOptions(uCropOptions)
            .getIntent(requireContext())

        cropImageLauncher.launch(uCropIntent)
    }

    private fun updatePreviewImage(uri: Uri) {
        val imagePreview = view?.findViewById<ImageView>(R.id.imagePreview)
        val previewContainer = view?.findViewById<ConstraintLayout>(R.id.previewContainer)
        if (imagePreview != null && previewContainer != null) {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            imagePreview.setImageBitmap(bitmap)
            previewContainer.background = BitmapDrawable(resources, bitmap)
        }
    }
}