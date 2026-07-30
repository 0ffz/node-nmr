package me.dvyy.nmr.ui.nodes

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import me.dvyy.nmr.AppDispatchers
import me.dvyy.nmr.bindings.imgui.ImGuiKt
import java.text.Format
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.typeOf

abstract class Node: AutoCloseable {
    val scope = CoroutineScope(AppDispatchers.Frontend)

    /**
     * Automatically retrieves the name from the companion object if it implements NodeInfo,
     * otherwise falls back to the simple class name.
     */
    val nodeInfo = (this::class.companionObjectInstance as NodeInfo<*>)
    val name: String get() = nodeInfo.name

    val id: Int = NodeGraphViewModel.nextId()
    val attributes = mutableListOf<Attribute<*>>()

    private val _parameters = mutableMapOf<String, AnyNodeParameter>()
    val parameters: Map<String, AnyNodeParameter> get() = _parameters

    fun registerParameter(parameter: AnyNodeParameter) {
        _parameters[parameter.name] = parameter
    }

    /**
     * Serializes all registered node parameters to a map of JsonElements.
     */
    fun exportParameters(json: Json = Formats.json): Map<String, JsonElement> {
        return _parameters.mapValues { (_, param) -> param.encodeToJson(json) }
    }

    /**
     * Deserializes and restores parameter values from a map of JsonElements.
     */
    fun importParameters(paramMap: Map<String, JsonElement>, json: Json = Formats.json) {
        paramMap.forEach { (key, element) ->
            _parameters[key]?.decodeFromJson(json, element)
        }
    }

    /**
     * Draws UI elements for this node
     */
    open fun ImGuiKt.draw() {}

    inline fun <reified T> inputAttribute(): InputAttribute<T> {
        return InputAttribute<T>(NodeGraphViewModel.nextId(), localId = attributes.size, typeOf<T>()).also { attributes += it }
    }

    inline fun <reified T> outputAttribute(noinline calculation: () -> T): OutputAttribute<T> {
        return OutputAttribute(NodeGraphViewModel.nextId(), localId = attributes.size, typeOf<T>(), calculation).also { attributes += it }
    }

    override fun close() {
        scope.cancel()
    }
}

