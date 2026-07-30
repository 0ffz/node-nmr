package me.dvyy.nmr.app.graphs.data

import me.dvyy.nmr.app.graphs.ui.state.Graph2DUiState
import me.dvyy.nmr.app.graphs.ui.state.GraphUiState

interface GraphEmitting {
    val graph: GraphUiState?
}

interface Graph2DEmitting {
    val texture: Graph2DUiState?
}

