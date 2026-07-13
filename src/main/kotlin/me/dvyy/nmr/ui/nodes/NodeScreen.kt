package me.dvyy.nmr.ui.nodes

import imgui.ImGui
import imgui.extension.imnodes.ImNodes
import imgui.extension.imnodes.flag.ImNodesMiniMapLocation
import imgui.flag.*
import imgui.type.ImInt
import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.ui.nodes.transformations.SignalTransformation

fun ImGuiKt.NodeScreen(graph: NodeGraphViewModel) {
    ImNodes.editorContextSet(graph.editorContext)
    ImNodes.beginNodeEditor()
    for (node in graph.nodes) {
        NodeUi(node, onDelete = { graph.removeNode(node.id) })
    }

    graph.links.forEach { link ->
        val (id, inId, outId) = link
        ImNodes.link(id, inId, outId)
    }
//    button("Save") {
//        ImNodes.saveCurrentEditorStateToIniFile("test.ini")
//    }
    ImNodes.miniMap(0.2f, ImNodesMiniMapLocation.BottomRight)
    ImNodes.endNodeEditor()

    val start = ImInt(0)
    val end = ImInt(0)
    if (ImNodes.isLinkCreated(start, end)) {
        println("Link created!")
        val from = graph.nodeForAttribute(start.get()) ?: return
        val to = graph.nodeForAttribute(end.get()) as? Node.Process ?: return
        graph.link(from, to)
    }
    if (ImNodes.isLinkDestroyed(start)) {
        println("Link destroyed!")
        graph.unlink(start.get())
    }

    if (ImGui.isMouseClicked(ImGuiMouseButton.Right)) {
        val link = ImNodes.getHoveredLink()
        println("Clicked $link")
        graph.unlink(link)
    }

    // Drag and drop to create new nodes
    if (ImGui.beginDragDropTarget()) {
        val payloadData = ImGui.acceptDragDropPayload<SignalTransformation>("node")
        if (payloadData != null) {
            val mouseX = ImGui.getMousePosX()
            val mouseY = ImGui.getMousePosY()
            val node = graph.addTransform(payloadData)
            ImNodes.setNodeScreenSpacePos(node.id, mouseX, mouseY)
        }
        ImGui.endDragDropTarget()
    }
}

