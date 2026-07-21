package me.dvyy.nmr.ui.nodes

import imgui.ImGui
import imgui.extension.imnodes.ImNodes
import imgui.extension.imnodes.flag.ImNodesMiniMapLocation
import imgui.flag.ImGuiMouseButton
import imgui.type.ImInt
import me.dvyy.nmr.bindings.imgui.ImGuiKt

fun ImGuiKt.NodeScreen(graph: NodeGraphViewModel) {
    ImNodes.editorContextSet(graph.editorContext)
    ImNodes.getIO().altMouseButton = ImGuiMouseButton.Middle
    ImNodes.beginNodeEditor()
    for (node in graph.nodes) {
        NodeUi(node)
    }

    graph.links.forEach { link ->
        val (id, from, into) = link
        ImNodes.link(id, from.id, into.id)
    }
//    button("Save") {
//        ImNodes.saveCurrentEditorStateToIniFile("test.ini")
//    }
    ImNodes.miniMap(0.2f, ImNodesMiniMapLocation.BottomRight)
    ImNodes.endNodeEditor()

    val start = ImInt(0)
    val end = ImInt(0)
    if (ImNodes.isLinkCreated(start, end)) {
        val from = graph.findAttribute(start.get()) ?: return
        val to = graph.findAttribute(end.get()) ?: return
        graph.link(from as OutputAttribute<*>, to as InputAttribute<*>)
    }
    if (ImNodes.isLinkDestroyed(start)) {
        graph.unlink(start.get())
    }
    val startAttr = ImInt()
    if(ImNodes.isLinkStarted(startAttr)) {
        println("Starting drag from ${startAttr.get()}")
    }

    if (ImGui.isMouseClicked(ImGuiMouseButton.Right)) {
        if(ImNodes.getHoveredNode() != -1) {
            graph.selectedNode = ImNodes.getHoveredNode()
            ImGui.openPopup("node_menu")
        }
        val link = ImNodes.getHoveredLink()
        graph.unlink(link)
    }

    // Drag and drop to create new nodes
    if (ImGui.beginDragDropTarget()) {
        val payloadData = ImGui.acceptDragDropPayload<() -> Node>("node")
        if (payloadData != null) {
            val mouseX = ImGui.getMousePosX()
            val mouseY = ImGui.getMousePosY()
            val node = graph.addNode(payloadData())
            ImNodes.setNodeScreenSpacePos(node.id, mouseX, mouseY)
        }
        ImGui.endDragDropTarget()
    }
    if (ImGui.beginPopup("node_menu")) {
        if (ImGui.menuItem("Delete Node")) {
            graph.removeNode(graph.selectedNode)
        }

        ImGui.endPopup()
    }
    if (ImGui.beginPopup("editor_context_menu")) {
        if (ImGui.menuItem("Add Node")) {
            // Add node logic
        }
        ImGui.endPopup()
    }
}

