package me.dvyy.nmr.ui.nodes

import androidx.compose.runtime.MutableState

data class Parameter(
    val name: String,
    val state: MutableState<*>
)