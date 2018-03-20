package com.pinejuice.notes

import android.app.Application
import java.io.File

class ApplicationContext : Application() {

    companion object {
        lateinit var appFiles: File
    }

    override fun onCreate() {
        super.onCreate()
        appFiles = getExternalFilesDir(null)
    }
}