package cn.hayring.view.cytoscapeview.bean

/**
 * @author Hayring
 * @date 2021/8/22
 * @description cy.add(Element);
 */
interface CyNode : CyElement {
    /**
     * type
     */
    override val group: String
        get() = CyElement.NODES

    /**
     * data
     */
    override var node: Node

    /**
     * position
     */
    var position: Position
}