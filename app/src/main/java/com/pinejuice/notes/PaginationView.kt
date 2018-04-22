package com.pinejuice.notes

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.view.LayoutInflater
import java.io.InputStream
import java.io.InputStreamReader

@CoordinatorLayout.DefaultBehavior(PaginationBottomBarBehavior::class)
class PaginationView: LinearLayout {

    var scrollDistance: Int = 0
    var data: CharArray? = null
    var offsetNavigation: IntArray = IntArray(0)
    var currentPage = 1
    var maxOffset: Int = 0

    constructor(ctx: Context): super(ctx) {
        init()
    }

    constructor(ctx: Context, attrs: AttributeSet?): super(ctx, attrs) {
        init()
    }

    constructor(ctx: Context, attrs: AttributeSet?, defStyleAttr: Int): super(ctx, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.pagination_view, this)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)
        if (state != null) {
            val savedState = state as SavedState
            currentPage = savedState.currentPage
            offsetNavigation = savedState.offsetNavigation
            scrollDistance = savedState.scrollDistance
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val state = SavedState(super.onSaveInstanceState())
        state.currentPage = currentPage
        state.offsetNavigation = offsetNavigation
        state.scrollDistance = scrollDistance
        return state
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        Log.e("test", "$t, $b, $scrollDistance")
        maxOffset = b - t
        super.onLayout(changed, l, t, r, b)
        top += scrollDistance
    }

    companion object {

        val bufSize = 10000

        val nul = 0.toChar()

        fun trimNull(cbuf: CharArray): CharArray {
            return cbuf.dropLastWhile { it == nul }.toCharArray()
        }

        fun truncateBufferBySpace(cbuf: CharArray): Pair<CharArray, CharArray> {
            if (cbuf.size < bufSize) {
                return Pair(cbuf, CharArray(0))
            }
            var last = cbuf.take(bufSize).indexOfLast{ it.isWhitespace() }
            if (last == -1) {
                last = bufSize
            }
            return Pair(cbuf.copyOf(last), cbuf.copyOfRange(last, cbuf.size))
        }
    }

    fun readInput(input: InputStream?): ArrayList<Int> {
        val res = arrayListOf<Int>()
        val reader = InputStreamReader(input)
        var cbuf: CharArray = kotlin.CharArray(bufSize)
        var cbuf2: CharArray = kotlin.CharArray(bufSize)
        var trunc: CharArray = kotlin.CharArray(0)
        var endLoop = reader.read(cbuf) == -1

        while (!endLoop || !cbuf.all { it == nul } || trunc.isNotEmpty()) {
            endLoop = reader.read(cbuf2) == -1
            val truncPair = truncateBufferBySpace(trunc.plus(if (!endLoop) cbuf else trimNull(cbuf)))
            if (data == null) {
                data = truncPair.first
            }
            res.add(truncPair.first.size)
            trunc = truncPair.second
            cbuf = cbuf2
            cbuf2 = kotlin.CharArray(bufSize)
        }
        offsetNavigation = res.toIntArray()
        return res
    }

    internal class SavedState : View.BaseSavedState {

        var currentPage: Int = 1
        var offsetNavigation: IntArray = IntArray(0)
        var scrollDistance: Int = 0

        constructor(source: Parcel) : super(source) {
            currentPage = source.readInt()
            source.readIntArray(offsetNavigation)
            scrollDistance = source.readInt()
        }

        constructor(superState: Parcelable) : super(superState)

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(currentPage)
            out.writeIntArray(offsetNavigation)
            out.writeInt(scrollDistance)
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