package cn.hayring.view.cytoscapeview

import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Looper
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Base64
import android.util.Log
import android.webkit.*
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import cn.hayring.view.cytoscapeview.bean.*
import com.google.gson.Gson
import com.housenkui.sdbridgekotlin.Callback
import com.housenkui.sdbridgekotlin.ConsolePipe
import com.housenkui.sdbridgekotlin.Handler
import com.housenkui.sdbridgekotlin.WebViewJavascriptBridge
import kotlinx.coroutines.runBlocking
import java.lang.reflect.Type
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


/**
 * @date 2022/9/25
 * @author Hayring
 * @description A cytoscape view implement by WebView and cytoscape.js
 */
class CytoscapeView: WebView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)



    val DEBUG = context.applicationInfo?.let {
        (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }?: false
    init {
        if (DEBUG) {
            setWebContentsDebuggingEnabled(true)
        }
        isVerticalScrollBarEnabled = false
        isHorizontalScrollBarEnabled = false
    }

    /**
     * attach to lifecycle before any other lifecycleObserver related to this view's onCreate, onStart, onResume
     * attach to lifecycle after any other lifecycleObserver related to this view's onPause, onStop, onDestroy
     * activity: use this
     * fragment: use viewLifecycleOwner.lifecycle
     */
    fun attatchToLifeCycle(lifecycle: Lifecycle) {
        lifecycle.addObserver(lifecycleObserver)
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
     * node event handler
     */
    val onNodeEventHandler = object: Handler<NodeEvent, Unit>  {
        override fun handle(p: NodeEvent) {
            nodeEventListeners[p.event]?.event(p.node)
        }
    }

    var onJavascriptInitializedListener: Runnable? = null

    /**
     * js initialized callBack handler
     */
    val onJavascriptInitialized = object: Handler<Unit, Unit> {
        override fun handle(p: Unit) {
            isJavaScriptInitialized = true
            onJsBridgeInitialized()
            onJavascriptInitializedListener?.run()
        }
    }

    /**
     * 是否初始化
     */
    var isJavaScriptInitialized = false

    /**
     * node event listeners
     */
    val nodeEventListeners = HashMap<CyEvents, OnNodeEventListener<out BaseNode>>()

    /**
     * edge event handler
     */
    val onEdgeEventHandler = object: Handler<EdgeEvent, Unit>  {
        override fun handle(p: EdgeEvent) {
            edgeEventListeners[p.event]?.event(p.edge)
        }
    }

    /**
     * edge event listeners
     */
    val edgeEventListeners = HashMap<CyEvents, OnEdgeEventListener<out BaseEdge>>()


    /**
     * it must be init at onCreate
     * beacause it's "setUpBridge" calls addJavascriptInterface
     * else window.normalPipe would be undefined
     *
     * bridge must be initialized after initialization of onNodeEventHandler and onEdgeEventHandler
     */
    private val bridge = WebViewJavascriptBridge(context, this).also {
        it.consolePipe = object : ConsolePipe {
            override fun post(string: String) {
                Log.d(JS_CONSOLE_TAG, string)
            }
        }
        it.registerTypeAdapter(
            listOf(
                Pair(NodeEvent::class.java, NodeEventTypeAdapter),
                Pair(EdgeEvent::class.java, EdgeEventTypeAdapter)
            )
        )
        //registe cy event handler
        it.register("onNodeEvent", onNodeEventHandler)
        it.register("onEdgeEvent", onEdgeEventHandler)
        it.register("onInitialized", onJavascriptInitialized)
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
            Log.i("CytoscapeLifecycle", "onCreate inner")
            settings.allowFileAccess = false
            settings.allowContentAccess = false
            settings.javaScriptEnabled = true
            super@CytoscapeView.setWebViewClient(webViewClientImplement)
            super@CytoscapeView.loadUrl(CYTOSCAPE_TARGET_URL)
        }



        override fun onPause(owner: LifecycleOwner) {
            Log.i("CytoscapeLifecycle", "onPause inner")
            try {
                var start: Long? = null
                if (DEBUG) {
                    start = System.currentTimeMillis()
                }
                webState = getCytoscapeJsonDataSyncString()
                if (DEBUG) {
                    Log.d(TAG, "onPause getCytoscapeJsonDataSyncString cost: ${System.currentTimeMillis() - start!!}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, "saveWebState failed")
                webState = ""
            }
        }

        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)
            //未初始化
            isJavaScriptInitialized = false
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
    fun addNode(node: BaseNode) {
        val cyNode = SimpleCyNode(node)
        bridge.call("add", cyNode, null)
    }

    /**
     * update Node
     */
    fun updateNode(node: BaseNode) {
        bridge.call("update", node, null)
    }

    /**
     * add Node with position
     */
    fun addNode(node: BaseNode, position: Position) {
        val cyNode = SimpleCyNode(node, position)
        bridge.call("add", cyNode, null)
    }

    /**
     * add Edge
     */
    fun addEdge(edge: BaseEdge) {
        val cyEdge = SimpleCyEdge(edge)
        bridge.call("add", cyEdge, null)
    }


    /**
     * update Edge
     */
    fun updateEdge(edge: BaseEdge) {
        bridge.call("update", edge, null)
    }

    /**
     * remove Node
     */
    fun removeNode(node: BaseNode) {
        val cyNode = SimpleCyNode(node)
        bridge.call("remove", cyNode, null)
    }

    /**
     * remove Node
     */
    fun removeElement(id: String) {
        bridge.call("remove", mapOf("id" to id), null)
    }
    
    

    /**
     * filter Node
     */
    fun filterNode(jsSelector: String, callback: (nodes: List<BaseNode>) -> Unit) {
        bridge.call("filterNode", mapOf("param" to jsSelector), object : Callback<List<BaseNode>> {
            override fun call(p: List<BaseNode>) {
                Log.d(TAG, "filterNode callback, thread: ${Thread.currentThread().name}")
                callback.invoke(p)
            }
        })
    }

    /**
     * filter node suspend
     */
    suspend fun filterNode(jsSelector: String): List<BaseNode> {
        return suspendCoroutine {
            bridge.call("filterNode", mapOf("param" to jsSelector), object : Callback<List<BaseNode>> {
                override fun call(p: List<BaseNode>) {
                    Log.d(TAG, "filterNode callback, thread: ${Thread.currentThread().name}")
                    it.resume(p)
                }
            })
        }
    }


    /**
     * filter Node
     */
    fun filterEdge(jsSelector: String, callback: (nodes: List<BaseEdge>) -> Unit) {
        bridge.call("filterEdge", mapOf("param" to jsSelector), object : Callback<List<BaseEdge>> {
            override fun call(p: List<BaseEdge>) {
                Log.d(TAG, "filterEdge callback, thread: ${Thread.currentThread().name}")
                callback.invoke(p)
            }
        })
    }

    /**
     * listener to the event of the node
     */
    interface OnNodeEventListener<N:BaseNode> {

        fun event(node: BaseNode) {
            onEvent(node as N)
        }

        fun onEvent(node: N)
    }

    /**
     * listener to the event of the edge
     */
    interface OnEdgeEventListener<E:BaseEdge> {

        fun event(edge: BaseEdge) {
            onEvent(edge as E)
        }

        fun onEvent(edge: E)
    }

    /**
     * set on node select listener
     */
    fun setOnNodeSelectListener(listener: OnNodeEventListener<out BaseNode>?) {
        listener?.let {
            nodeEventListeners[CyEvents.select]?:run{
                bridge.call("addNodeListener", CyEvent(CyEvents.select, CyGroup.NODE))
            }
            nodeEventListeners[CyEvents.select] = it
        }?:run {
            bridge.call("removeNodeListener", CyEvent(CyEvents.select, CyGroup.NODE))
            nodeEventListeners.remove(CyEvents.select)
        }
    }

    /**
     * set on edge select listener
     */
    fun setOnEdgeSelectListener(listener: OnEdgeEventListener<out BaseEdge>?) {
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




    /**
     * set on node event listener
     */
    fun setOnNodeEventListener(event: CyEvents, listener: OnNodeEventListener<out BaseNode>?) {
        listener?.let {
            nodeEventListeners[event]?:run{
                bridge.call("addNodeListener", CyEvent(event, CyGroup.NODE))
            }
            nodeEventListeners[event] = it
        }?:run {
            bridge.call("removeNodeListener", CyEvent(event, CyGroup.NODE))
            nodeEventListeners.remove(event)
        }
    }


    /**
     * set on edge event listener
     */
    fun setOnEdgeEventListener(event: CyEvents, listener: OnEdgeEventListener<out BaseEdge>?) {
        listener?.let {
            edgeEventListeners[event]?:run{
                bridge.call("addEdgeListener", CyEvent(event, CyGroup.EDGE))
            }
            edgeEventListeners[event] = it
        }?:run {
            bridge.call("removeNodeListener", CyEvent(event, CyGroup.EDGE))
            edgeEventListeners.remove(event)
        }
    }

    /**
     * change layout position
     */
    fun center(vararg args: String) {
        when (args.size) {
            0 ->
                bridge.call("center")
            1 ->
                bridge.call("center", args[0])
            else ->
                bridge.call("center", args)
        }
    }


    /**
     * reset layout position
     */
    fun reset() {
        bridge.call("reset")
    }

    fun select(id: String) {
        bridge.call("select", id)
    }

    fun unselect(id: String) {
        bridge.call("unselect", id)
    }


    /**
     * make all node selectable
     */
    fun selectifyAllNodes() {
        bridge.call("selectifyAllNodes")
    }

    /**
     * make all node unselectable
     */
    fun unselectifyAllNodes() {
        bridge.call("unselectifyAllNodes")
    }





    /**
     * get bitmap by cy.png
     */
    suspend fun getBitmap(option: ImageOutputOption = ImageOutputOption()): Bitmap? {
        return suspendCoroutine { continuation ->
            bridge.call("png", option, object : Callback<String> {
                override fun call(p: String) {
                    try {
                        val decode: ByteArray =
                            Base64.decode(p, Base64.NO_WRAP)
                        val bitmap: Bitmap = BitmapFactory.decodeByteArray(decode, 0, decode.size)
                        continuation.resume(bitmap)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        continuation.resume(null)
                    }
                }

            })
        }
    }

    /**
     * get bitmap blocked
     */
    fun getBitmapSync(option: ImageOutputOption = ImageOutputOption()) = runBlocking {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            throw IllegalThreadStateException("Could not run on the main thread")
        }
        suspendCoroutine { continuation ->
            bridge.call("png", option, object : Callback<String> {
                override fun call(p: String) {
                    try {
                        val decode: ByteArray =
                            Base64.decode(p, Base64.NO_WRAP)
                        val bitmap: Bitmap = BitmapFactory.decodeByteArray(decode, 0, decode.size)
                        continuation.resume(bitmap)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        continuation.resume(null)
                    }
                }

            }, true)
        }
    }

    /**
     * call cy.json get its data as String
     */
    suspend fun getCytoscapeJsonDataString(): String {
        return suspendCoroutine { continuation ->
            bridge.call("jsonString", object : Callback<String> {
                override fun call(p: String) {
                    continuation.resume(p)
                }

            })

        }
    }

    /**
     * call cy.json get its data as String blocked
     */
    fun getCytoscapeJsonDataSyncString() = runBlocking {
//        if (Looper.getMainLooper() == Looper.myLooper()) {
//            throw IllegalThreadStateException("Could not run on the main thread")
//        }
        suspendCoroutine { continuation ->
            bridge.call("jsonString", object : Callback<String> {
                override fun call(p: String) {
                    continuation.resume(p)
                }
            }, true)
        }
    }



    /**
     * if save WebState with cy.json() at saving Instance State
     */
    var saveWebState = false

    /**
     * saved cy state with cy.json() at saving Instance State
     */
    var webState: String? = null


    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val state = CySavedState(superState!!)
        if (saveWebState && webState != null) {
            state.webState = webState
        }
        return state
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val cyState: CySavedState = state as CySavedState
        super.onRestoreInstanceState(cyState.superState)
        cyState.webState?.let {
            saveWebState = true
            if (it != "") {
                webState = it
            }
        }?:run {
            saveWebState = false
        }

    }

    /**
     * js initialized
     */
    private fun onJsBridgeInitialized() {
        webState?.let {
            bridge.call("restoreFromJsonString", it)
        }
    }

    /**
     * register custom adapter at one time
     */
    fun registerTypeAdapter(list: Collection<Pair<Type?, Any?>>) {
//        if ((context as LifecycleOwner).lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
//            throw IllegalStateException("You must register before starting")
//        }
        val typeList = ArrayList<Pair<Type?, Any?>>()
        list.forEach {
            typeList.add(it)
        }
        typeList.add(Pair(NodeEvent::class.java, NodeEventTypeAdapter))
        typeList.add(Pair(EdgeEvent::class.java, EdgeEventTypeAdapter))
        bridge.registerTypeAdapter(typeList)

    }


    /**
     * restore from json
     * @param json json data
     */
    fun restoreFromJson(json: String) {
        reset()
        if (json.isNotEmpty()) {
            bridge.call("restoreFromJsonString", json)
        }
    }

    /**
     * get all Edges of the node
     */
    suspend fun getLinkedEdges(id: String): List<BaseEdge> {
        return suspendCoroutine {
            bridge.call("connectedEdges", id, object : Callback<List<BaseEdge>> {
                override fun call(p: List<BaseEdge>) {
                    Log.d(TAG, "getLinkedEdges callback, thread: ${Thread.currentThread().name}")
                    it.resume(p)
                }
            })
        }
    }

    /**
     * get nodes between the edge
     */
    suspend fun getLinkedNodes(id: String): List<BaseNode> {
        return suspendCoroutine {
            bridge.call("connectedNodes", id, object : Callback<List<BaseNode>> {
                override fun call(p: List<BaseNode>) {
                    Log.d(TAG, "getLinkedNodes callback, thread: ${Thread.currentThread().name}")
                    it.resume(p)
                }
            })
        }
    }

    class CySavedState: BaseSavedState {


        constructor(superState: Parcelable) : super(superState)

        constructor(source: Parcel) : super(source) {
            webState = source.readString()!!
        }

        constructor(source: Parcel, loader: ClassLoader?) : super(source, loader) {
            webState = source.readString()
        }


        var webState: String? = null

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeString(webState)
        }

        companion object {
            @JvmField
            val CREATOR: Parcelable.Creator<CySavedState> =
                object : Parcelable.ClassLoaderCreator<CySavedState> {
                    override fun createFromParcel(`in`: Parcel): CySavedState {
                        return CySavedState(`in`)
                    }

                    override fun createFromParcel(`in`: Parcel, loader: ClassLoader): CySavedState {
                        return CySavedState(`in`, loader)
                    }

                    override fun newArray(size: Int): Array<CySavedState?> {
                        return arrayOfNulls(size)
                    }
                }
        }
    }
}