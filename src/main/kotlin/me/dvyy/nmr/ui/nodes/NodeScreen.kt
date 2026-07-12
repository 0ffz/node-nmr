package me.dvyy.nmr.ui.nodes

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import imgui.ImGui
import imgui.ImVec2
import imgui.extension.imnodes.ImNodes
import imgui.extension.imnodes.flag.ImNodesMiniMapLocation
import imgui.extension.imnodes.flag.ImNodesPinShape
import imgui.extension.implot.ImPlot
import imgui.extension.implot.flag.ImPlotAxis
import imgui.extension.implot.flag.ImPlotAxisFlags
import imgui.extension.implot.flag.ImPlotFlags
import imgui.flag.ImGuiMouseButton
import imgui.flag.ImGuiTreeNodeFlags
import imgui.type.ImInt
import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.ui.graphs.GraphType

data class NodeUiState(
    val graphType: GraphType = GraphType.FID,
)
sealed interface Node {
    val id: Int
    val name: String
    val signalStep: SignalProviding
    val outputId: Int
    var state: NodeUiState

    data class Process(
        override val id: Int,
        override val name: String,
        override val signalStep: SignalTransformation,
        val inputId: Int,
        override val outputId: Int
    ) : Node {
        override var state by mutableStateOf(NodeUiState())
    }

    data class Input(
        override val id: Int,
        override val name: String,
        override val signalStep: SignalProviding,
        override val outputId: Int,
    ) : Node {
        override var state by mutableStateOf(NodeUiState())
    }
}

fun ImGuiKt.NodeScreen(graph: NodeGraphViewModel) {
    ImNodes.editorContextSet(graph.editorContext)
    ImNodes.beginNodeEditor()
    for (node in graph.nodes) {
        NodeUi(node)
    }

    var linkId = 0
    graph.links.forEach { inId, outId ->
        ImNodes.link(inId, inId, outId)
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
}


fun ImGuiKt.NodeUi(node: Node) {
    ImNodes.beginNode(node.id);

    ImNodes.beginNodeTitleBar();
    ImGui.text(node.name);
    ImNodes.endNodeTitleBar();

    when (node) {
        is Node.Process -> {
//            ImNodes.pushColorStyle(ImNodesCol.Pin, ImColor.rgb("ffffffff"))
            ImNodes.beginInputAttribute(node.inputId, ImNodesPinShape.CircleFilled);
            ImGui.text("In");
            ImNodes.endInputAttribute();
//            ImNodes.popColorStyle()
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


    if (node is Node.Process) {
        val states = node.signalStep.parameters
        ImGui.pushItemWidth(100f)
        for (param in states) {
            val value = param.state.value
            when (value) {
                is Double -> dragDouble(param.name, value, onChange = { (param.state as MutableState<Double>).value = it })
                is Int -> sliderInt(param.name, value, 0, 10, onChange = { (param.state as MutableState<Int>).value = it })
            }

        }
        ImGui.popItemWidth()
    }

    section("Plot", defaultOpen = false, flags = ImGuiTreeNodeFlags.SpanLabelWidth) {

        ImGui.pushItemWidth(100f)
        if (ImGui.beginCombo("Spectrum", node.state.graphType.name)) {
            GraphType.entries.forEachIndexed { index, type ->
                if (ImGui.selectable(type.name, index == type.ordinal)) {
                    node.state = node.state.copy(graphType = type)
                }
            }
            ImGui.endCombo()
        }

        ImGui.popItemWidth()
        plot("fid", ImVec2(400f, 200f), flags = ImPlotFlags.NoTitle) {
            ImPlot.setupAxis(ImPlotAxis.X1, "x", ImPlotAxisFlags.AutoFit)
            ImPlot.setupAxis(ImPlotAxis.Y1, "y", ImPlotAxisFlags.AutoFit)

            val value = node.signalStep.output.value ?: return@plot
            val data = when(node.state.graphType) {
                GraphType.FID -> value.graphFid
                GraphType.FFT -> value.graphFft
                GraphType.WAVELET -> TODO()
            } ?: return@plot
            line("fid", data)
        }

    }

    ImNodes.endNode();
}
