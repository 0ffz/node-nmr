package me.dvyy.nmr.ui.graphs

import imgui.ImGui
import imgui.extension.implot.ImPlot
import imgui.extension.implot.ImPlotSpec
import imgui.extension.implot.flag.ImPlotAxis
import imgui.extension.implot.flag.ImPlotAxisFlags
import imgui.extension.implot.flag.ImPlotItemFlags
import me.dvyy.nmr.app.bindings.imgui.ImGuiKt
import me.dvyy.nmr.app.bindings.imgui.implotSpec
import me.dvyy.nmr.app.graphs.data.GraphEmitting
import me.dvyy.nmr.app.graphs.data.GraphType
import me.dvyy.nmr.app.nodes.data.Node
import me.dvyy.nmr.processing.model.Signal
import me.dvyy.nmr.app.bindings.imgui.toImVec4
import java.util.*


fun ImGuiKt.GraphScreen(
    nodes: List<Node>,
    selectedPlots: EnumSet<GraphType>,
    onPlotsChange: (EnumSet<GraphType>) -> Unit,
) {
    if (ImGui.beginCombo("Visible Plots", selectedPlots.joinToString(", ") { it.name })) {
        GraphType.entries.forEach { type ->
            val isSelected = selectedPlots.contains(type)
            if (ImGui.selectable(type.name, isSelected)) {
                val newSelection = EnumSet.copyOf(selectedPlots)
                if (isSelected) newSelection.remove(type) else newSelection.add(type)
                if (newSelection.isNotEmpty()) {
                    onPlotsChange(newSelection)
                }
            }
        }
        ImGui.endCombo()
    }

    val rows = selectedPlots.size

    subplots("##plots", rows = rows, cols = 1) {//, flags = ImplotSubplotFlags.ShareItems or ImplotSubplotFlags.NoTitle) {
        if (selectedPlots.contains(GraphType.FID)) plot("Spectra") {
            nodes.forEach { node ->
                if (node !is GraphEmitting) return@forEach
                val graph = node.graph
                val signal = graph?.signal ?: return@forEach
                if (signal != Signal.Empty) {
                    val spec = implotSpec {
                        if (graph.color != null) lineColor = graph.color.toImVec4()
                    }
                    line(graph.title, signal.graphFid, spec = spec)
                }
            }
        }
        if (selectedPlots.contains(GraphType.FFT)) plot("FFT") {
            ImPlot.setupAxis(ImPlotAxis.X1, "ppm", ImPlotAxisFlags.Invert)
            nodes.forEach { node ->
                if (node !is GraphEmitting) return@forEach
                val graph = node.graph
                val signal = graph?.signal ?: return@forEach
                if (signal != Signal.Empty) {
                    val spec = implotSpec {
                        if (graph.color != null) lineColor = graph.color.toImVec4()
                        lineWeight = 2f
                    }
                    line(
                        graph.title,
                        signal.graphFft,
                        xStart = signal.offset - signal.widthPPM,
                        xScale = signal.widthPPM / signal.graphFft.size,
                        spec = spec
                    )
                }
            }
        }
        if (selectedPlots.contains(GraphType.WAVELET)) plot("Wavelet") {
            nodes.forEach { node ->
                if (node !is GraphEmitting) return@forEach
                val graph = node.graph
                val signal = graph?.signal ?: return@forEach
                if (signal != Signal.Empty) {
                    val spec = implotSpec {
                        if (graph.color != null) lineColor = graph.color.toImVec4()
                    }
                    ImPlot.plotInfLines("Levels", signal.waveletLevels, ImPlotSpec().apply {
                        flags = ImPlotItemFlags.NoLegend
//                        lineColor = ImVec4(0.9f, 0.9f, 0.9f, 1f)
                    })
                    line(graph.title, signal.graphWavelet, spec = spec)
                }
            }
        }
    }
}
