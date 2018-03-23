package com.pinejuice.notes

import android.app.Application
import android.content.Context
import java.io.File
import android.content.SharedPreferences
import android.util.Log

class ApplicationContext : Application() {

    companion object {

        lateinit private var preferences_key: String
        lateinit var appFiles: File
        lateinit var sharedPref: SharedPreferences
    }

    override fun onCreate() {
        super.onCreate()
        preferences_key = "$packageName.preferences"
        Log.e("test", preferences_key)
        appFiles = getExternalFilesDir(null)
        sharedPref = this.getSharedPreferences(preferences_key, Context.MODE_PRIVATE)
    }
}