package me.dvyy.nmr.ui.nodes.transformations

import androidx.compose.runtime.*
import imgui.ImGui
import imgui.ImVec4
import imgui.flag.ImGuiColorEditFlags
import imgui.type.ImBoolean
import imgui.type.ImString
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import me.dvyy.nmr.AppDispatchers
import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.signal.Signal
import me.dvyy.nmr.signal.SignalUiState
import java.awt.Color

sealed interface SignalNode {
    val name: String
    fun ImGuiKt.drawParams() {}
}

interface SignalProviding : SignalNode {
    val output: State<SignalUiState?>

    /**
     * Pipes outputs from this transformation into input of [other].
     */
    fun pipeInto(other: SignalInput) {
        other.inputRef = output
    }
}

enum class ComputeState {
    COMPUTING, DONE
}

abstract class SignalInput : SignalNode {
    private val emptyState = mutableStateOf(null)
    internal var inputRef by mutableStateOf<State<SignalUiState?>>(emptyState)
    val input: Signal? by derivedStateOf { inputRef.value?.signal }

    fun removePipe() {
        inputRef = emptyState
    }
}

fun Color.toImVec4() = ImVec4(red.toFloat(), green.toFloat(), blue.toFloat(), alpha.toFloat()).div(255f, 255f, 255f, 255f)

data class GraphUiState(
    val title: String = "Untitled",
    val signal: SignalUiState,
    val color: Color? = null,
)

interface GraphEmittingNode {
    val graph: GraphUiState?
}

class GraphNode : SignalInput(), GraphEmittingNode {
    override val name: String = "Graph"
    var title by mutableStateOf("Untitled")
    var color by mutableStateOf<Color?>(null)
    val string = ImString(title, 42)
    var autoPhase by mutableStateOf(false)

    override fun ImGuiKt.drawParams() {
        if (ImGui.inputText("Title", string)) {
            title = string.get()
        }
        colorEdit4("Color", color ?: Color.BLACK, onChange = { color = it }, flags = ImGuiColorEditFlags.NoInputs)
        val bool = ImBoolean(autoPhase)
        if (ImGui.checkbox("Auto Phase", bool)) {
            autoPhase = bool.get()
        }
    }

    override val graph: GraphUiState? by derivedStateOf {
        val input = inputRef.value ?: return@derivedStateOf null
        GraphUiState(title, input, color)
    }
}

abstract class SignalTransformation : SignalInput(), SignalProviding {
    val scope = CoroutineScope(AppDispatchers.Frontend)
    var state by mutableStateOf(ComputeState.DONE)

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
        }.filterNotNull().map {
            it.start()
            it.await()
        }.onEach {
            output.value = transformUiState(inputRef.value?.copy(signal = it))
        }
            .launchIn(scope)
    }

    override val output: MutableState<SignalUiState?> = mutableStateOf(null)
//    override val output: State<SignalUiState?> = derivedStateOf {
//        inputRef.value?.copy(signal = transform())
//    }
}
