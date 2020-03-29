package com.android.apps.wikisearch.ui.webview

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.android.apps.wikisearch.R
import kotlinx.android.synthetic.main.fragment_web_view.*


class WebViewFragment : Fragment() {
    private val args: WebViewFragmentArgs by navArgs()

    private lateinit var title: String
    private lateinit var urlToLoad: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_web_view, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        title = args.title
        urlToLoad = args.urlToLoad

        tvFwvTitle.text = title
        tvFwvUrl.text = urlToLoad

        if (!TextUtils.isEmpty(urlToLoad)) {
            initWebView()
            wvFwvMainContent.loadUrl(urlToLoad)
        }

        // This callback will only be called when MyFragment is at least Started.
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    if (wvFwvMainContent.canGoBack()) {
                        wvFwvMainContent.goBack()

                    } else {
                        isEnabled = false
                        activity?.onBackPressed()
                    }
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)

        // The callback can be enabled or disabled here or in handleOnBackPressed()
    }

    private fun initWebView() {
        pbFwvLoadingProgress.visibility = View.GONE
        wvFwvMainContent.webViewClient = object : WebViewClient() {
            override fun onPageStarted(
                view: WebView?,
                url: String,
                favicon: Bitmap?
            ) {
                pbFwvLoadingProgress.progress = 0
                pbFwvLoadingProgress.visibility = View.VISIBLE
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(
                view: WebView?,
                url: String
            ) {
                pbFwvLoadingProgress.visibility = View.GONE
                super.onPageFinished(view, url)
            }
        }

        setupWebViewSettings()

        wvFwvMainContent.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(
                view: WebView,
                progress: Int
            ) {
                super.onProgressChanged(view, progress)
                pbFwvLoadingProgress.progress = progress
                if (progress == 100) {
                    pbFwvLoadingProgress.visibility = View.GONE
                }
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebViewSettings() {
        val webSettings = wvFwvMainContent.settings
        webSettings.builtInZoomControls = true
        webSettings.javaScriptEnabled = true
        webSettings.loadWithOverviewMode = true
        webSettings.useWideViewPort = true
        webSettings.displayZoomControls = false
        webSettings.cacheMode = WebSettings.LOAD_NO_CACHE
        webSettings.setSupportMultipleWindows(true)
        webSettings.javaScriptCanOpenWindowsAutomatically = true
        webSettings.domStorageEnabled = true
        wvFwvMainContent.clearHistory()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> TODO("Do something")
            else -> return super.onOptionsItemSelected(item)
        }

        return true
    }
}
