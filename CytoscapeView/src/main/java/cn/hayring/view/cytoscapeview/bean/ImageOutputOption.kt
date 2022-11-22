package cn.hayring.view.cytoscapeview.bean

import androidx.annotation.ColorInt

/**
 * https://js.cytoscape.org/#cy.jpg
 */
data class ImageOutputOption(
    @ColorInt var bg: Int? = null,
    var full: Boolean? = null,
    var scale: Float? = null,
    var maxWidth: Int? = null,
    var maxHeight: Int? = null,
    /**
     * only jpg
     */
    var quality: Int? = null
) {
    @JvmField
    val output = "base64"
}
