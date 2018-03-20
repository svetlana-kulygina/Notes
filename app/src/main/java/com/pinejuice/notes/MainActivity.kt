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

class MainActivity : SlideActivity() {

    private val attributeName = "name"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        fillList()
    }

    private fun fillList() {
        val allNotes = ApplicationContext.appFiles.list()
        if (allNotes == null || allNotes.isEmpty()) {
            emptyListHint.visibility = View.VISIBLE
            return
        }
        val data = ArrayList<Map<String, Any>>()
        for (title in allNotes) {
            val m = HashMap<String, Any>()
            m.put(attributeName, title.removeSuffix(".txt"))
            data.add(m)
        }
        val from = arrayOf(attributeName)
        val to = intArrayOf(R.id.text1)
        listView.adapter = SimpleAdapter(this, data, R.layout.list_item, from, to)
        listView.onItemClickListener = onClickListener
    }

    private val onClickListener = AdapterView.OnItemClickListener { _, view, _, _ ->
        val name = view.findViewById<TextView>(R.id.text1).text
        val file = File(ApplicationContext.appFiles, "$name.txt")
        val intent = Intent(this, NoteActivity::class.java).apply {}
        intent.data = Uri.fromFile(file)
        startActivity(intent)
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
}
