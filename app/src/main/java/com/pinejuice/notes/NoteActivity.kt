package com.pinejuice.notes

import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_note.*

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

    fun toggleCaps() {
        editNote.inputType = editNote.inputType.xor(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
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
}
