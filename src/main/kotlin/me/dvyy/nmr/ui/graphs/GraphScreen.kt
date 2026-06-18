package me.dvyy.nmr.ui.graphs

import imgui.ImGui.*
import imgui.extension.implot.ImPlot.*
import imgui.flag.ImGuiWindowFlags

fun GraphScreen(graphs: List<SpectrumUiState>) {
    val viewport = getMainViewport()
    setNextWindowPos(viewport.posX, viewport.posY)
    setNextWindowSize(viewport.sizeX, viewport.sizeY)


    if (begin("Graphs", ImGuiWindowFlags.NoDecoration or ImGuiWindowFlags.NoMove or ImGuiWindowFlags.NoResize)) {
        val height = getContentRegionAvailY() / 2f
        if (beginPlot("Spectra", getContentRegionAvailX(), height)) {
            graphs.forEach { spectrum ->
                plotLine(spectrum.name, spectrum.spectrum)
            }
        }
        endPlot()
        if (beginPlot("FFT", getContentRegionAvailX(), height)) {
            graphs.forEach { spectrum ->
                plotLine(spectrum.name, spectrum.fft)
            }
        }
        endPlot()
    }
    end()
}