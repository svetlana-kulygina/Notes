package com.pinejuice.notes

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_note.*
import android.view.inputmethod.InputMethodManager
import android.text.*


class NoteActivity : SlideActivity() {

    private var noteExists = false
    private var menu: Menu? = null
    lateinit var imm: InputMethodManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)
        if (savedInstanceState?.get("key") != null) {
            noteExists = true
        }
        editNote.addTextChangedListener(textWatcher)
        imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    fun getFocusedChar(): Char? {
        val focus = editNote.selectionEnd
        if (focus == editNote.selectionStart) {
            return editNote.text.getOrNull(focus - 1)
        }
        return null
    }

    fun setInputMask(enableCaps: Boolean) {
        var mask = editNote.inputType
        if (enableCaps) {
            mask = mask.or(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
        } else if (mask.and(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES) != 0) {
            mask = mask.xor(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
        }
        editNote.inputType = mask
    }

    private val textWatcher = object : TextWatcher {

        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

        }

        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            val f = getFocusedChar()
            if (f?.toByte() == java.lang.Character.LETTER_NUMBER) {
                setInputMask(false)
            } else {
                setInputMask(true)
            }
        }

        override fun afterTextChanged(editable: Editable) {

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
