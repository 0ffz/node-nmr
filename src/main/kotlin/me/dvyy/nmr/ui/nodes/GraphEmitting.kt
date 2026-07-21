package me.dvyy.nmr.ui.nodes

import me.dvyy.nmr.signal.SignalUiState
import me.dvyy.nmr.ui.graphs.Texture
import org.w3c.dom.Text
import java.awt.Color

data class GraphUiState(
    val title: String = "Untitled",
    val signal: SignalUiState,
    val color: Color? = null,
)

interface GraphEmitting {
    val graph: GraphUiState?
}

interface Graph2DEmitting {
    val texture: Graph2DUiState?
}

class Graph2DUiState(
    val fidTexture: Texture,
    val fftTexture: Texture,
)