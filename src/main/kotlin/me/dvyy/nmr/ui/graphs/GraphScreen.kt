package me.dvyy.nmr.ui.graphs

import imgui.extension.implot.ImPlot
import imgui.extension.implot.flag.ImPlotAxis
import imgui.extension.implot.flag.ImPlotAxisFlags
import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.bindings.imgui.implotSpec
import me.dvyy.nmr.signal.Signal
import me.dvyy.nmr.ui.nodes.Node
import me.dvyy.nmr.ui.nodes.transformations.GraphEmittingNode
import me.dvyy.nmr.ui.nodes.transformations.toImVec4

fun ImGuiKt.GraphScreen(
    nodes: List<Node>,
) {
    //TODO allow toggling certain graphs on or off
//    if (ImGui.beginCombo("Spectrum", type.name)) {
//        GraphType.entries.forEachIndexed { index, type ->
//            if (ImGui.selectable(type.name, index == type.ordinal)) {
//                onGraphChange(type)
//            }
//        }
//        ImGui.endCombo()
//    }
    subplots("##plots", rows = 2, cols = 1, flags = ImplotSubplotFlags.ShareItems or ImplotSubplotFlags.NoTitle) {
        plot("Spectra") {
            nodes.forEach { node ->
                val step = node.signalStep
                if (step !is GraphEmittingNode) return@forEach
                val graph = step.graph
                val signal = graph?.signal ?: return@forEach
                if (signal != Signal.Empty) {
                val spec = implotSpec {
                    if (graph.color != null) lineColor = graph.color.toImVec4()
                }
                    line(graph.title, signal.graphFid, spec = spec)
                }
            }
        }
        plot("FFT") {
            ImPlot.setupAxis(ImPlotAxis.X1, "ppm", ImPlotAxisFlags.Invert)
            nodes.forEach { node ->
                val step = node.signalStep
                if (step !is GraphEmittingNode) return@forEach
                val graph = step.graph
                val signal = graph?.signal ?: return@forEach
                if (signal != Signal.Empty) {
                    val spec = implotSpec {
                        if (graph.color != null) lineColor = graph.color.toImVec4()
                    }
                    line(graph.title, signal.graphFft, xStart = signal.offset, spec = spec)
                }
            }
        }
    }
}
