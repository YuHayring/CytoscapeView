package cn.hayring.view.cytoscape

import cn.hayring.view.cytoscapeview.bean.Node
import com.google.gson.*
import org.json.JSONStringer
import java.lang.reflect.Type

/**
 * @date 2022/10/4
 * @author Hayring
 * @description simpleNode implement
 */
class SimpleNode(
    override var id: String,
    var name: String
    ) : Node {




        object SimpleNodeTypeAdapter : JsonDeserializer<Node>, JsonSerializer<Node> {
            override fun deserialize(
                json: JsonElement?,
                typeOfT: Type?,
                context: JsonDeserializationContext?
            ) = context?.deserialize<SimpleNode>(json, SimpleNode::class.java)

            override fun serialize(
                src: Node?,
                typeOfSrc: Type?,
                context: JsonSerializationContext?
            ): JsonElement {
                val element = JsonObject()
                element.addProperty("id", (src as SimpleNode).id)
                element.addProperty("name", (src as SimpleNode).name)
                return element
            }
        }
    }