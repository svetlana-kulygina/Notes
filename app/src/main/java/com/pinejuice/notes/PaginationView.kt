package com.pinejuice.notes

import android.content.Context
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import kotlinx.android.synthetic.main.pagination_view.view.*
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.lang.ref.WeakReference

@CoordinatorLayout.DefaultBehavior(PaginationBottomBarBehavior::class)
class PaginationView: LinearLayout {

    var scrollDistance: Int = 0
    var offsetNavigation: IntArray = IntArray(0)
    var currentPage = 0
    var maxOffset: Int = 0
    var initTop: Int = 0
    var loadingListener: InputLoader.LoadingListener? = null
    var uri: Uri? = null
    var pageChangeListener: () -> Unit = {}

    private var _reader: InputStreamReader? = null
    val reader: InputStreamReader
        get() {
            if (_reader == null) {
                if (uri == null) {
                    throw Exception("Uri must be defined.")
                }
                _reader = InputStreamReader(context.contentResolver.openInputStream(uri), Charsets.ISO_8859_1)
            }
            return _reader!!
        }

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
        page_first.setOnClickListener { goToPage(1) }
        page_prev.setOnClickListener { goToPage(maxOf(currentPage - 1, 1)) }
        page_next.setOnClickListener { goToPage(minOf(currentPage + 1, offsetNavigation.size)) }
        page_last.setOnClickListener { goToPage(offsetNavigation.size) }
        page_number.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                val page = page_number.text.toString().toInt()
                goToPage(page)
                true
            } else {
                false
            }
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)
        if (state != null) {
            val savedState = state as SavedState
            currentPage = savedState.currentPage
            offsetNavigation = savedState.offsetNavigation
            scrollDistance = savedState.scrollDistance
            visibility = if (savedState.visible) View.VISIBLE else View.GONE
            uri = savedState.uri
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val state = SavedState(super.onSaveInstanceState())
        state.currentPage = currentPage
        state.offsetNavigation = offsetNavigation
        state.scrollDistance = scrollDistance
        state.visible = visibility == View.VISIBLE
        state.uri = uri
        return state
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        maxOffset = b - t
        initTop = t
        super.onLayout(changed, l, t, r, b)
        top += scrollDistance
    }

    fun getPageInputView(): EditText {
        return page_number
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

    /**
     * Reads {@link InputStream} and fills an array of offsets that are used for page navigation.
     * This method should be called only once.
     * @param page Number of page which content will be returned.
     * @return Content of page with given number or first if given page does not exist.
     */
    fun readInput(page: Int): CharArray {
        Log.e("test", "the only time i called")
        val res = arrayListOf<Int>()
        var cbuf = CharArray(bufSize)
        var cbuf2 = CharArray(bufSize)
        var trunc = CharArray(0)
        var endLoop = readBuf(cbuf) == -1
        var cur = 1
        var data = CharArray(0)

        while (!endLoop || !cbuf.all { it == nul } || trunc.isNotEmpty()) {
            endLoop = readBuf(cbuf2) == -1
            val truncPair = truncateBufferBySpace(trunc.plus(if (!endLoop) cbuf else trimNull(cbuf)))
            if (cur == page || cur == 1) {
                data = truncPair.first
                currentPage = page
            }
            res.add(truncPair.first.size)
            trunc = truncPair.second
            cbuf = cbuf2
            cbuf2 = CharArray(bufSize)
            cur += 1
        }
        offsetNavigation = res.toIntArray()
        resetInput()
        return data
    }

    private fun readBuf(buf: CharArray): Int {
        try {
            return reader.read(buf)
        } catch (ex: FileNotFoundException){
            return -1
        }
    }

    fun loadPage(page: Int): CharArray {
        if (offsetNavigation.isEmpty()) {
            return readInput(page)
        } else {
            val p = minOf(offsetNavigation.size, maxOf(1, page))
            var cur = if (p > currentPage && _reader != null) currentPage else 0
            if (cur == 0) {
                resetInput()
            }
            var cbuf: CharArray? = null
            for (i in cur..offsetNavigation.size - 1) {
                val offset = offsetNavigation[i]
                if (i + 1 == p) {
                    currentPage = p
                    cbuf = CharArray(offset)
                    readBuf(cbuf)
                    Log.e("test", "im here")
                    break
                } else {
                    val k = reader.skip(offset.toLong())
                    Log.e("test", "$k")
                }
                cur += 1
            }
            return cbuf ?: CharArray(0)
        }
    }

    fun resetInput() {
        _reader?.close()
        _reader = null
    }

    fun goToPage(page: Int) {
        pageChangeListener()
        if (page != currentPage) {
            val loader = InputLoader(WeakReference(this), loadingListener)
            loader.page = page
            loader.execute()
        }
    }

    internal class SavedState : View.BaseSavedState {

        var currentPage: Int = 1
        var offsetNavigation: IntArray = IntArray(0)
        var scrollDistance: Int = 0
        var visible: Boolean = true
        var uri: Uri? = null

        constructor(source: Parcel) : super(source) {
            currentPage = source.readInt()
            source.readIntArray(offsetNavigation)
            scrollDistance = source.readInt()
            visible = source.readInt() != 0
            uri = source.readParcelable(Uri::class.java.classLoader)
        }

        constructor(superState: Parcelable) : super(superState)

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(currentPage)
            out.writeIntArray(offsetNavigation)
            out.writeInt(scrollDistance)
            out.writeInt(if (visible) 1 else 0)
            uri?.writeToParcel(out, flags)
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