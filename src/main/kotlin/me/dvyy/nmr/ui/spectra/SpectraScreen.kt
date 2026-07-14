package me.dvyy.nmr.ui.spectra

import imgui.ImGui
import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.ui.SpectrumViewModel
import me.dvyy.nmr.ui.graphs.SpectrumUiState
import me.dvyy.nmr.ui.nodes.transformations.ApodizationTransformation
import me.dvyy.nmr.ui.nodes.transformations.PhaseCorrectTransformation
import me.dvyy.nmr.ui.nodes.transformations.SVDTransformation
import me.dvyy.nmr.ui.nodes.transformations.SignalTransformation
import me.dvyy.nmr.ui.nodes.transformations.WaveletTransformation
import me.dvyy.nmr.ui.nodes.transformations.ZeroFillTransformation

fun ImGuiKt.SpectraScreen(state: SpectrumViewModel) {
    val spectra = state.spectra
//    spectra.forEachIndexed { index, spectrum ->
//        SpectrumOptions(spectrum, onDelete = { state.deleteSpectrum(index)})
//    }

    DragDropTransformationSource("Apodization") { ApodizationTransformation() }
    DragDropTransformationSource("Wavelet denoise") { WaveletTransformation() }
    DragDropTransformationSource("Zero-fill") { ZeroFillTransformation() }
    DragDropTransformationSource("Phase") { PhaseCorrectTransformation() }
    DragDropTransformationSource("SVDTransformation") { SVDTransformation() }
}

private inline fun ImGuiKt.SpectrumOptions(
    state: SpectrumUiState,
    onDelete: () -> Unit,
) {
    ImGui.checkbox(state.name, true)

    ImGui.sameLine(ImGui.getWindowWidth() - 40)
    // Delete
    button("${Icons.delete}##${state.name}") { onDelete() }

    ImGui.sameLine(ImGui.getWindowWidth() - 80)
    val icon = if(state.visible) '\ue8f4' else '\ue8f5'
    button("$icon##${state.name}") {
        state.visible = !state.visible
    }
//    ImGui.separator()
}

fun ImGuiKt.DragDropTransformationSource(
    name: String,
    create: () -> SignalTransformation,
) {
    ImGui.button(name)
    if (ImGui.beginDragDropSource()) {
        ImGui.setDragDropPayload("node", create())
        ImGui.text(name)
        ImGui.endDragDropSource()
    }
}
