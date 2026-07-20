package me.dvyy.nmr.ui.nodes

import me.dvyy.nmr.signal.SignalUiState
import java.awt.Color

data class GraphUiState(
    val title: String = "Untitled",
    val signal: SignalUiState,
    val color: Color? = null,
)

interface GraphEmitting {
    val graph: GraphUiState?
}