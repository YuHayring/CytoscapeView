package cn.hayring.view.cytoscapeview.bean

import androidx.annotation.StringDef
import cn.hayring.view.cytoscapeview.bean.CyElement.Companion.EDGES
import cn.hayring.view.cytoscapeview.bean.CyElement.Companion.NODES
import cn.hayring.view.cytoscapeview.bean.CyGroup.Companion.EDGE
import cn.hayring.view.cytoscapeview.bean.CyGroup.Companion.NODE

/**
 * @author Hayring
 * @date 2021/8/22
 * @description Element基类
 */
interface CyElement {
    /**
     * type
     * node or edge
     */
    val group: String

    /**
     * data
     */
    val data: Node

    companion object {
        /**
         * type node
         */
        const val NODES = "nodes"

        /**
         * type edge
         */
        const val EDGES = "edges"
    }
}