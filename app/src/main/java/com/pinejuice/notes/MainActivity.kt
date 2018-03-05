package com.pinejuice.notes

import android.content.Intent
import android.os.Bundle
import android.view.*


class MainActivity : SlideActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
