package me.dvyy.nmr.ui.nodes

import androidx.compose.runtime.*

interface SignalProviding {
    val output: State<Signal?>
    val parameters: List<Parameter> get() = emptyList()


    /**
     * Pipes outputs from this transformation into input of [other].
     */
    fun pipeInto(other: SignalTransformation) {
        other.inputRef = output
    }
}

abstract class SignalTransformation: SignalProviding {
    private val emptyState = mutableStateOf(null)
    internal var inputRef by mutableStateOf<State<Signal?>>(emptyState)
    val input: Signal? by derivedStateOf { inputRef.value }
    abstract override val output: State<Signal?>


    fun removePipe() {
        inputRef = emptyState
    }
}
