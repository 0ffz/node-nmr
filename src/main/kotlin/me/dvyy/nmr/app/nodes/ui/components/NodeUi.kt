package me.dvyy.nmr.app.nodes.ui.components

import imgui.ImColor
import imgui.extension.imnodes.ImNodes
import imgui.extension.imnodes.flag.ImNodesCol
import me.dvyy.nmr.app.bindings.imgui.ImGuiKt
import me.dvyy.nmr.app.nodes.data.ComputeState
import me.dvyy.nmr.app.nodes.data.Node
import me.dvyy.nmr.app.nodes.data.SignalTransformationNode

fun ImGuiKt.NodeUi(node: Node) {
    val isComputing = (node as? SignalTransformationNode)?.state == ComputeState.COMPUTING
    if (isComputing) {

        ImNodes.pushColorStyle(ImNodesCol.TitleBar, ImColor.rgb("#525252"))
        ImNodes.pushColorStyle(ImNodesCol.TitleBarSelected, ImColor.rgb("#525252"))
        ImNodes.pushColorStyle(ImNodesCol.TitleBarHovered, ImColor.rgb("#737373"))
    }
    node(node.id) {
        nodeTitleBar {
            text(node.name)
        }

        withItemWidth(100f) {
            with(node) { draw() }
        }
    }
    if (isComputing) {
        ImNodes.popColorStyle()
        ImNodes.popColorStyle()
        ImNodes.popColorStyle()
    }
}