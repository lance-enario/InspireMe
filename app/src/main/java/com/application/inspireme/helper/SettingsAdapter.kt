package com.application.inspireme.helper

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.application.inspireme.R

public class SettingsAdapter(private val context: Context, private val settingsList: List<SettingItem>) : BaseAdapter() {

    override fun getCount(): Int {
        return settingsList.size
    }

    override fun getItem(position: Int): Any {
        return settingsList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item_setting, parent, false)
        }

        val settingItem = settingsList[position]

        val iconImageView = view?.findViewById<ImageView>(R.id.icon)
        val textView = view?.findViewById<TextView>(R.id.setting_text)
        val arrowImageView = view?.findViewById<ImageView>(R.id.arrow_icon)

        iconImageView?.setImageResource(settingItem.iconRes)
        textView?.text = settingItem.text

        if (settingItem.arrowRes != null) {
            arrowImageView?.setImageResource(settingItem.arrowRes)
            arrowImageView?.visibility = View.VISIBLE
        } else {
            arrowImageView?.visibility = View.INVISIBLE
        }

        return view!!
    }
}
