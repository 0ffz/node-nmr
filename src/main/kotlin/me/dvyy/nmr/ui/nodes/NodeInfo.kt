package me.dvyy.nmr.ui.nodes

interface NodeInfo<out T : Node> {
    val name: String
    val factory: () -> T
}
