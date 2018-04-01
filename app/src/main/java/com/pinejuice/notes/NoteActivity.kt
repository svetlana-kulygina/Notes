package com.pinejuice.notes

import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import kotlinx.android.synthetic.main.action_bar_note_custom.*
import kotlinx.android.synthetic.main.activity_note.*
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import android.provider.MediaStore
import android.support.v4.content.ContextCompat
import java.lang.Exception
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.*
import android.widget.TextView
import kotlinx.android.synthetic.main.toolbar.*
import java.lang.ref.WeakReference

class NoteActivity : SlideActivity() {

    private val scrollYKey = "scrollY"
    private val editStateKey = "enableEdit"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    private val gestureListener = GestureListener()
    private val truncateLength = 60

    private var editEnabled = true
    private var menu: Menu? = null
    private var file: File? = null
    private lateinit var gestureDetector: GestureDetector
    private var savedOffset = 0F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayShowCustomEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        layoutInflater.inflate(R.layout.action_bar_note_custom, toolbar)
        scrollView.addOnLayoutChangeListener(scrollLayoutListener)
        scrollView.viewTreeObserver.addOnScrollChangedListener {
            setFabBehavior(scrollView.scrollY + scrollView.height != editNote.bottom)
        }

        if (savedInstanceState == null) {
            if (intent.data != null) {
                enableEdit(false)
                parseIntentUri(intent.data)
            }
        } else {
            savedOffset = savedInstanceState.getFloat(scrollYKey)
            enableEdit(savedInstanceState.getBoolean(editStateKey))
        }
        gestureDetector = GestureDetector(this, gestureListener)
        editNote.setOnTouchListener(gestureListener)
        noteTitle.setOnTouchListener(gestureListener)
    }

    private val scrollLayoutListener = View.OnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
        if (!editEnabled && editNote.height > scrollView.height) {
            toggleFab(true)
            var fabDown = true

            if (savedOffset != 0F) {
                val newScroll = (savedOffset * (editNote.height)).toInt()
                scrollView.scrollTo(scrollView.scrollX, newScroll)
                fabDown = scrollView.scrollY + scrollView.height < editNote.height
                savedOffset = 0F
            }
            setFabBehavior(fabDown)
        }
    }

    private val fabForBottomListener = View.OnClickListener {
        scrollView.smoothScrollTo(scrollView.scrollX, editNote.bottom)
    }

    private val fabForTopListener = View.OnClickListener {
        scrollView.smoothScrollTo(scrollView.scrollX, editNote.top)
    }

    private fun setFabBehavior(down: Boolean) {
        if (down) {
            fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_bottom_arrow))
            fab.setOnClickListener(fabForBottomListener)
        } else {
            fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_top_arrow))
            fab.setOnClickListener(fabForTopListener)
        }
    }

    class Loader(private val rootRef: WeakReference<View>) : AsyncTask<InputStream, Void, String>() {

        override fun doInBackground(vararg params: InputStream?): String? {
            val input = params[0]
            if (input != null) {
                BufferedInputStream(input).use { bis ->
                    ByteArrayOutputStream().use {
                        var result = bis.read()
                        while (result != -1) {
                            it.write(result)
                            result = bis.read()
                        }
                        return it.toString()
                    }
                }
            }
            return null
        }

        override fun onPreExecute() {
            super.onPreExecute()
            val root = rootRef.get()
            root?.findViewById<View>(R.id.loadingHint)?.visibility = View.VISIBLE
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            val root = rootRef.get()
            val textView = root?.findViewById<TextView>(R.id.editNote)
            root?.findViewById<View>(R.id.loadingHint)?.visibility = View.INVISIBLE
            textView?.text = result
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putFloat(scrollYKey, scrollView.scrollY.toFloat() / editNote.height)
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
        val contentView = this.findViewById<View>(android.R.id.content)
        Loader(WeakReference(contentView)).execute(input)
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

    private fun toggleFab(enable: Boolean) {
        if (enable) {
            fab.show()
        } else {
            fab.hide()
        }
    }

    private fun enableEdit(enable: Boolean) {
        editEnabled = enable
        if (enable) {
            toggleFab(false)
        }
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

    private fun generateTitle(): String {
        var title: String = Global.makeValidTitle(noteTitle.text)
        if (title.isBlank()) {
            val lines = editNote.text.split(Regex("[\n?]"))
            val firstLine = lines.firstOrNull() ?: ""
            var truncatedLine = firstLine.take(truncateLength)
            val spacePos = truncatedLine.lastIndexOf(" ")
            if (firstLine.length > truncateLength && spacePos > -1) {
                truncatedLine = truncatedLine.substring(0, spacePos)
            }
            title = Global.makeValidTitle(truncatedLine)
        }
        if (title.isBlank()) {
            val creationDate = ApplicationContext.sharedPref.getLong(file?.name, Date().time)
            title = dateFormat.format(Date(creationDate))
        }
        return title
    }

    private fun createFile(): File {
        val dir = ApplicationContext.appFiles
        val f = File(dir.absolutePath, Global.generateTitle(dir, generateTitle()))
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
            val newName = Global.generateTitle(f.parentFile, generateTitle(), f)
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
