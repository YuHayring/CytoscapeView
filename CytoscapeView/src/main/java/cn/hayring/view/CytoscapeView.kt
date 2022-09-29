package cn.hayring.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.webkit.*
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.webkit.WebViewAssetLoader
import com.housenkui.sdbridgekotlin.Callback
import com.housenkui.sdbridgekotlin.ConsolePipe
import com.housenkui.sdbridgekotlin.WebViewJavascriptBridge


/**
 * @date 2022/9/25
 * @author Hayring
 * @description A cytoscape view implement by WebView and cytoscape.js
 */
class CytoscapeView: WebView {

    constructor(context: Context) : super(context) {
        assertLifeCycleContext(context)
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        assertLifeCycleContext(context)
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        assertLifeCycleContext(context)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        assertLifeCycleContext(context)
    }


    init {
        if (BuildConfig.DEBUG) {
            setWebContentsDebuggingEnabled(true)
        }
    }

    /**
     * check if lifecycle supported
     */
    private fun assertLifeCycleContext(context: Context) {
        if (context !is LifecycleOwner) {
            throw java.lang.UnsupportedOperationException("You must put this view into an Activity implement by LifecycleOwner")
        }
        context.lifecycle.addObserver(lifecycleObserver)
    }


    companion object {
        /**
         * error message when load other url
         */
        const val UNSUPPORTED_LOAD_URL_MESSAGE = "This view is not supported load other uri"


        /**
         * error message when load other webViewClient
         */
        const val UNSUPPORTED_SET_CLIENT = "This view is not supported load other webViewClient"

        /**
         * Log tag
         */
        private const val TAG = "CytoscapeView"

        /**
         * JavaScript Console Tag
         */
        private const val JS_CONSOLE_TAG = "$TAG\$Bridge"

        /**
         * https://developer.android.google.cn/reference/kotlin/androidx/webkit/WebViewAssetLoader
         * Security android assert url prefix
         */
//        private const val SECURITY_ASSERT_URL_PREFIX = "https://${WebViewAssetLoader.DEFAULT_DOMAIN}/assets"
        private const val SECURITY_ASSERT_URL_PREFIX = "file:///android_asset"

            public const val CYTOSCAPE_TARGET_URL = "$SECURITY_ASSERT_URL_PREFIX/index.html"
    }

    final override fun loadUrl(url: String, additionalHttpHeaders: MutableMap<String, String>) {
        throw java.lang.UnsupportedOperationException(UNSUPPORTED_LOAD_URL_MESSAGE)
    }

     fun superLoadUrl(url: String) {
        super.loadUrl(url)
    }


    final override fun loadUrl(url: String) {
        throw java.lang.UnsupportedOperationException(UNSUPPORTED_LOAD_URL_MESSAGE)
    }


    final override fun setWebViewClient(client: WebViewClient) {
        throw java.lang.UnsupportedOperationException(UNSUPPORTED_SET_CLIENT)
    }

    /**
     *
     */
    private val bridge by lazy {
        WebViewJavascriptBridge(context, this).also {
            it.consolePipe = object : ConsolePipe {
                override fun post(string: String) {
                    Log.d(JS_CONSOLE_TAG, string)
                }
            }
        }
    }

//    /**
//     * https://developer.android.google.cn/reference/kotlin/androidx/webkit/WebViewAssetLoader
//     * assetLoader
//     */
//    private val assetLoader by lazy {
//        WebViewAssetLoader.Builder()
//            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(context))
//            .build()
//    }

    /**
     * intercept url by assetLoader
     *
     * https://developer.android.google.cn/reference/kotlin/androidx/webkit/WebViewAssetLoader
     */
    private val webViewClientImplement by lazy {
        object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
//                return request?.url?.let { assetLoader.shouldInterceptRequest(it) }
                return super.shouldInterceptRequest(view, request)
            }
        }
    }

    /**
     * lifecycle event Observer
     */
    val lifecycleObserver = object : DefaultLifecycleObserver {

        override fun onCreate(owner: LifecycleOwner) {
            super.onCreate(owner)
            settings.allowFileAccess = false
            settings.allowContentAccess = false
            settings.javaScriptEnabled = true
            super@CytoscapeView.setWebViewClient(webViewClientImplement)
            super@CytoscapeView.loadUrl(CYTOSCAPE_TARGET_URL)
        }
    }

    fun addTest() {
        //call js Sync function
        bridge.call("addTest", null, null)
    }






}