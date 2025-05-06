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

        init {
            view.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    selectedItem = images[position].second
                    onItemSelected(selectedItem!!)
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
            holder.selectionIndicator.visibility = if (id == selectedItem) View.VISIBLE else View.INVISIBLE
        } catch (e: Exception) {
            Log.e("ImageAdapter", "Error loading image: $drawableRes", e)
            holder.imageView.setImageResource(
                if (isBanner) R.drawable.banner3 else R.drawable.capybara
            )
        }
    }

    override fun getItemCount() = images.size

    fun setSelectedItem(id: String) {
        selectedItem = id
        notifyDataSetChanged()
    }
}