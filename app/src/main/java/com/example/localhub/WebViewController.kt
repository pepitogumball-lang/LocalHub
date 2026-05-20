package com.example.localhub

import android.webkit.WebView
import android.webkit.WebViewClient

class WebViewController(private val webView: WebView) {

    fun init() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            loadWithOverviewMode = true
            useWideViewPort = true
        }
        webView.webViewClient = object : WebViewClient() {
            // Handle page load errors, etc.
        }
    }

    fun loadUrl(url: String) {
        webView.loadUrl(url)
    }
}
