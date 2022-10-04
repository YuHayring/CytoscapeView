package cn.hayring.view.cytoscape

import cn.hayring.view.cytoscapeview.bean.Node

/**
 * @date 2022/10/4
 * @author Hayring
 * @description simpleNode implement
 */
class SimpleNode(
    override var id: String,
    var name: String
    ) : Node