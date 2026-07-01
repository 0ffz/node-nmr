package me.dvyy.nmr.ui.spectra

import imgui.ImGui
import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.ui.SpectrumViewModel
import me.dvyy.nmr.ui.graphs.SpectrumUiState

fun ImGuiKt.SpectraScreen(state: SpectrumViewModel) {
    val spectra = state.spectra
    spectra.forEachIndexed { index, spectrum ->
        SpectrumOptions(spectrum, onDelete = { state.deleteSpectrum(index)})
    }
}

private inline fun ImGuiKt.SpectrumOptions(
    state: SpectrumUiState,
    onDelete: () -> Unit,
) {
    ImGui.checkbox(state.name, true)

    ImGui.sameLine(ImGui.getWindowWidth() - 40)
    // Delete
    button("\ue872##${state.name}") { onDelete() }

    ImGui.sameLine(ImGui.getWindowWidth() - 80)
    val icon = if(state.visible) '\ue8f4' else '\ue8f5'
    button("$icon##${state.name}") {
        state.visible = !state.visible
    }
//    ImGui.separator()
}