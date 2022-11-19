package cn.hayring.view.cytoscapeview.bean

import androidx.annotation.StringDef
import cn.hayring.view.cytoscapeview.bean.CyGroup.Companion.EDGE
import cn.hayring.view.cytoscapeview.bean.CyGroup.Companion.NODE

/**
 * @date 2022/11/10
 * @author Hayring
 * @description
 */
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD, AnnotationTarget.FUNCTION)
@MustBeDocumented
@StringDef(NODE, EDGE)
@kotlin.annotation.Retention(AnnotationRetention.SOURCE)
annotation
class CyGroup {
    companion object {
        /**
         * type node
         */
        const val NODE = "node"

        /**
         * type edge
         */
        const val EDGE = "edge"
    }
}