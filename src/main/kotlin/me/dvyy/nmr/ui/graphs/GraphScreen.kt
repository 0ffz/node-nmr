package me.dvyy.nmr.ui.graphs

import imgui.ImGui.*
import imgui.flag.ImGuiWindowFlags
import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.bindings.imgui.implotSpec

fun ImGuiKt.GraphScreen(graphs: List<SpectrumUiState>) {
    val viewport = getMainViewport()
    setNextWindowPos(viewport.posX, viewport.posY)
    setNextWindowSize(viewport.sizeX, viewport.sizeY)

    window("Graphs", ImGuiWindowFlags.NoDecoration) {
        subplots("##ItemSharing", rows = 2, cols = 1) {
            plot("Spectra") {
                graphs.forEach { spectrum ->
                    val spec = implotSpec {
                        if (spectrum.color != null) lineColor = spectrum.color
                    }
                    line(spectrum.name, spectrum.spectrum, spec = spec)
                }
            }
            plot("FFT") {
                graphs.forEach { spectrum ->
                    val spec = implotSpec {
                        if (spectrum.color != null) lineColor = spectrum.color
                    }
                    line(spectrum.name, spectrum.fft, spec = spec)
                }
            }
        }
    }
}