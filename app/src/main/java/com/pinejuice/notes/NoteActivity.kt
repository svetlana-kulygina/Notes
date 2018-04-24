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
import android.support.design.widget.AppBarLayout
import java.lang.Exception
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.*
import kotlinx.android.synthetic.main.toolbar.*
import java.lang.ref.WeakReference
import kotlin.concurrent.schedule

class NoteActivity : SlideActivity(), View.OnLayoutChangeListener, InputLoader.LoadingListener {

    private val scrollYKey = "scrollY"
    private val editStateKey = "enableEdit"
    private val navigationStateKey = "enableNavigation"
    private val capsEnabledKey = "capsEnabled"
    private val fileKey = "file"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    private val gestureListener = GestureListener()
    private val truncateLength = 60
    private val timer = Timer("showNavigation", false)
    private val scheduleTime = 5000L

    private var task: TimerTask? = null
    private var editEnabled = true
    private var navigationEnabled = true
    private var capsEnabled = true
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
        scrollView.addOnLayoutChangeListener(this)
        if (savedInstanceState == null) {
            if (intent.data != null) {
                enableEdit(false)
                parseIntentUri(intent.data)
            }
        } else {
            val path = savedInstanceState.getString(fileKey)
            file = if (path != null) File(path) else null
            savedOffset = savedInstanceState.getFloat(scrollYKey)
            enableEdit(savedInstanceState.getBoolean(editStateKey))
            capsEnabled = savedInstanceState.getBoolean(capsEnabledKey)
        }
        gestureDetector = GestureDetector(this, gestureListener)
        editNote.setOnTouchListener(gestureListener)
        noteTitle.setOnTouchListener(gestureListener)
    }

    override fun onLayoutChange(v: View?, left: Int, top: Int, right: Int, bottom: Int,
                                oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
        toolbarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (-verticalOffset == appBarLayout?.height) {
                navigationEnabled = false
                task?.cancel()
            } else if (verticalOffset == 0) {
                navigationEnabled = true
                if (!editEnabled) {
                    setTimer()
                }
            }
        }
        if (!editEnabled) {

            if (savedOffset != 0F) {
                val newScroll = (savedOffset * (editNote.height)).toInt()
                scrollView.scrollTo(scrollView.scrollX, newScroll)
                savedOffset = 0F
            }
            showNavigation(navigationEnabled)
        } else {
            showEditModeNavigation()
        }
    }

    private fun setTimer() {
        task?.cancel()
        task = timer.schedule(scheduleTime) {
            runOnUiThread( { showNavigation(false) } )
            task?.cancel()
        }
    }

    private fun showNavigation(show: Boolean) {
        task?.cancel()
        toolbarLayout.setExpanded(show, true)
    }

    private fun showEditModeNavigation() {
        toolbarLayout.setExpanded(true, true)
        val params = toolbar.layoutParams as AppBarLayout.LayoutParams
        params.scrollFlags = 0
        paginationView.visibility = View.GONE
        task?.cancel()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        navigationEnabled = savedInstanceState?.getBoolean(navigationStateKey) ?: navigationEnabled
        if (!navigationEnabled) {
            toolbarLayout.setExpanded(false, false)
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        task?.cancel()
        outState?.putFloat(scrollYKey, scrollView.scrollY.toFloat() / editNote.height)
        outState?.putBoolean(editStateKey, editEnabled)
        outState?.putBoolean(navigationStateKey, navigationEnabled)
        outState?.putBoolean(capsEnabledKey, capsEnabled)
        outState?.putString(fileKey, file?.absolutePath)
    }

    private fun parseIntentUri(data: Uri) {
        val input = contentResolver.openInputStream(data)
        var filePath: String? = null
        when (data.scheme) {
            "file" -> {
                filePath = data.path
            }
            "content" -> {
                filePath = getRealPathFromURI(data)
            }
        }
        InputLoader(WeakReference(paginationView), this).execute(input)
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
        toggleCaps()
        return true
    }

    private fun enableEdit(enable: Boolean, showEditModeNavigation: Boolean = true) {
        editEnabled = enable
        if (enable && showEditModeNavigation) {
            showEditModeNavigation()
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
        val caps = menu?.findItem(R.id.action_caps)
        if (capsEnabled) {
            caps?.setIcon(R.drawable.ic_caps)
            caps?.setTitle(R.string.menu_caps_enabled)
            editNote.inputType = editNote.inputType.or(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
        } else {
            caps?.setIcon(R.drawable.ic_lower)
            caps?.setTitle(R.string.menu_caps_disabled)
            editNote.inputType = editNote.inputType.and(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES.inv())
        }
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
            capsEnabled = !capsEnabled
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

    override fun onLoadingStart() {
        loadingHint.visibility = View.VISIBLE
    }

    override fun onLoadingEnd(result: String) {
        editNote.setText(result)
        loadingHint.visibility = View.INVISIBLE
    }

    private inner class GestureListener : SimpleOnGestureListener(), View.OnTouchListener {

        private var view: View? = null

        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            v?.performClick()
            view = v
            if (!editEnabled && event?.action == MotionEvent.ACTION_MOVE && navigationEnabled) {
                setTimer()
            }
            return gestureDetector.onTouchEvent(event)
        }

        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
            if (!editEnabled) {
                showNavigation(!navigationEnabled)
            }
            return super.onSingleTapConfirmed(e)
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            if (!editEnabled) {
                enableEdit(true, false)
                toggleEditBtn(true)
                view?.requestFocus()
                return true
            }
            return false
        }
    }
}
