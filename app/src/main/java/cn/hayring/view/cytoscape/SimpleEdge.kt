package cn.hayring.view.cytoscape

import cn.hayring.view.cytoscapeview.bean.Edge

/**
 * @date 2022/10/4
 * @author Hayring
 * @description simpleEdge implement
 */
class SimpleEdge(
    override var id: String,
    override var source: String,
    override var target: String
) : Edge