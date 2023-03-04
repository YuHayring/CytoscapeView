package cn.hayring.view.cytoscapeview.bean

/**
 * @author Hayring
 * @date 2021/8/23
 * @description edge
 */
interface CyEdge : CyElement {
    /**
     * 类型
     * [节点][.NODES]
     */
    override val group: String
        get() = CyElement.EDGES

    /**
     * data
     */
    override var data: BaseEdge

}