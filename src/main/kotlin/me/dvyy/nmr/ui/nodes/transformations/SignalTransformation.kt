package me.dvyy.nmr.ui.nodes.transformations

import androidx.compose.runtime.*
import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.ui.nodes.NodeAttribute
import me.dvyy.nmr.signal.Signal

interface SignalProviding {
    val output: State<Signal>
    val parameters: List<NodeAttribute> get() = emptyList()

    fun ImGuiKt.drawParams() {

    }

    /**
     * Pipes outputs from this transformation into input of [other].
     */
    fun pipeInto(other: SignalTransformation) {
        other.inputRef = output
    }
}

abstract class SignalTransformation: SignalProviding {
    abstract val name: String
    private val emptyState = mutableStateOf(null)
    internal var inputRef by mutableStateOf<State<Signal?>>(emptyState)
    val input: Signal? by derivedStateOf { inputRef.value }
    abstract override val output: State<Signal>


    fun removePipe() {
        inputRef = emptyState
    }
}
