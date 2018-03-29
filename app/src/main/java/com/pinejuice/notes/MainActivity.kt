package com.pinejuice.notes

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.SimpleAdapter
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import android.view.ContextMenu.ContextMenuInfo
import android.view.ContextMenu
import android.app.AlertDialog
import android.os.Parcelable
import android.support.constraint.ConstraintLayout
import android.util.Log
import android.widget.EditText
import kotlinx.android.synthetic.main.toolbar.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class MainActivity : SlideActivity() {

    private val listStateKey = "listState"
    private val attributeName = "name"
    private val attributeDate = "created at"
    private val attributeFile = "file"
    private val dateFormat = SimpleDateFormat.getDateInstance(DateFormat.SHORT)

    private var dataSet = mutableListOf<HashMap<String, Any>>()
    private lateinit var adapter: SimpleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        adapter = initAdapter()
        listView.adapter = adapter
        listView.onItemClickListener = onClickListener
        registerForContextMenu(listView)
        if (savedInstanceState != null) {
            val state = savedInstanceState.getParcelable<Parcelable>(listStateKey)
            listView.onRestoreInstanceState(state)
        }
    }

    override fun onResume() {
        super.onResume()
        createDataSet()
        adapter.notifyDataSetChanged()
    }

    // DEBUG SECTION
    private fun appPrefDebug() {
        val files = ApplicationContext.appFiles.listFiles()
        Log.e("test", "${ApplicationContext.sharedPref.all.size} , ${files.size}")
        for (i in ApplicationContext.sharedPref.all) {
            Log.e("test", i.key)
            if (files.find { it.name == i.key } == null) {
                Log.e("test", "im here")
                ApplicationContext.sharedPref.edit().remove(i.key).apply()
            }
        }
    }

    private fun fillAppPref() {
        val list = arrayListOf(Pair("test", "test"))
        val files = ApplicationContext.appFiles.listFiles()
        for (i in list) {
            val f = files.find { it.name.startsWith(i.first) }
            if (f == null) {
                Log.e("test", "cannot find ${i.first}")
            } else {
                ApplicationContext.sharedPref.edit().putLong(f.name, dateFormat.parse(i.second).time).apply()
            }
        }
    }
    // DEBUG SECTION END

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        val state = listView.onSaveInstanceState()
        outState?.putParcelable(listStateKey, state)
    }

    private fun getCreationDate(file: File): Long {
        return ApplicationContext.sharedPref.getLong(file.name, file.lastModified())
    }

    private fun createDataSet() {
        var allNotes = ApplicationContext.appFiles.listFiles()
        if (allNotes != null && !allNotes.isEmpty()) {
            dataSet.clear()
            allNotes = allNotes.sortedWith(compareByDescending { getCreationDate(it) }).toTypedArray()
            for (file in allNotes) {
                val m = HashMap<String, Any>()
                m.put(attributeName, getDisplayName(file))
                m.put(attributeFile, file)
                val creationDate = ApplicationContext.sharedPref.getLong(file.name, file.lastModified())
                m.put(attributeDate, dateFormat.format(Date(creationDate)))
                dataSet.add(m)
            }
        }
    }

    private fun getDisplayName(file: File): String {
        if (file.nameWithoutExtension.isNotBlank()) {
            return file.nameWithoutExtension
        }
        return ".".plus(file.extension)
    }

    private fun initAdapter(): SimpleAdapter {
        val from = arrayOf(attributeName, attributeDate)
        val to = intArrayOf(R.id.text1, R.id.text2)
        return SimpleAdapter(this, dataSet, R.layout.list_item, from, to)
    }

    private val onClickListener = AdapterView.OnItemClickListener { _, _, pos, _ ->
        val element = dataSet[pos]
        val file = getFile(element)
        val intent = Intent(this, NoteActivity::class.java).apply {}
        intent.data = Uri.fromFile(file)
        startActivity(intent)
    }

    private fun getFile(dataItem: Map<String, Any>): File {
        return dataItem[attributeFile] as File
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
            val intent = Intent(this, SettingsActivity::class.java).apply {}
            startActivity(intent)
            true
        }

        R.id.action_new -> {
            val intent = Intent(this, NoteActivity::class.java).apply {}
            startActivity(intent)
            true
        }

        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.action_bar_main, menu)
        return true
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val element = dataSet[(item.menuInfo as AdapterView.AdapterContextMenuInfo).position]
        return when (item.itemId) {

            R.id.context_rename -> {
                renameDialogShow(element)
                true
            }

            R.id.context_delete -> {
                deleteDialogShow(element)
                true
            }

            else -> {
                super.onOptionsItemSelected(item)
                true
            }
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater = menuInflater
        inflater.inflate(R.menu.context_menu, menu)
    }

    private fun renameDialogShow(dataItem: HashMap<String, Any>) {
        val alert = AlertDialog.Builder(this)
        val file = getFile(dataItem)
        alert.setTitle(getString(R.string.dialog_rename).format(file.name))

        val layout = layoutInflater.inflate(R.layout.rename_dialog_input, ConstraintLayout(this))
        val input = layout.findViewById<EditText>(R.id.renameInput)
        input.setText(file.name)
        input.setSelection(file.nameWithoutExtension.length, file.nameWithoutExtension.length)
        alert.setView(layout)

        alert.setPositiveButton(android.R.string.ok, { _, _ ->
            val f = renameFile(file, Global.makeValidTitle(input.editableText))
            dataItem.put(attributeName, getDisplayName(f))
            dataItem.put(attributeFile, f)
            adapter.notifyDataSetChanged()
        })

        alert.setNegativeButton(android.R.string.cancel, { dialog, _ -> dialog.cancel() })
        val alertDialog = alert.create()
        alertDialog.show()
    }

    private fun renameFile(file: File, newName: String): File {
        val iterTitle = Global.generateTitleExtended(file.parentFile, newName, file)
        if (iterTitle.isNotBlank() || file.name != iterTitle) {
            val creationDate = ApplicationContext.sharedPref.getLong(file.name, file.lastModified())
            ApplicationContext.sharedPref.edit().remove(file.name).putLong(iterTitle, creationDate).apply()
            val newFile = File(ApplicationContext.appFiles, iterTitle)
            if (file.renameTo(newFile)) {
                return newFile
            }
        }
        return file
    }

    private fun deleteDialogShow(dataItem: HashMap<String, Any>) {
        val file = getFile(dataItem)
        val alert = AlertDialog.Builder(this)
        val title = getString(R.string.dialog_delete).format(file.name)
        alert.setTitle(title)

        alert.setPositiveButton(android.R.string.ok, { _, _ ->
            ApplicationContext.sharedPref.edit().remove(file.name).apply()
            if (file.delete()) {
                dataSet.removeAll { getFile(it) == file }
            }
            adapter.notifyDataSetChanged()
        })

        alert.setNegativeButton(android.R.string.cancel, { dialog, _ -> dialog.cancel() })
        val alertDialog = alert.create()
        alertDialog.show()
    }
}
