package me.dvyy.nmr.ui.nodes

import imgui.ImColor
import imgui.ImGui
import imgui.extension.imnodes.ImNodes
import imgui.extension.imnodes.flag.ImNodesCol
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiStyleVar
import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.ui.nodes.transformations.ComputeState
import me.dvyy.nmr.ui.nodes.transformations.SignalTransformationNode
import me.dvyy.nmr.ui.spectra.Icons

fun ImGuiKt.NodeUi(node: Node, onDelete: () -> Unit) {
    val isComputing = (node as? SignalTransformationNode)?.state == ComputeState.COMPUTING
    if(isComputing) {
        ImNodes.pushColorStyle(ImNodesCol.TitleBar, ImColor.rgb("#525252"))
        ImNodes.pushColorStyle(ImNodesCol.TitleBarSelected, ImColor.rgb("#525252"))
        ImNodes.pushColorStyle(ImNodesCol.TitleBarHovered, ImColor.rgb("#737373"))
    }
    node(node.id) {
        nodeTitleBar {
            text(node.name)
            ImGui.sameLine()
            ImGui.pushStyleColor(ImGuiCol.Button, 0.0f, 0.0f, 0.0f, 0.0f);
            ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 1.0f, 1.0f, 1.0f, 0.1f);
            withStyle(ImGuiStyleVar.FrameRounding, 5.0f) {
                button(Icons.delete) { onDelete() }
            }
            ImGui.popStyleColor(2)
        }

        withItemWidth(100f) {
            with(node) { draw() }
        }

//    section("Plot", defaultOpen = false, flags = ImGuiTreeNodeFlags.SpanLabelWidth) {
//
//        ImGui.pushItemWidth(100f)
//        if (ImGui.beginCombo("Spectrum", node.state.graphType.name)) {
//            GraphType.entries.forEachIndexed { index, type ->
//                if (ImGui.selectable(type.name, index == type.ordinal)) {
//                    node.state = node.state.copy(graphType = type)
//                }
//            }
//            ImGui.endCombo()
//        }
//
//        ImGui.popItemWidth()
//        plot("fid", ImVec2(400f, 200f), flags = ImPlotFlags.NoTitle) {
//            ImPlot.setupAxis(ImPlotAxis.X1, "x", ImPlotAxisFlags.AutoFit)
//            ImPlot.setupAxis(ImPlotAxis.Y1, "y", ImPlotAxisFlags.AutoFit)
//
//            val value = node.signalStep.output.value ?: return@plot
//            val data = when (node.state.graphType) {
//                GraphType.FID -> value.graphFid
//                GraphType.FFT -> value.graphFft
//                GraphType.WAVELET -> TODO()
//            } ?: return@plot
//            line("fid", data)
//        }
//
//    }
    }
    if(isComputing) {
        ImNodes.popColorStyle()
        ImNodes.popColorStyle()
        ImNodes.popColorStyle()
    }
}