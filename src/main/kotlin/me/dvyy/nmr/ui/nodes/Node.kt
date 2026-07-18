package me.dvyy.nmr.ui.nodes

import me.dvyy.nmr.ui.nodes.transformations.SignalNode

data class Node(
    val id: Int,
    val name: String,
    val signalStep: SignalNode,
    val inputId: Int?,
    val outputId: Int?,
)