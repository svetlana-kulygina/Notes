package com.pinejuice.notes

import android.os.AsyncTask
import java.lang.ref.WeakReference

class InputLoader(private val paginationRef: WeakReference<PaginationView>,
        private val loadingListener: LoadingListener? = null) : AsyncTask<Void, Void, String>() {

    var page = 1

    override fun doInBackground(vararg params: Void?): String {
        val paginationView = paginationRef.get()?.findViewById<PaginationView>(R.id.paginationView)
        return String(paginationView?.loadPage(page) ?: CharArray(0))
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