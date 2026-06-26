package me.dvyy.nmr.ui

import me.dvyy.nmr.bindings.imgui.ImGuiKt

fun ImGuiKt.ControlScreen(state: AppUiState) {
    val spectrum = state.spectra.value.first()
    sliderDouble("lb", spectrum.lb, -0.001, 0.01, onChange = { spectrum.lb = it })
    sliderDouble("gauss", spectrum.gauss ?: 0.0, -0.0001, 0.001, onChange = { spectrum.gauss = it })
    sliderInt("singularValues", state.controls.numSingularValues, 1, 100, onChange = { state.controls.numSingularValues = it })
    sliderDouble("p0", spectrum.p0, -180.0, 180.0, onChange = { spectrum.p0 = it })
    button("Apply preprocessing") {
        state.update(spectrum)
    }
    button("Run SVD") {
        state.generateSVD(spectrum)
    }
}