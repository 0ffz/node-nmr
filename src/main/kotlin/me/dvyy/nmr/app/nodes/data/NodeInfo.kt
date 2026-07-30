package me.dvyy.nmr.app.nodes.data

interface NodeInfo<out T : Node> {
    val name: String
    val factory: () -> T
}