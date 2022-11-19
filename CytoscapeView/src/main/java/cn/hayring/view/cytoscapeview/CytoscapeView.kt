package cn.hayring.view.cytoscapeview

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.webkit.*
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import cn.hayring.view.BuildConfig
import cn.hayring.view.cytoscapeview.bean.*
import com.google.gson.Gson
import com.housenkui.sdbridgekotlin.Callback
import com.housenkui.sdbridgekotlin.ConsolePipe
import com.housenkui.sdbridgekotlin.Handler
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
     * it must be init at onCreate
     * beacause it's "setUpBridge" calls addJavascriptInterface
     * else window.normalPipe would be undefined
     */
    private val bridge = WebViewJavascriptBridge(context, this).also {
        it.consolePipe = object : ConsolePipe {
            override fun post(string: String) {
                Log.d(JS_CONSOLE_TAG, string)
            }
        }
        //registe cy event handler
        it.register("onNodeEvent", onNodeEventHandler)
        it.register("onEdgeEvent", onEdgeEventHandler)
    }


    /**
     * node event handler
     */
    val onNodeEventHandler = object: Handler<NodeEvent, Unit>  {
        override fun handle(p: NodeEvent) {
            nodeEventListeners[p.event]?.onEvent(p.node)
        }
    }

    /**
     * node event listeners
     */
    val nodeEventListeners = HashMap<CyEvents, OnNodeEventListener>()

    /**
     * edge event handler
     */
    val onEdgeEventHandler = object: Handler<EdgeEvent, Unit>  {
        override fun handle(p: EdgeEvent) {
            edgeEventListeners[p.event]?.onEvent(p.edge)
        }
    }

    /**
     * edge event listeners
     */
    val edgeEventListeners = HashMap<CyEvents, OnEdgeEventListener>()

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

    @Deprecated("Only for test")
    fun addTest() {
        //call js Sync function
        bridge.call("addTest", null, null)
    }

    /**
     * json parser
     */
    var gson: Gson = Gson()


    /**
     * add Node
     */
    fun addNode(node: Node) {
        val cyNode = SimpleCyNode(node)
        val tempString = gson.toJson(cyNode)
        val mapData = gson.fromJson<Map<String,Any>>(tempString, Map::class.java)
        bridge.call("add", HashMap(mapData), null)
    }

    /**
     * add Node with position
     */
    fun addNode(node: Node, position: Position) {
        val cyNode = SimpleCyNode(node, position)
        val tempString = gson.toJson(cyNode)
        val mapData = gson.fromJson<Map<String,Any>>(tempString, Map::class.java)
        bridge.call("add", HashMap(mapData), null)
    }

    /**
     * add Edge
     */
    fun addEdge(edge: Edge) {
        val cyEdge = SimpleCyEdge(edge)
        val tempString = gson.toJson(cyEdge)
        val mapData = gson.fromJson<Map<String,Any>>(tempString, Map::class.java)
        bridge.call("add", HashMap(mapData), null)
    }



    /**
     * remove Node
     */
    fun removeNode(node: Node) {
        val cyNode = SimpleCyNode(node)
        val tempString = gson.toJson(cyNode)
        val mapData = gson.fromJson<Map<String,Any>>(tempString, Map::class.java)
        bridge.call("remove", HashMap(mapData), null)
    }

    /**
     * remove Node
     */
    fun removeElement(id: String) {
        bridge.call("remove", HashMap<String, Any>().also{ it["id"] = id }, null)
    }
    
    

    /**
     * filter Node
     */
    fun filterNode(jsSelector: String, callback: (nodes: List<Node>) -> Unit) {
        bridge.call("filterNode", HashMap<String, Any>().also{ it["param"] = jsSelector }, object : Callback<List<Node>> {
            override fun call(p: List<Node>) {
                Log.d(TAG, "filterNode callback, thread: ${Thread.currentThread().name}")
                callback.invoke(p)
            }
        })
    }

    /**
     * filter Node
     */
    fun filterEdge(jsSelector: String, callback: (nodes: List<Edge>) -> Unit) {
        bridge.call("filterEdge", HashMap<String, Any>().also{ it["param"] = jsSelector }, object : Callback<List<Edge>> {
            override fun call(p: List<Edge>) {
                Log.d(TAG, "filterEdge callback, thread: ${Thread.currentThread().name}")
                callback.invoke(p)
            }
        })
    }

    /**
     * listener to the event of the node
     */
    interface OnNodeEventListener {
        fun onEvent(node: Node)
    }

    /**
     * listener to the event of the edge
     */
    interface OnEdgeEventListener {
        fun onEvent(edge: Edge)
    }

    /**
     * set on node select listener
     */
    fun setOnNodeSelectListener(listener: OnNodeEventListener?) {
        listener?.let {
            nodeEventListeners[CyEvents.select]?:run{
                bridge.call("addNodeListener", CyEvent(CyEvents.select, CyGroup.NODE))
            }
        }?:run {
            bridge.call("removeNodeListener", CyEvent(CyEvents.select, CyGroup.NODE))
            nodeEventListeners.remove(CyEvents.select)
        }
    }

    /**
     * set on edge select listener
     */
    fun setOnEdgeSelectListener(listener: OnEdgeEventListener?) {
        listener?.let {
            edgeEventListeners[CyEvents.select]?:run {
                bridge.call("addEdgeListener", CyEvent(CyEvents.select, CyGroup.EDGE))
            }
            edgeEventListeners[CyEvents.select] = it
        }?:run {
            bridge.call("removeEdgeListener", CyEvent(CyEvents.select, CyGroup.EDGE))
            edgeEventListeners.remove(CyEvents.select)
        }
    }





}