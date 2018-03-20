package com.pinejuice.notes

import android.app.Application
import kotlinx.android.synthetic.main.action_bar_note_custom.*
import kotlinx.android.synthetic.main.activity_note.*
import java.io.File
import java.util.*

class ApplicationContext : Application() {

    companion object {

        private val filenamePattern = "[?|/\"*<>\n]+"

        lateinit var appFiles: File

        fun makeValidTitle(title: CharSequence): String {
            return title.replace(Regex(filenamePattern), "").trim()
        }

        fun iterateTitle(directory: File, title: String): String {
            val children = directory.list()
            if (children != null) {
                var iterTitle = title
                var i = 1
                while (children.contains("$iterTitle.txt")) {
                    iterTitle = "$title ($i)"
                    i++
                }
                return iterTitle
            }
            return title
        }
    }

    override fun onCreate() {
        super.onCreate()
        appFiles = getExternalFilesDir(null)
    }
}