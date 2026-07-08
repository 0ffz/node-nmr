package me.dvyy.nmr.ui.nodes

import imgui.ImGui
import imgui.extension.imnodes.ImNodes
import imgui.extension.imnodes.flag.ImNodesPinShape
import imgui.flag.ImGuiMouseButton
import imgui.type.ImInt
import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.complex.ComplexDoubleArray
import me.dvyy.nmr.ui.SpectrumViewModel

sealed interface Node {
    val id: Int
    val name: String

    data class Process(
        override val id: Int,
        override val name: String,
        val inputId: Int,
        val outputId: Int,
    ): Node {
        var input: ComplexDoubleArray? = null
    }

    data class Input(
        override val id: Int,
        override val name: String,
        val outputId: Int,
    ): Node
}

fun ImGuiKt.NodeScreen(state: SpectrumViewModel) {
    ImNodes.editorContextSet(state.editorContext)
    ImNodes.beginNodeEditor()
    for(node in state.nodes) {
        NodeUi(node)
    }

    var linkId = 0
    state.links.forEach { inId, outId ->
        ImNodes.link(linkId++, inId, outId)
    }
//    button("Save") {
//        ImNodes.saveCurrentEditorStateToIniFile("test.ini")
//    }
    ImNodes.endNodeEditor()
    val start = ImInt(0)
    val end = ImInt(0)
    if(ImNodes.isLinkCreated(start, end)) {
        println("Link created!")
        state.links[start.get()] = end.get()
    }
    if(ImNodes.isLinkDestroyed(start)) {
        println("Link destroyed!")
        state.links.remove(start.get())
    }

    if (ImGui.isMouseClicked(ImGuiMouseButton.Right)) {
        val link = ImNodes.getHoveredLink()
        println("Clicked $link")
        state.links.remove(link)
    }
}


fun ImGuiKt.NodeUi(node: Node) {
    ImNodes.beginNode(node.id);

    ImNodes.beginNodeTitleBar();
    ImGui.text(node.name);
    ImNodes.endNodeTitleBar();

    when (node) {
        is Node.Process -> {
            ImNodes.beginInputAttribute(node.inputId, ImNodesPinShape.CircleFilled);
            ImGui.text("In");
            ImNodes.endInputAttribute();
            ImGui.sameLine();
            ImNodes.beginOutputAttribute(node.outputId);
            ImGui.text("Out");
            ImNodes.endOutputAttribute();
        }

        is Node.Input -> {
            ImNodes.beginOutputAttribute(node.outputId);
            ImGui.text("Out");
            ImNodes.endOutputAttribute();
        }
    }


    if(node is Node.Process) {
        ImGui.pushItemWidth(100f)
        sliderDouble("lb", 5.0, 0.0, 10.0, onChange = {})
//    sliderDouble("test", 3.0, 0.0, 10.0, onChange = {})
        ImGui.popItemWidth()
    }

    ImNodes.endNode();
}
