package cn.hayring.view.cytoscapeview.bean

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

open class CyEvent(
    open val event: CyEvents,
    @CyGroup val group: String
)

data class NodeEvent(
    override val event: CyEvents,
    val node: BaseNode
): CyEvent(event, CyGroup.NODE)

data class EdgeEvent(
    override val event: CyEvents,
    val edge: BaseEdge
): CyEvent(event, CyGroup.EDGE)

object NodeEventTypeAdapter: JsonDeserializer<NodeEvent?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): NodeEvent? {
        return json?.asJsonObject?.let {
            context?.deserialize<BaseNode>(it["node"], BaseNode::class.java)
                ?.let { node -> NodeEvent(CyEvents.valueOf(it["event"].asString), node) }
        }
    }
}

object EdgeEventTypeAdapter: JsonDeserializer<EdgeEvent?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): EdgeEvent? {
        return json?.asJsonObject?.let {
            context?.deserialize<BaseEdge>(it["edge"], BaseEdge::class.java)
                ?.let { edge -> EdgeEvent(CyEvents.valueOf(it["event"].asString), edge) }
        }
    }
}