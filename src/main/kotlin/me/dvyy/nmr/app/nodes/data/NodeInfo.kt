package me.dvyy.nmr.app.nodes.data

interface NodeInfo<out T : Node> {
    val name: String
    val category: String
    val subcategory: String? get() = null
    val factory: () -> T
}