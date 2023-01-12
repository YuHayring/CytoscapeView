package cn.hayring.view.cytoscape

import cn.hayring.view.cytoscapeview.bean.Edge
import com.google.gson.*
import java.lang.reflect.Type

/**
 * @date 2022/10/4
 * @author Hayring
 * @description simpleEdge implement
 */
class SimpleEdge(
    override var id: String,
    override var source: String,
    override var target: String
) : Edge {




    object SimpleEdgeTypeAdapter: JsonDeserializer<Edge>, JsonSerializer<Edge> {
        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ) = context?.deserialize<SimpleEdge>(json, SimpleEdge::class.java)

        override fun serialize(
            src: Edge?,
            typeOfSrc: Type?,
            context: JsonSerializationContext?
        ): JsonElement {
            val element = JsonObject()
            element.addProperty("id", (src as SimpleEdge).id)
            element.addProperty("source", (src as SimpleEdge).source)
            element.addProperty("target", (src as SimpleEdge).target)
            return element
        }
    }
}