package com.pinejuice.notes

import android.app.Activity
import java.util.*
import kotlin.concurrent.schedule

class NavigationTimer(val activity: Activity, val cb: () -> Unit) {

    companion object {
        private val scheduleTime = 5000L
    }

    private val timer = Timer("NavigationTimer", false)
    private var task: TimerTask? = null

    fun start() {
        task?.cancel()
        task = timer.schedule(scheduleTime) {
            activity.runOnUiThread( { cb() } )
            task?.cancel()
        }
    }

    fun cancel() {
        task?.cancel()
    }
}