package com.pinejuice.notes

import android.content.Context
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.View
import android.support.v4.view.ViewCompat

class PaginationBottomBarBehavior(context: Context?, attrs: AttributeSet?) :
        CoordinatorLayout.Behavior<View>(context, attrs) {

    override fun layoutDependsOn(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        return dependency is AppBarLayout
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        if (child !is PaginationView) {
            throw IllegalArgumentException(
                    "${this::class.java.name} can only be applied to ${PaginationView::class.java.name}")
        }
        shift(child, dependency.top.toFloat() / dependency.height)
        return false
    }

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: View,
            directTargetChild: View, target: View, nestedScrollAxes: Int, type: Int): Boolean {
        return nestedScrollAxes and ViewCompat.SCROLL_AXIS_VERTICAL != 0
    }

    private fun shift(view: PaginationView, shift: Float) {
        val offset = calculateOffset(view, shift)
        view.scrollDistance = offset - view.initTop
        view.top = offset
    }

    private fun calculateOffset(view: PaginationView, shift: Float): Int {
        var distance = -(view.maxOffset * shift).toInt()
        if (distance < 0) {
            distance = 0
        } else if (distance > view.maxOffset) {
            distance = view.maxOffset
        }
        return view.initTop + distance
    }
}