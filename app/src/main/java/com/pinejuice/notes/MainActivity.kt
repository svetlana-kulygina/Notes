package com.pinejuice.notes

import android.content.Intent
import android.os.Bundle
import android.support.v4.widget.SimpleCursorAdapter
import android.view.*
import android.widget.SimpleAdapter
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : SlideActivity() {

    private val ATTRIBUTE_NAME = "name"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fillList()
    }

    private fun fillList() {
        val allNotes = getExternalFilesDir(null).list()
        if (allNotes == null || allNotes.isEmpty()) {
            emptyListHint.visibility = View.VISIBLE
            return
        }
        val data = ArrayList<Map<String, Any>>()
        for (title in allNotes) {
            val m = HashMap<String, Any>()
            m.put(ATTRIBUTE_NAME, title)
            data.add(m)
        }
        val from = arrayOf(ATTRIBUTE_NAME)
        val to = intArrayOf(R.id.text1)
        listView.adapter = SimpleAdapter(this, data, R.layout.list_item, from, to)
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
