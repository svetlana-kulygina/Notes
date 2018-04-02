package com.pinejuice.notes

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle

abstract class SlideActivity : AppCompatActivity() {

    override fun startActivity(intent: Intent) {
        super.startActivity(intent)
        overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out)
    }

    override fun startActivity(intent: Intent, bundle: Bundle?) {
        super.startActivity(intent, bundle)
        overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out)
    }
}
