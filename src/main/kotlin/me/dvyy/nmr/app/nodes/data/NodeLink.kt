package me.dvyy.nmr.app.nodes.data

import me.dvyy.nmr.ui.nodes.InputAttribute
import me.dvyy.nmr.ui.nodes.OutputAttribute

data class NodeLink(
    val id: Int,
    val from: OutputAttribute<*>,
    val into: InputAttribute<*>,
)