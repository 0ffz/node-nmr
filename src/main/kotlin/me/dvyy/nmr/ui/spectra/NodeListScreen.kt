package me.dvyy.nmr.ui.spectra

import imgui.ImGui
import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.ui.SpectrumViewModel
import me.dvyy.nmr.ui.nodes.Node
import me.dvyy.nmr.ui.nodes.inputs.SyntheticDataset
import me.dvyy.nmr.ui.nodes.outputs.GraphNode
import me.dvyy.nmr.ui.nodes.transformations.ApodizationNode
import me.dvyy.nmr.ui.nodes.multiscan.MultiScanAverage
import me.dvyy.nmr.ui.nodes.multiscan.MultiScanWaveletNode
import me.dvyy.nmr.ui.nodes.multiscan.NoiseAddingNode
import me.dvyy.nmr.ui.nodes.multiscan.SignalSelectNode

fun ImGuiKt.NodeListScreen(state: SpectrumViewModel) {
    val spectra = state.spectra
//    spectra.forEachIndexed { index, spectrum ->
//        SpectrumOptions(spectrum, onDelete = { state.deleteSpectrum(index)})
//    }


    collapsingHeader("Data sources") {
        DragDropTransformationSource("Synthetic") { SyntheticDataset() }
    }
    collapsingHeader("Outputs") {
        DragDropTransformationSource("Graph") { GraphNode() }
    }
    collapsingHeader("Transformations") {
        DragDropTransformationSource("Apodization") { ApodizationNode() }
//        DragDropTransformationSource("MultiScanWaveletDenoise") { MultiScanWaveletDenoise() }
//        DragDropTransformationSource("Zero-fill") { ZeroFillTransformation() }
//        DragDropTransformationSource("Phase") { PhaseCorrectTransformation() }
//        DragDropTransformationSource("Wavelet denoise") { WaveletTransformation() }
//        DragDropTransformationSource("SVDTransformation") { SVDTransformation() }
    }

    collapsingHeader("Multi signal") {
        DragDropTransformationSource("Add noise") { NoiseAddingNode() }
        DragDropTransformationSource("Select signal") { SignalSelectNode() }
        DragDropTransformationSource("Multi scan wavelet denoise") { MultiScanWaveletNode() }
        DragDropTransformationSource("Average") { MultiScanAverage() }
    }
}

fun ImGuiKt.DragDropTransformationSource(
    name: String,
    create: () -> Node,
) {
    ImGui.button(name)
    if (ImGui.beginDragDropSource()) {
        println("Began drag drop source!")
        ImGui.setDragDropPayload("node", create)
        ImGui.text(name)
        ImGui.endDragDropSource()
    }
}
