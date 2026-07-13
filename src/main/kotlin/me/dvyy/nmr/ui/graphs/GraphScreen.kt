package me.dvyy.nmr.ui.graphs

import imgui.ImGui
import imgui.extension.implot.ImPlot
import imgui.extension.implot.flag.ImPlotAxis
import imgui.extension.implot.flag.ImPlotAxisFlags
import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.ui.nodes.Node

fun ImGuiKt.GraphScreen(
    type: GraphType,
    onGraphChange: (GraphType) -> Unit,
    nodes: List<Node>,
    graphs: List<SpectrumUiState>,
) {
    if (ImGui.beginCombo("Spectrum", type.name)) {
        GraphType.entries.forEachIndexed { index, type ->
            if (ImGui.selectable(type.name, index == type.ordinal)) {
                onGraphChange(type)
            }
        }
        ImGui.endCombo()
    }
    subplots("##ItemSharing", rows = 2, cols = 1, flags = ImplotSubplotFlags.SHARE_ITEMS) {
        plot("Spectra") {
            nodes.forEach { node ->
                val signal = node.signalStep.output.value ?: return@forEach
//                val spec = implotSpec {
//                    if (spectrum.color != null) lineColor = spectrum.color
//                }
                line(node.name, signal.graphFid/*, spec = spec*/)
            }
        }
        plot("FFT") {
            ImPlot.setupAxis(ImPlotAxis.X1, "ppm", ImPlotAxisFlags.Invert)
            nodes.forEach {
                val signal = it.signalStep.output.value ?: return@forEach
//                val spec = implotSpec {
//                    if (spectrum.color != null) lineColor = spectrum.color
//                }

//            }
//            graphs.forEach { spectrum ->
//                val draw = when (type) {
//                    GraphType.FFT -> spectrum.fft
//                    GraphType.WAVELET -> spectrum.waveletRe
//                    GraphType.FID -> spectrum.spectrum
//                }
//                val offset = when (type) {
//                    GraphType.FFT -> spectrum.offset
//                    else -> 0.0
//                }
//                val scale = when (type) {
//                    GraphType.FFT -> spectrum.scale
//                    else -> 1.0
//                }

                line(it.name, signal.graphFft)//, xStart = offset, xScale = scale, spec = spec)
            }
        }
    }
}
