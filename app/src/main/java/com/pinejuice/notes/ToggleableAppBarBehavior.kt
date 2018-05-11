package com.pinejuice.notes

import android.content.Context
import android.graphics.Rect
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet
import android.util.Log
import android.view.View

class ToggleableAppBarBehavior(context: Context?, attrs: AttributeSet?) :
        AppBarLayout.Behavior(context, attrs) {

    override fun onStopNestedScroll(coordinatorLayout: CoordinatorLayout, child: AppBarLayout, target: View, type: Int) {
        val enabled = ToggleableCoordinatorLayout.isScrollEnabled(coordinatorLayout)
        Log.e("test", "ich bin da")
        if (enabled) {
            Log.e("test", "scroll enabled")
            super.onStopNestedScroll(coordinatorLayout, child, target, type)
        }
    }

    override fun onStartNestedScroll(parent: CoordinatorLayout, child: AppBarLayout,
            directTargetChild: View, target: View, nestedScrollAxes: Int, type: Int): Boolean {
        val enabled = ToggleableCoordinatorLayout.isScrollEnabled(parent)
        Log.e("test", "ich bin hier")
        if (enabled) {
            Log.e("test", "scroll enabled")
        }
        return enabled &&
                super.onStartNestedScroll(parent, child, directTargetChild, target, nestedScrollAxes, type)
    }
}