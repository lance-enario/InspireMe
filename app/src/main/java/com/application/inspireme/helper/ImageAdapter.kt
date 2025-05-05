package com.application.inspireme

import android.content.res.Resources
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class ImageAdapter(
    private val images: List<Pair<Int, String>>,
    private val isBanner: Boolean,
    private val onItemSelected: (String) -> Unit
) : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    private var selectedItem: String? = null

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageItem)
        val selectionIndicator: View = view.findViewById(R.id.selectionIndicator)
        private val clickArea: View = view.findViewById(R.id.clickArea)

        init {
            // Make sure we're clicking on the entire item, not just parts
            clickArea.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val (_, id) = images[position]
                    selectedItem = id
                    onItemSelected(id)
                    notifyDataSetChanged()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutRes = if (isBanner) R.layout.item_banner_picker else R.layout.item_profile_picker
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (drawableRes, id) = images[position]
        try {
            holder.imageView.setImageResource(drawableRes)
            holder.imageView.scaleType = ImageView.ScaleType.CENTER_CROP

            // Ensure the image doesn't overlap the border
            holder.imageView.setPadding(4.dpToPx(), 4.dpToPx(), 4.dpToPx(), 4.dpToPx())

            holder.selectionIndicator.visibility =
                if (id == selectedItem) View.VISIBLE else View.INVISIBLE
        } catch (e: Resources.NotFoundException) {
            Log.e("ImageAdapter", "Resource not found: $drawableRes")
            // Fallback with white background
            holder.imageView.setImageResource(R.drawable.profile)
            holder.imageView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, android.R.color.white))
        }
    }

    // Add this extension function to your adapter
    private fun Int.dpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

    override fun getItemCount() = images.size

    fun setSelectedItem(id: String) {
        selectedItem = id
        notifyDataSetChanged()
    }
}