package me.dvyy.nmr.app.graphs.ui.state

import me.dvyy.nmr.app.nodes.ui.state.SignalUiState
import java.awt.Color

data class GraphUiState(
    val title: String = "Untitled",
    val signal: SignalUiState,
    val color: Color? = null,
)