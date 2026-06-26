package me.dvyy.nmr.ui.graphs

import imgui.ImGui.*
import imgui.flag.ImGuiStyleVar
import imgui.flag.ImGuiWindowFlags
import kotlinx.collections.immutable.ImmutableList
import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.bindings.imgui.implotSpec

object ImplotSubplotFlags {
    val SHARE_ITEMS = 1 shl 5
}

fun ImGuiKt.GraphScreen(graphs: ImmutableList<SpectrumUiState>) {
    val viewport = getMainViewport()
    setNextWindowPos(viewport.posX, viewport.posY)
    setNextWindowSize(viewport.sizeX, viewport.sizeY)

    pushStyleVar(ImGuiStyleVar.WindowBorderSize, 0.0f)
    window("Graphs", ImGuiWindowFlags.NoTitleBar or ImGuiWindowFlags.NoResize or ImGuiWindowFlags.NoMove or ImGuiWindowFlags.NoBringToFrontOnFocus) {
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
                graphs.forEach { spectrum ->
                    val spec = implotSpec {
                        if (spectrum.color != null) lineColor = spectrum.color
                    }
                    line(spectrum.name, spectrum.fft, xStart = spectrum.offset, xScale = spectrum.scale, spec = spec)
                }
            }
        }
    }
    popStyleVar()
}