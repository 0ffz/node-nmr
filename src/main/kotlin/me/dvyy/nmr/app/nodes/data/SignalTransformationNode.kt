package me.dvyy.nmr.app.nodes.data

import androidx.compose.runtime.*
import imgui.ImGui
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import me.dvyy.nmr.app.bindings.imgui.ImGuiKt
import me.dvyy.nmr.app.bindings.imgui.ImNodeContext
import me.dvyy.nmr.app.nodes.ui.state.SignalUiState
import me.dvyy.nmr.processing.model.Signal

abstract class SignalTransformationNode : Node() {
    var state by mutableStateOf(ComputeState.DONE)
    val inputRef = inputAttribute<SignalUiState?>()
    val input get() = inputRef.value?.signal
    val outputState: MutableState<SignalUiState?> = mutableStateOf(null)
    val output = outputAttribute<SignalUiState?> { outputState.value }

    fun ImGuiKt.drawInput() {
        with(ImNodeContext) {
            inputAttribute(inputRef.id) {
                text("In")
            }
            ImGui.sameLine()
            outputAttribute(output.id) { text("Out") }
        }
    }

    override fun ImGuiKt.draw() {
        drawInput()
    }

    inline fun compute(crossinline block: suspend () -> Signal): Deferred<Signal> {
        return scope.async(Dispatchers.IO, start = CoroutineStart.LAZY) {
            state = ComputeState.COMPUTING
            try {
                block()
            } finally {
                state = ComputeState.DONE
            }
        }
    }

    abstract fun transform(): Deferred<Signal>?

    open fun transformUiState(state: SignalUiState?): SignalUiState? {
        return state
    }

    init {
        snapshotFlow {
            transform()
        }.map {
            if (it == null) return@map null
            it.start()
            it.await()
        }.onEach {
            if (it == null) outputState.value = null
            else outputState.value = transformUiState(inputRef.value?.copy(signal = it))
        }
            .launchIn(scope)
    }
}