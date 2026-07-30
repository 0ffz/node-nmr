package me.dvyy.nmr.io.project

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class NodeModel(
    val id: Int,
    val type: String,
    val position: Vec2Model = Vec2Model(0f, 0f),
    val params: Map<String, JsonElement> = emptyMap()
)