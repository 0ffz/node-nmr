package me.dvyy.nmr.ui.nodes.transformations

import imgui.ImVec4
import java.awt.Color

fun Color.toImVec4() = ImVec4(red.toFloat(), green.toFloat(), blue.toFloat(), alpha.toFloat()).div(255f, 255f, 255f, 255f)