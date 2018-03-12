package com.pinejuice.notes

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem

class NoteActivity : SlideActivity() {

    private var noteExists = false
    private var menu: Menu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)
        if (savedInstanceState?.get("key") != null) {
            noteExists = true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.action_bar_note, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        this.menu = menu
        val save = menu.findItem(R.id.action_save)
        val edit = menu.findItem(R.id.action_edit)
        if (noteExists) {
            edit?.isVisible = true
        } else {
            save?.isVisible = true
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {

        R.id.action_save -> {
            item.isVisible = false
            menu?.findItem(R.id.action_edit)?.isVisible = true
            true
        }

        R.id.action_edit -> {
            item.isVisible = false
            menu?.findItem(R.id.action_save)?.isVisible = true
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
}
