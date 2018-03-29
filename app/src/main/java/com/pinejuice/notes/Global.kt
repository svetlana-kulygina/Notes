package com.pinejuice.notes

import java.io.File

object Global {

    private val disallowedChars = "[?|/\"*<>\n]+"
    private val defaultExtension = "txt"

    fun makeValidTitle(title: CharSequence): String {
        return title.replace(Regex(disallowedChars), " ").trim()
    }

    private fun iterateTitle(directory: File, title: CharSequence, extension: CharSequence = defaultExtension): String {
        val children = directory.list()
        if (children != null) {
            var iterTitle = title
            var i = 1
            while (children.contains(addExtension(iterTitle, extension))) {
                iterTitle = "$title ($i)"
                i++
            }
            return addExtension(iterTitle, extension)
        }
        return addExtension(title, extension)
    }

    /**
     * Generates iterated file name. If file exist, applies existing extension, otherwise applies
     * default.
     *
     * @param directory Parent directory.
     * @param newTitle New file title without extension.
     * @param file Current file for which title is generated. {@code null} if not exist.
     * @return File name with extension.
     */
    fun generateTitle(directory: File, newTitle: CharSequence, file: File? = null): String {
        val extension = getExtension(file)
        return iterateTitle(directory, newTitle, extension, file)
    }

    /**
     * Generates iterated file title.
     *
     * @param directory Parent directory.
     * @param newTitle New title with extension. If ends with dot, last dot is ignored and extension
     * is empty.
     * @param file Current file for which title is generated. <code>null</code> if not exist.
     * @return File name with extension.
     */
    fun generateTitleExtended(directory: File, newTitle: CharSequence, file: File? = null): String {
        val pair = splitByExtension(newTitle)
        return iterateTitle(directory, pair.first, pair.second, file)
    }

    private fun iterateTitle(directory: File, title: CharSequence, extension: CharSequence,
                             file: File? = null): String {
        if (file?.name == addExtension(title, extension)) {
            return file.name
        }
        return iterateTitle(directory, title, extension)
    }

    private fun addExtension(title: CharSequence, extension: CharSequence): String {
        if (extension.isNotBlank()) {
            return "$title.$extension"
        }
        return title.toString()
    }

    /**
     * @return extension of given file or default extension if file does not exist. If file has no
     * extension, method returns empty <code>CharSequence</code>.
     */
    private fun getExtension(file: File?): CharSequence {
        return file?.extension ?: defaultExtension
    }

    /**
     * Splits given <code>CharSequence</code> to name and extension by last dot position.
     * @return <code>Pair</code> with name part and extension part. If dot not found, extension will
     * be empty <code>CharSequence</code>.
     *
     * Examples:
     * "usualName.txt" -> "usualName" and "txt".
     * "separated.Name.txt" -> "separated.Name" and "txt".
     * "dotAtTheEnd." -> "dotAtTheEnd" and "".
     * ".dotAtTheStart" -> "" and "dotAtTheStart".
     * "noExtension" -> "noExtension" and "".
     */
    private fun splitByExtension(str: CharSequence): Pair<CharSequence, CharSequence> {
        val dotPos = str.lastIndexOf(".")
        var name = str
        var ext = ""
        if (dotPos > -1) {
            name = str.substring(0, dotPos)
            ext = str.substring(dotPos + 1)
        }
        return Pair(name, ext)
    }
}