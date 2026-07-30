package me.dvyy.nmr.app.nodes.data.parameters

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer
import me.dvyy.nmr.app.nodes.data.Node
import kotlin.reflect.KProperty

interface AnyNodeParameter {
    val name: String
    fun encodeToJson(json: Json): JsonElement
    fun decodeFromJson(json: Json, element: JsonElement)
}

class NodeParameter<T>(
    override val name: String,
    val state: MutableState<T>,
    val serializer: KSerializer<T>,
) : AnyNodeParameter {
    override fun encodeToJson(json: Json): JsonElement =
        json.encodeToJsonElement(serializer, state.value)

    override fun decodeFromJson(json: Json, element: JsonElement) {
        state.value = json.decodeFromJsonElement(serializer, element)
    }
}

inline fun <reified T> nodeState(initialValue: T, serializer: KSerializer<T> = serializer<T>(), name: String? = null): NodeStateProvider<T> =
    NodeStateProvider(initialValue, serializer, name)

class NodeStateProvider<T>(
    private val initialValue: T,
    private val serializer: KSerializer<T>,
    private val nameOverride: String? = null,
) {
    operator fun provideDelegate(thisRef: Node, property: KProperty<*>): NodeStateDelegate<T> {
        val paramName = nameOverride ?: property.name
        val state = mutableStateOf(initialValue)
        val parameter = NodeParameter(paramName, state, serializer)
        thisRef.registerParameter(parameter)
        return NodeStateDelegate(state)
    }
}

class NodeStateDelegate<T>(val state: MutableState<T>) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = state.value
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        state.value = value
    }
}
