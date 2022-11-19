package cn.hayring.view.cytoscapeview.bean

open class CyEvent(
    open val event: CyEvents,
    @CyGroup val group: String
)

data class NodeEvent(
    override val event: CyEvents,
    val node: Node
): CyEvent(event, CyGroup.NODE)

data class EdgeEvent(
    override val event: CyEvents,
    val edge: Edge
): CyEvent(event, CyGroup.EDGE)