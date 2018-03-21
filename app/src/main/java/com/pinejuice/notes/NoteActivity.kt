package com.pinejuice.notes

import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import kotlinx.android.synthetic.main.action_bar_note_custom.*
import kotlinx.android.synthetic.main.activity_note.*
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import android.provider.MediaStore
import android.view.GestureDetector
import android.view.MotionEvent
import java.lang.Exception
import android.view.GestureDetector.SimpleOnGestureListener

class NoteActivity : SlideActivity() {

    private var editEnabled = true
    private var menu: Menu? = null
    private var file: File? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    private lateinit var gestureDetector: GestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)
        val actionBar = supportActionBar
        actionBar?.setDisplayShowTitleEnabled(false)
        actionBar?.setDisplayShowCustomEnabled(true)
        actionBar?.setCustomView(R.layout.action_bar_note_custom)
        if (savedInstanceState == null) {
            parseIntentUri()
        }
        gestureDetector = GestureDetector(this, GestureListener())
        editNote.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
        }
    }

    private fun parseIntentUri() {
        if (intent.data != null) {
            editEnabled = false
            var input: InputStream? = null
            var filePath: String? = null
            when (intent.data.scheme) {
                "file" -> {
                    filePath = intent.data.path
                    input = try {
                        File(filePath).inputStream() } catch (ex: FileNotFoundException) { null }
                }
                "content" -> {
                    filePath = getRealPathFromURI(intent.data)
                    input = contentResolver.openInputStream(intent.data)
                }
            }
            if (input != null) {
                BufferedInputStream(input).use { bis ->
                    ByteArrayOutputStream().use {
                        var result = bis.read()
                        while (result != -1) {
                            it.write(result)
                            result = bis.read()
                        }
                        editNote.setText(it.toString())
                    }
                }
            }
            if (filePath != null) {
                file = File(filePath)
                val pathSplit = filePath.split("/")
                noteTitle.setText(pathSplit.last().removeSuffix(".txt"))
            }
        }
    }

    private fun getRealPathFromURI(contentUri: Uri): String? {
        try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            contentResolver.query(contentUri, proj, null, null, null).use {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                it.moveToFirst()
                return it.getString(columnIndex)
            }
        } catch (ex: Exception) {
            return null
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.action_bar_note, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        this.menu = menu
        enableEdit(editEnabled)
        return true
    }

    private fun enableEdit(enable: Boolean) {
        editEnabled = enable
        menu?.findItem(R.id.action_save)?.isVisible = enable
        menu?.findItem(R.id.action_edit)?.isVisible = !enable
        noteTitle.isFocusable = enable
        noteTitle.isFocusableInTouchMode = enable
        noteTitle.isCursorVisible = enable
        editNote.isFocusable = enable
        editNote.isFocusableInTouchMode = enable
        editNote.isCursorVisible = enable
    }

    private fun toggleCaps() {
        editNote.inputType = editNote.inputType.xor(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {

        R.id.action_save -> {
            item.isVisible = false
            menu?.findItem(R.id.action_edit)?.isVisible = true
            save()
            finish()
            true
        }

        R.id.action_edit -> {
            enableEdit(true)
            item.isVisible = false
            menu?.findItem(R.id.action_save)?.isVisible = true
            true
        }

        R.id.action_caps -> {
            item.isVisible = false
            menu?.findItem(R.id.action_lower)?.isVisible = true
            toggleCaps()
            true
        }

        R.id.action_lower -> {
            item.isVisible = false
            menu?.findItem(R.id.action_caps)?.isVisible = true
            toggleCaps()
            true
        }

        android.R.id.home -> {
            finish()
            true
        }

        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    private fun generateTitle(directory: File): String {
        var title: String = ApplicationContext.makeValidTitle(noteTitle.text)
        if (title.isBlank()) {
            title = ApplicationContext.makeValidTitle(editNote.text.take(20))
        }
        if (title.isBlank()) {
            title = dateFormat.format(Date())
        }
        if (title != file?.name?.removeSuffix(".txt")) {
            title = ApplicationContext.iterateTitle(directory, title)
        }
        return "$title.txt"
    }

    private fun createFile(): File {
        val dir = ApplicationContext.appFiles
        val f = File(dir.absolutePath, generateTitle(dir))
        f.createNewFile()
        return f
    }

    private fun save() {
        if (file == null) {
            file = createFile()
        } else {
            val f = file!!
            val currentName = f.name
            val newName = generateTitle(f.parentFile)
            if (currentName != newName) {
                file = File(f.parentFile, newName)
                f.renameTo(file)
            }
        }
        val outWriter = OutputStreamWriter(file!!.outputStream())
        outWriter.use {
            it.append(editNote.text)
        }
        Toast.makeText(this, R.string.toast_saved, Toast.LENGTH_SHORT).show()
    }

    private inner class GestureListener : SimpleOnGestureListener() {

        override fun onDoubleTap(e: MotionEvent): Boolean {
            enableEdit(true)
            return true
        }
    }
}
