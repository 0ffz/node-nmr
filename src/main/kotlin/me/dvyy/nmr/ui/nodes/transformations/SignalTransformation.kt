package me.dvyy.nmr.ui.nodes.transformations

import androidx.compose.runtime.*
import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.signal.Signal
import me.dvyy.nmr.signal.SignalUiState
import me.dvyy.nmr.ui.nodes.NodeAttribute

interface SignalProviding {
    val output: State<SignalUiState?>
    val parameters: List<NodeAttribute> get() = emptyList()

    fun ImGuiKt.drawParams() {}

    /**
     * Pipes outputs from this transformation into input of [other].
     */
    fun pipeInto(other: SignalTransformation) {
        other.inputRef = output
    }
}

abstract class SignalTransformation : SignalProviding {
    abstract val name: String
    private val emptyState = mutableStateOf(null)
    internal var inputRef by mutableStateOf<State<SignalUiState?>>(emptyState)
    val input: Signal? by derivedStateOf { inputRef.value?.signal }

    abstract fun transform(): Signal

    override val output: State<SignalUiState?> = derivedStateOf {
        inputRef.value?.copy(signal = transform())
    }

    fun removePipe() {
        inputRef = emptyState
    }
}
