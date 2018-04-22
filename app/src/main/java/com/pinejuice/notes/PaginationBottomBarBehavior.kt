package com.pinejuice.notes

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.View
import android.support.v4.view.ViewCompat
import android.util.Log
import android.animation.PropertyValuesHolder



class PaginationBottomBarBehavior(context: Context?, attrs: AttributeSet?) :
        CoordinatorLayout.Behavior<View>(context, attrs) {

    var isAnimationRunning: Boolean = false
    var animator: Animator? = null

    override fun layoutDependsOn(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        return dependency is AppBarLayout
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
     //   Log.e("test", "onDependentViewChanged")
        if (child !is PaginationView) {
            throw IllegalArgumentException(
                    "${this::class.java.name} can only be applied to ${PaginationView::class.java.name}")
        }
        if (dependency.top >= 0) {
            Log.e("test", "show pagination")
            show(child)
        }
        if (dependency.bottom <= 0) {
            Log.e("test", "hide pagination")
            hide(child)
        }
        return false
    }

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: View,
            directTargetChild: View, target: View, nestedScrollAxes: Int, type: Int): Boolean {
     //   Log.e("test", "onStartNestedScroll")
        return nestedScrollAxes and ViewCompat.SCROLL_AXIS_VERTICAL != 0
    }

    override fun onStopNestedScroll(coordinatorLayout: CoordinatorLayout, child: View, target: View, type: Int) {
    //    Log.e("test", "onStopNestedScroll")
    }

    override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout, child: View, target: View,
            dx: Int, dy: Int, consumed: IntArray, type: Int) {
        if (child !is PaginationView) {
            throw IllegalArgumentException(
                    "${this::class.java.name} can only be applied to ${PaginationView::class.java.name}")
        }
        shift(child, dy)
    }

    private fun shift(view: PaginationView, y: Int, animate: Boolean = false) {
        val f = {
            val offset = calculateOffset(view, y)
            if (animate) {
                animate(view, offset)
            } else {
                view.scrollDistance += offset
                view.top += offset
                val i = Log.e("test", "no anim $offset ${view.top} ${view.scrollDistance}")
            }
        }
        if (animator != null) {
            Log.e("test", "anim currently running")
            doAfterAnimation(f)
            animator!!.cancel()
        } else {
            f()
        }
    }

    private fun calculateOffset(view: PaginationView, y: Int): Int {
        var offset = y
        val newDistance = view.scrollDistance + y
        if (newDistance < 0) {
            Log.e("test", "of min")
            offset = -view.scrollDistance
        } else if (newDistance > view.maxOffset) {
            Log.e("test", "of max")
            offset = view.maxOffset - view.scrollDistance
        }
        return offset
    }

    private fun animate(view: PaginationView, offset: Int) {
        val t = view.top
        val s = view.scrollDistance
        Log.e("test", "new anim $offset ${t + offset} ${s + offset}")
        val pvhX = PropertyValuesHolder.ofInt("top", t, (t + offset))
        val pvhY = PropertyValuesHolder.ofInt("scrollDistance", s, (s + offset))
        animator = ObjectAnimator.ofPropertyValuesHolder(view, pvhX, pvhY)
        doAfterAnimation()
        animator!!.start()
    }

    private fun hide(view: PaginationView) {
        Log.e("test", "${view.maxOffset} ${view.height} ${view.bottom - view.top}")
        shift(view, view.maxOffset - view.scrollDistance, true)
    }

    private fun show(view: PaginationView) {
        shift(view, -view.scrollDistance, true)
    }

    private fun doAfterAnimation(f: () -> Unit = {}, log: Boolean = true) {
        animator?.addListener(object : Animator.AnimatorListener {

            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                if (log) {Log.e("test", "animation end")}
                animator = null
                f()
            }

            override fun onAnimationCancel(animation: Animator?) {
                if (log) {Log.e("test", "animation cancelled")}
                animator = null
                f()
            }

            override fun onAnimationStart(animation: Animator?) {
                if (log) {Log.e("test", "animation start")}
            }
        })
    }
}