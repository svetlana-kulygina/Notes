package com.pinejuice.notes

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.SimpleAdapter
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import android.view.ContextMenu.ContextMenuInfo
import android.view.ContextMenu
import android.app.AlertDialog
import android.support.constraint.ConstraintLayout
import android.widget.EditText
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : SlideActivity() {

    private val attributeName = "name"
    private val attributeDate = "created at"
    private val dateFormat = SimpleDateFormat.getDateInstance(DateFormat.SHORT)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        registerForContextMenu(listView)
    }

    override fun onResume() {
        super.onResume()
        fillList()
    }

    private fun getCreationDate(file: File): Long {
        return ApplicationContext.sharedPref.getLong(file.name, file.lastModified())
    }

    private fun fillList() {
        var allNotes = ApplicationContext.appFiles.listFiles()
        if (allNotes == null || allNotes.isEmpty()) {
            emptyListHint.visibility = View.VISIBLE
            return
        }
        val data = ArrayList<Map<String, Any>>()
        allNotes = allNotes.sortedWith(compareByDescending { getCreationDate(it) }).toTypedArray()
        for (file in allNotes) {
            val m = HashMap<String, Any>()
            m.put(attributeName, file.nameWithoutExtension)
            val creationDate = ApplicationContext.sharedPref.getLong(file.name, file.lastModified())
            m.put(attributeDate, dateFormat.format(Date(creationDate)))
            data.add(m)
        }
        val from = arrayOf(attributeName, attributeDate)
        val to = intArrayOf(R.id.text1, R.id.text2)
        listView.adapter = SimpleAdapter(this, data, R.layout.list_item, from, to)
        listView.onItemClickListener = onClickListener
    }

    private val onClickListener = AdapterView.OnItemClickListener { _, view, _, _ ->
        val name = view.findViewById<TextView>(R.id.text1).text
        val file = getFile(name)
        val intent = Intent(this, NoteActivity::class.java).apply {}
        intent.data = Uri.fromFile(file)
        startActivity(intent)
    }

    private fun getFile(name: CharSequence): File {
        return File(ApplicationContext.appFiles, "$name.txt")
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
        val info = item.menuInfo as AdapterView.AdapterContextMenuInfo
        return when (item.itemId) {

            R.id.context_rename -> {
                renameFile((info.targetView.findViewById<TextView>(R.id.text1)).text)
                true
            }

            R.id.context_delete -> {
                delete((info.targetView.findViewById<TextView>(R.id.text1)).text)
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

    private fun renameFile(name: CharSequence) {
        val alert = AlertDialog.Builder(this)
        val title = getString(R.string.dialog_rename).format(name)
        alert.setTitle(title)

        val layout = layoutInflater.inflate(R.layout.rename_dialog_input, ConstraintLayout(this))
        val input = layout.findViewById<EditText>(R.id.renameInput)
        input.setText(name)
        input.setSelection(name.length, name.length)
        alert.setView(layout)

        alert.setPositiveButton(android.R.string.ok, { _, _ ->
            val file = getFile(name)
            val creationDate = ApplicationContext.sharedPref.getLong(name.toString(), file.lastModified())
            var newTitle = Global.makeValidTitle(input.editableText.toString())
            if (!newTitle.isBlank() && name != newTitle) {
                newTitle = Global.iterateTitle(ApplicationContext.appFiles, newTitle)
            }
            ApplicationContext.sharedPref.edit().remove(name.toString()).putLong(newTitle, creationDate).apply()
            file.renameTo(File(ApplicationContext.appFiles, "$newTitle.txt"))
            fillList()
        })

        alert.setNegativeButton(android.R.string.cancel, { dialog, _ -> dialog.cancel() })
        val alertDialog = alert.create()
        alertDialog.show()
    }

    private fun delete(name: CharSequence) {
        val alert = AlertDialog.Builder(this)
        val title = getString(R.string.dialog_delete).format(name)
        alert.setTitle(title)

        alert.setPositiveButton(android.R.string.ok, { _, _ ->
            ApplicationContext.sharedPref.edit().remove(name.toString()).apply()
            getFile(name).delete()
            fillList()
        })

        alert.setNegativeButton(android.R.string.cancel, { dialog, _ -> dialog.cancel() })
        val alertDialog = alert.create()
        alertDialog.show()
    }
}
