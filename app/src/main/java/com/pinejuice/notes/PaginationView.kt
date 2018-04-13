package com.pinejuice.notes

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.view.LayoutInflater
import java.io.InputStream
import java.io.InputStreamReader

class PaginationView: LinearLayout {

    var data: CharArray? = null
    var offsetNavigation: IntArray? = null
    var currentPage = 1

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
            val savedState = SavedState(state)
            currentPage = savedState.currentPage
            offsetNavigation = savedState.offsetNavigation
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val state = SavedState(super.onSaveInstanceState())
        state.currentPage = currentPage
        state.offsetNavigation = offsetNavigation
        return state
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
            Log.e("test", truncPair.first.contentToString())
            res.add(truncPair.first.size)
            trunc = truncPair.second
            cbuf = cbuf2
            cbuf2 = kotlin.CharArray(bufSize)
        }
        for (i in res) {
            Log.e("test", "$i")
        }
        offsetNavigation = res.toIntArray()
        return res
    }

    internal class SavedState : View.BaseSavedState {

        var currentPage: Int = 1
        var offsetNavigation: IntArray? = null

        constructor(source: Parcel) : super(source) {
            currentPage = source.readInt()
            source.readIntArray(offsetNavigation)
        }

        constructor(superState: Parcelable) : super(superState)

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(currentPage)
            out.writeIntArray(offsetNavigation)
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