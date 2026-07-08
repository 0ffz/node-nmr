package me.dvyy.nmr.ui.graphs

import imgui.ImGui
import imgui.extension.implot.ImPlot
import imgui.extension.implot.flag.ImPlotAxis
import imgui.extension.implot.flag.ImPlotAxisFlags
import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.bindings.imgui.implotSpec

object ImplotSubplotFlags {
    val SHARE_ITEMS = 1 shl 5
}

enum class GraphType {
    FID, FFT, WAVELET
}

fun ImGuiKt.GraphScreen(
    type: GraphType,
    onGraphChange: (GraphType) -> Unit,
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
            graphs.forEach { spectrum ->
                val spec = implotSpec {
                    if (spectrum.color != null) lineColor = spectrum.color
                }
                line(spectrum.name, spectrum.spectrum, spec = spec)
            }
        }
        plot("FFT") {
            ImPlot.setupAxis(ImPlotAxis.X1, "ppm", ImPlotAxisFlags.Invert)
            graphs.forEach { spectrum ->
                val spec = implotSpec {
                    if (spectrum.color != null) lineColor = spectrum.color
                }
                val draw = when (type) {
                    GraphType.FFT -> spectrum.fft
                    GraphType.WAVELET -> spectrum.waveletRe
                    GraphType.FID -> spectrum.spectrum
                }
                val offset = when (type) {
                    GraphType.FFT -> spectrum.offset
                    else -> 0.0
                }
                val scale = when (type) {
                    GraphType.FFT -> spectrum.scale
                    else -> 1.0
                }
                line(spectrum.name, draw, xStart = offset, xScale = scale, spec = spec)
            }
        }
    }
}
