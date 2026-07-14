package me.dvyy.nmr.ui.nodes.transformations

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.Snapshot
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.sample
import me.dvyy.nmr.AppDispatchers
import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.signal.Signal
import me.dvyy.nmr.signal.SignalUiState
import me.dvyy.nmr.ui.nodes.NodeAttribute
import kotlin.time.Duration.Companion.milliseconds

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

enum class ComputeState {
    COMPUTING, DONE
}
abstract class SignalTransformation : SignalProviding {
    abstract val name: String
    private val emptyState = mutableStateOf(null)
    internal var inputRef by mutableStateOf<State<SignalUiState?>>(emptyState)
    val input: Signal? by derivedStateOf { inputRef.value?.signal }
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

    init {
        snapshotFlow {
            transform()
        }.filterNotNull().map {
            it.start()
            it.await()
        }.onEach {
            Snapshot.withMutableSnapshot {
                output.value = inputRef.value?.copy(signal = it)
            }
        }
            .launchIn(scope)
    }

    override val output: MutableState<SignalUiState?> = mutableStateOf(null)
//    override val output: State<SignalUiState?> = derivedStateOf {
//        inputRef.value?.copy(signal = transform())
//    }

    fun removePipe() {
        inputRef = emptyState
    }
}
