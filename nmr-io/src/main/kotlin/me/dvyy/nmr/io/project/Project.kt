package me.dvyy.nmr.io.project

import kotlinx.serialization.Serializable

@Serializable
data class Project(
    val version: Int = 1,
    val nodes: List<NodeModel> = emptyList(),
    val links: List<LinkModel> = emptyList()
)
