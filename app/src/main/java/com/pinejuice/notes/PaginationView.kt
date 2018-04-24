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
    var data: CharArray = CharArray(0)
    var offsetNavigation: IntArray = IntArray(0)
    var currentPage = 1
    var maxOffset: Int = 0
    var loadingListener: InputLoader.LoadingListener? = null

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
            visibility = if (savedState.visible) View.VISIBLE else View.GONE
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val state = SavedState(super.onSaveInstanceState())
        state.currentPage = currentPage
        state.offsetNavigation = offsetNavigation
        state.scrollDistance = scrollDistance
        state.visible = visibility == View.VISIBLE
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

    fun readInput(input: InputStream): ArrayList<Int> {
        val res = arrayListOf<Int>()
        val reader = InputStreamReader(input)
        var cbuf: CharArray = kotlin.CharArray(bufSize)
        var cbuf2: CharArray = kotlin.CharArray(bufSize)
        var trunc: CharArray = kotlin.CharArray(0)
        var endLoop = reader.read(cbuf) == -1

        while (!endLoop || !cbuf.all { it == nul } || trunc.isNotEmpty()) {
            endLoop = reader.read(cbuf2) == -1
            val truncPair = truncateBufferBySpace(trunc.plus(if (!endLoop) cbuf else trimNull(cbuf)))
            if (data.isEmpty()) {
                data = truncPair.first
            }
            res.add(truncPair.first.size)
            trunc = truncPair.second
            cbuf = cbuf2
            cbuf2 = kotlin.CharArray(bufSize)
        }
        offsetNavigation = res.toIntArray()
        visibility = if (res.size <= 1) View.GONE else View.VISIBLE
        return res
    }

    internal class SavedState : View.BaseSavedState {

        var currentPage: Int = 1
        var offsetNavigation: IntArray = IntArray(0)
        var scrollDistance: Int = 0
        var visible: Boolean = true

        constructor(source: Parcel) : super(source) {
            currentPage = source.readInt()
            source.readIntArray(offsetNavigation)
            scrollDistance = source.readInt()
            visible = source.readInt() != 0
        }

        constructor(superState: Parcelable) : super(superState)

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(currentPage)
            out.writeIntArray(offsetNavigation)
            out.writeInt(scrollDistance)
            out.writeInt(if (visible) 1 else 0)
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