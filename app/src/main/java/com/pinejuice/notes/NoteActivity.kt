package com.pinejuice.notes

import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import kotlinx.android.synthetic.main.action_bar_note_custom.*
import kotlinx.android.synthetic.main.activity_note.*
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import android.provider.MediaStore
import java.lang.Exception
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.*
import kotlinx.android.synthetic.main.toolbar.*

class NoteActivity : SlideActivity() {

    private val scrollYKey = "scrollY"
    private val editStateKey = "enableEdit"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    private val gestureListener = GestureListener()
    private val ellipsis = (0x2026).toChar()
    private val truncateLength = 30

    private var editEnabled = true
    private var menu: Menu? = null
    private var file: File? = null
    private lateinit var gestureDetector: GestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayShowCustomEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        layoutInflater.inflate(R.layout.action_bar_note_custom, toolbar)
        if (savedInstanceState == null) {
            if (intent.data != null) {
                parseIntentUri(intent.data)
                enableEdit(false)
            }
            scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
        } else {
            scrollView.scrollTo(scrollView.scrollX, savedInstanceState.getInt(scrollYKey))
            enableEdit(savedInstanceState.getBoolean(editStateKey))
        }
        gestureDetector = GestureDetector(this, gestureListener)
        editNote.setOnTouchListener(gestureListener)
        noteTitle.setOnTouchListener(gestureListener)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putInt(scrollYKey, scrollView.scrollY)
        outState?.putBoolean(editStateKey, editEnabled)
    }

    private fun parseIntentUri(data: Uri) {
        var input: InputStream? = null
        var filePath: String? = null
        when (data.scheme) {
            "file" -> {
                filePath = data.path
                input = try {
                    File(filePath).inputStream()
                } catch (ex: FileNotFoundException) {
                    null
                }
            }
            "content" -> {
                filePath = getRealPathFromURI(data)
                input = contentResolver.openInputStream(data)
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
            noteTitle.setText(file?.nameWithoutExtension)
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
        toggleEditBtn(editEnabled)
        return true
    }

    private fun enableEdit(enable: Boolean) {
        editEnabled = enable
        noteTitle.isFocusable = enable
        noteTitle.isFocusableInTouchMode = enable
        noteTitle.isCursorVisible = enable
        editNote.isFocusable = enable
        editNote.isFocusableInTouchMode = enable
        editNote.isCursorVisible = enable
    }

    private fun toggleEditBtn(enable: Boolean) {
        val save = menu?.findItem(R.id.action_save)
        if (enable) {
            save?.setIcon(R.drawable.ic_ok)
            save?.setTitle(R.string.menu_save)
        } else {
            save?.setIcon(R.drawable.ic_edit)
            save?.setTitle(R.string.menu_edit)
        }
    }

    private fun toggleCaps() {
        val input = editNote.inputType.and(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
        val caps = menu?.findItem(R.id.action_caps)
        if (input == 0) {
            caps?.setIcon(R.drawable.ic_caps)
            caps?.setTitle(R.string.menu_caps_enabled)
        } else {
            caps?.setIcon(R.drawable.ic_lower)
            caps?.setTitle(R.string.menu_caps_disabled)
        }
        editNote.inputType = editNote.inputType.xor(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {

        R.id.action_save -> {
            if (editEnabled) {
                save()
                finish()
            } else {
                enableEdit(true)
                toggleEditBtn(true)
                editNote.requestFocus()
                editNote.setSelection(editNote.text.length)
            }
            true
        }

        R.id.action_caps -> {
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
        var title: String = Global.makeValidTitle(noteTitle.text)
        if (title.isBlank()) {
            val lines = editNote.text.split("\n")
            val firstLine = lines.firstOrNull() ?: ""
            title = Global.makeValidTitle(firstLine.take(truncateLength))
            if (lines.size > 1 || firstLine.length > truncateLength) {
                title = title.plus(Character.toString(ellipsis))
            }
        }
        if (title.isBlank()) {
            title = dateFormat.format(Date())
        }
        if (title != file?.nameWithoutExtension) {
            title = Global.iterateTitle(directory, title)
        }
        return "$title.txt"
    }

    private fun createFile(): File {
        val dir = ApplicationContext.appFiles
        val f = File(dir.absolutePath, generateTitle(dir))
        f.createNewFile()
        val creationDate = Date()
        ApplicationContext.sharedPref.edit().putLong(f.name, creationDate.time).apply()
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
                val creationDate = ApplicationContext.sharedPref.getLong(currentName, -1)
                if (creationDate != -1L) {
                    ApplicationContext.sharedPref.edit().remove(currentName).putLong(newName, creationDate).apply()
                }
            }
        }
        val outWriter = OutputStreamWriter(file!!.outputStream())
        outWriter.use {
            it.append(editNote.text)
        }
        Toast.makeText(this, R.string.toast_saved, Toast.LENGTH_SHORT).show()
    }

    private inner class GestureListener : SimpleOnGestureListener(), View.OnTouchListener {

        private var view: View? = null

        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            v?.performClick()
            view = v
            return gestureDetector.onTouchEvent(event)
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            if (!editEnabled) {
                enableEdit(true)
                toggleEditBtn(true)
                view?.requestFocus()
                return true
            }
            return false
        }
    }
}
