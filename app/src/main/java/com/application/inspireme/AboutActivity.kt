package com.application.inspireme

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast

class AboutActivity : Activity() {
    val creatorList = listOf("Lance Joseph Lorenz S. Enario", "Raimar Shaun C. Epan")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)


        val arrayAdapter = ArrayAdapter(this,android.R.layout.simple_list_item_1, creatorList)
        val listview = findViewById<ListView>(R.id.listview1)

        listview.adapter = arrayAdapter
        listview.setOnItemClickListener {_, _, position, _ ->
            showCreatorDialog(position)
        }



        val backButton = findViewById<ImageView>(R.id.back_icon_about)
        backButton.setOnClickListener {
            val intent = Intent(this, SettingsPageActivity::class.java)
            startActivity(intent)
        }
    }
    fun showCreatorDialog(position : Int) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.about_creator)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(true)

        val pfp = dialog.findViewById<ImageView>(R.id.ProfilePic)
        val name = dialog.findViewById<TextView>(R.id.Name)
        val about = dialog.findViewById<TextView>(R.id.About)

        val img = listOf(R.drawable.volcano_profile,R.drawable.cat_icon)
        val abouts = "Student"

        if(position in creatorList.indices) {
            pfp.setImageResource(img[position])
            name.text = creatorList[position]
            about.text = abouts
        }

        dialog.show()
    }
}