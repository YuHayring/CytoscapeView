package cn.hayring.view.cytoscape

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import cn.hayring.view.cytoscape.ImageDialog

/**
 * @author Hayring
 * @date 2023/1/5
 * @description
 */
class ImageDialog : Dialog {
    var imageView: ImageView? = null
        private set
    var bitmap: Bitmap? = null
        private set

    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context?, theme: Int) : super(context!!, theme) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageView = ImageView(context)
        this.setContentView(imageView!!)
        if (bitmap != null) {
            imageView!!.setImageBitmap(bitmap)
        }
        // 这里要写全，
        imageView!!.setOnClickListener { v: View? -> dismiss() }
    }

    fun setBitmap(bitmap: Bitmap?): ImageDialog {
        this.bitmap = bitmap
        return this
    }
}