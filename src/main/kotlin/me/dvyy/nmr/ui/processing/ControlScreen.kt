package me.dvyy.nmr.ui.processing

import imgui.ImGui
import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.ui.SpectrumViewModel

fun ImGuiKt.ControlScreen(state: SpectrumViewModel) {
    val spectra = state.spectra
    val selectedIndex = state.selectedSpectrum
    val spectrum = spectra.getOrNull(selectedIndex) ?: return
    if (ImGui.beginCombo("Spectrum", spectrum.name)) {
        spectra.forEachIndexed { index, spectrum ->
            if (ImGui.selectable(spectrum.name, index == selectedIndex)) {
                state.selectedSpectrum = index
            }
        }
        ImGui.endCombo()
    }

    section("Preprocessing") {
        sliderDouble("lb", spectrum.lb, -0.001, 0.01, onChange = {
            spectrum.lb = it
            state.update(spectrum)
        })
        sliderDouble("gauss", spectrum.gauss ?: 0.0, -0.0001, 0.001, onChange = { spectrum.gauss = it })
//        sliderDouble("p0", spectrum.p0, -180.0, 180.0, onChange = { spectrum.p0 = it })
    }


    section("SVD") {
        sliderInt("singularValues", state.controls.numSingularValues, 1, 100, onChange = { state.controls.numSingularValues = it })
        button("Run SVD") {
            state.generateSVD(spectrum)
        }
    }

    section("Copy") {
        button("Make copy from current view") {
            val type = state.graphType
            state.copy(spectrum, type)
        }
    }
}
