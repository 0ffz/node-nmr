package me.dvyy.nmr.ui.nodes

import me.dvyy.nmr.bindings.imgui.ImGuiKt
import kotlin.reflect.typeOf

abstract class Node {
    abstract val name: String
    val id: Int = NodeGraphViewModel.nextId()
    val attributes = mutableListOf<Attribute<*>>()

    /**
     * Draws UI elements for this node
     */
    open fun ImGuiKt.draw() {}

    inline fun <reified T> inputAttribute(): InputAttribute<T> {
        return InputAttribute<T>(NodeGraphViewModel.nextId(), typeOf<T>()).also { attributes += it }
    }

    inline fun <reified T> outputAttribute(noinline calculation: () -> T): OutputAttribute<T> {
        return OutputAttribute(NodeGraphViewModel.nextId(), typeOf<T>(), calculation).also { attributes += it }
    }
}
