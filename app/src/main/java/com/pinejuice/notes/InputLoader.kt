package com.pinejuice.notes

import android.os.AsyncTask
import java.io.InputStream
import java.lang.ref.WeakReference

class InputLoader(private val paginationRef: WeakReference<PaginationView>,
        private val loadingListener: LoadingListener? = null) : AsyncTask<InputStream, Void, String>() {

    override fun doInBackground(vararg params: InputStream?): String {
        val input = params[0]
        if (input != null) {
            val paginationView = paginationRef.get()?.findViewById<PaginationView>(R.id.paginationView)
            paginationView?.readInput(input)
            return String(paginationView?.data ?: CharArray(0))
        }
        return String(CharArray(0))
    }

    override fun onPreExecute() {
        super.onPreExecute()
        loadingListener?.onLoadingStart()
    }

    override fun onPostExecute(result: String) {
        super.onPostExecute(result)
        loadingListener?.onLoadingEnd(result)
    }

    interface LoadingListener {

        fun onLoadingStart()

        fun onLoadingEnd(result: String)
    }
}