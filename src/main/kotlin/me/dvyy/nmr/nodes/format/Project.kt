package me.dvyy.nmr.nodes.format

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable
data class Vec2Model(
    val x: Float = 0f,
    val y: Float = 0f
)

@Serializable
data class NodeModel(
    val id: Int,
    val type: String,
    val position: Vec2Model = Vec2Model(0f, 0f),
    val params: Map<String, JsonElement> = emptyMap()
)

@Serializable
data class LinkModel(
    val id: Int,
    val fromNode: Int,
    val fromAttribute: Int,
    val toNode: Int,
    val toAttribute: Int
)

@Serializable
data class Project(
    val version: Int = 1,
    val nodes: List<NodeModel> = emptyList(),
    val links: List<LinkModel> = emptyList()
)
