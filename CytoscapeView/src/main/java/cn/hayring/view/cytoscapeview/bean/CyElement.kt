package cn.hayring.view.cytoscapeview.bean

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
    val data: BaseNode

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