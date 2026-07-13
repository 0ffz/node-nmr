package me.dvyy.nmr.ui.nodes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import me.dvyy.nmr.ui.nodes.transformations.SignalProviding
import me.dvyy.nmr.ui.nodes.transformations.SignalTransformation

sealed interface Node {
    val id: Int
    val name: String
    val signalStep: SignalProviding
    val outputId: Int
    var state: NodeUiState

    data class Process(
        override val id: Int,
        override val name: String,
        override val signalStep: SignalTransformation,
        val inputId: Int,
        override val outputId: Int,
    ) : Node {
        override var state by mutableStateOf(NodeUiState())
    }

    data class Input(
        override val id: Int,
        override val name: String,
        override val signalStep: SignalProviding,
        override val outputId: Int,
    ) : Node {
        override var state by mutableStateOf(NodeUiState())
    }
}