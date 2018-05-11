package com.pinejuice.notes

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet

class ToggleableCoordinatorLayout : CoordinatorLayout {

    var scrollEnabled: Boolean = true

    constructor(ctx: Context): super(ctx)

    constructor(ctx: Context, attrs: AttributeSet?): super(ctx, attrs)

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)
        if (state != null) {
            val savedState = state as SavedState
            scrollEnabled = savedState.scrollEnabled
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val state = SavedState(super.onSaveInstanceState())
        state.scrollEnabled = scrollEnabled
        return state
    }

    companion object {

        fun isScrollEnabled(coordinatorLayout: CoordinatorLayout?): Boolean {
            return (coordinatorLayout as? ToggleableCoordinatorLayout)?.scrollEnabled == true
        }
    }

    internal class SavedState : BaseSavedState {

        var scrollEnabled: Boolean = true

        constructor(source: Parcel) : super(source) {
            scrollEnabled = source.readInt() != 0
        }

        constructor(superState: Parcelable) : super(superState)

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(if (scrollEnabled) 1 else 0)
        }

        companion object {

            @JvmField
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {

                override fun createFromParcel(source: Parcel): SavedState {
                    return SavedState(source)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }
}