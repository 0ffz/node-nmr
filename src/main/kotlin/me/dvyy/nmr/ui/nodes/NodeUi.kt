package me.dvyy.nmr.ui.nodes

import androidx.compose.runtime.MutableState
import imgui.ImColor
import imgui.ImGui
import imgui.extension.imnodes.ImNodes
import imgui.extension.imnodes.flag.ImNodesCol
import imgui.extension.imnodes.flag.ImNodesPinShape
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiColorEditFlags
import imgui.flag.ImGuiStyleVar
import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.ui.nodes.transformations.ComputeState
import me.dvyy.nmr.ui.nodes.transformations.SignalTransformation
import me.dvyy.nmr.ui.spectra.Icons
import java.awt.Color

fun ImGuiKt.NodeUi(node: Node, onDelete: () -> Unit) {
    val isComputing = (node.signalStep as? SignalTransformation)?.state == ComputeState.COMPUTING
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

        when (node) {
            is Node.Process -> {
//              ImNodes.pushColorStyle(ImNodesCol.Pin, ImColor.rgb("ffffffff"))
                inputAttribute(node.inputId, ImNodesPinShape.CircleFilled) {
                    text("In")
                }
                ImGui.sameLine();
                outputAttribute(node.outputId) {
                    text("Out");
                }
            }

            is Node.Input -> {
                outputAttribute(node.outputId) {
                    text("Out");
                }
            }
        }


        withItemWidth(100f) {
            val states = node.signalStep.parameters
            for (param in states) {
                val value = param.state.value
                when (value) {
                    is Double -> dragDouble(param.name, value, onChange = { (param.state as MutableState<Double>).value = it })
                    is Int -> sliderInt(param.name, value, 0, 50, onChange = { (param.state as MutableState<Int>).value = it })
                    is Color -> {
                        colorEdit4(param.name, value, { (param.state as MutableState<Color>).value = it }, flags = ImGuiColorEditFlags.NoInputs)
                    }
                }

            }
            with(node.signalStep) { drawParams() }
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