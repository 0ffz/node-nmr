package me.dvyy.nmr.io.project

import kotlinx.serialization.Serializable

@Serializable
data class LinkModel(
    val id: Int,
    val fromNode: Int,
    val fromAttribute: Int,
    val toNode: Int,
    val toAttribute: Int
)