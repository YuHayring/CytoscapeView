package cn.hayring.view.cytoscapeview.bean

/**
 * @date 2022/10/2
 * @author Hayring
 * @description CyNode simple implement
 */
class SimpleCyNode(
    override var node: Node,
    override var position: Position = Position()
) : CyNode