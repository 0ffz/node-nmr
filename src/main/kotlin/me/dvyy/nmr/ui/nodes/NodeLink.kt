package me.dvyy.nmr.ui.nodes

data class NodeLink(
    val id: Int,
    val from: OutputAttribute<*>,
    val into: InputAttribute<*>,
)