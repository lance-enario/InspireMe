package com.application.inspireme

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class InspireMeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}