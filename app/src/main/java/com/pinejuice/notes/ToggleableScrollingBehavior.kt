package com.pinejuice.notes

import android.content.Context
import android.graphics.Rect
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.View

class ToggleableScrollingBehavior(ctx: Context, attrs: AttributeSet?):
        AppBarLayout.ScrollingViewBehavior(ctx, attrs) {

    override fun onRequestChildRectangleOnScreen(parent: CoordinatorLayout?, child: View?,
            rectangle: Rect?, immediate: Boolean): Boolean {
        return ToggleableCoordinatorLayout.isScrollEnabled(parent) &&
                super.onRequestChildRectangleOnScreen(parent, child, rectangle, immediate)
    }
}