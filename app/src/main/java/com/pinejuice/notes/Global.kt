package com.pinejuice.notes

import java.io.File

object Global {

    private val filenamePattern = "[?|/\"*<>\n]+"

    fun makeValidTitle(title: CharSequence): String {
        return title.replace(Regex(filenamePattern), " ").trim()
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