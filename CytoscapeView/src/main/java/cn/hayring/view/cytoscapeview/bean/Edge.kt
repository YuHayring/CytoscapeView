package cn.hayring.view.cytoscapeview.bean

/**
 * @date 2022/10/2
 * @author Hayring
 * @description data of the edge
 */
interface Edge: Node {

    /**
     * start node id of the edge
     */
    var source: String

    /**
     * end node id of the edge
     */
    var target: String

}