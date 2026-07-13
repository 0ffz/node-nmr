package me.dvyy.nmr.ui.nodes

import me.dvyy.nmr.ui.graphs.GraphType

data class NodeUiState(
    val graphType: GraphType = GraphType.FID,
)