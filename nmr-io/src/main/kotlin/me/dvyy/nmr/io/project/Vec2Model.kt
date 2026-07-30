package me.dvyy.nmr.io.project

import kotlinx.serialization.Serializable

@Serializable
data class Vec2Model(
    val x: Float = 0f,
    val y: Float = 0f
)