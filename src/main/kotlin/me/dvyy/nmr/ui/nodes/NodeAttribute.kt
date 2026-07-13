package me.dvyy.nmr.ui.nodes

import androidx.compose.runtime.MutableState

@Deprecated("TODO, move to just calling ImGUI functions directly")
data class NodeAttribute(
    val name: String,
    val state: MutableState<*>
)