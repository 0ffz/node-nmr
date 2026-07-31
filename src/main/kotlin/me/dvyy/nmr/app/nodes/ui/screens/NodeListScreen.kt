package me.dvyy.nmr.app.nodes.ui.screens

import imgui.ImGui
import me.dvyy.nmr.app.bindings.imgui.ImGuiKt
import me.dvyy.nmr.app.nodes.data.NodeInfo
import me.dvyy.nmr.app.nodes.data.NodeRegistry

fun ImGuiKt.NodeListScreen() {
    val nodesByCategory = NodeRegistry.availableNodes.groupBy { it.category }
    for ((category, categoryNodes) in nodesByCategory) {
        val is1D = category == "1D"
        collapsingHeader(category, defaultOpen = is1D) {
            val nodesBySubcategory = categoryNodes.groupBy { it.subcategory }
            for ((subcategory, nodes) in nodesBySubcategory) {
                if (subcategory != null) {
                    treeNode("$subcategory##$category", defaultOpen = is1D) {
                        for (nodeInfo in nodes) {
                            DragDropTransformationSource(nodeInfo)
                        }
                    }
                } else {
                    for (nodeInfo in nodes) {
                        DragDropTransformationSource(nodeInfo)
                    }
                }
            }
        }
    }
}

fun ImGuiKt.DragDropTransformationSource(info: NodeInfo<*>) {
    ImGui.button(info.name)
    if (ImGui.beginDragDropSource()) {
        ImGui.setDragDropPayload("node", info.factory)
        ImGui.text(info.name)
        ImGui.endDragDropSource()
    }
}
