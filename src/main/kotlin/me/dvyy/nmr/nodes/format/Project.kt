package me.dvyy.nmr.nodes.format

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class NodeModel(
    val id: Int,
    val type: String,
    val data: JsonObject,
)

@Serializable
data class Project(
    val nodes: List<NodeModel>
) {
}