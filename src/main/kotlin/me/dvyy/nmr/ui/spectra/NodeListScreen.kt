package me.dvyy.nmr.ui.spectra

import imgui.ImGui
import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.ui.nodes.Node
import me.dvyy.nmr.ui.nodes.inputs.DatasetNode
import me.dvyy.nmr.ui.nodes.inputs.SyntheticDataset
import me.dvyy.nmr.ui.nodes.outputs.GraphNode
import me.dvyy.nmr.ui.nodes.transformations.ApodizationNode
import me.dvyy.nmr.ui.nodes.multiscan.MultiScanAverage
import me.dvyy.nmr.ui.nodes.multiscan.MultiScanWaveletNode
import me.dvyy.nmr.ui.nodes.multiscan.NoiseAddingNode
import me.dvyy.nmr.ui.nodes.multiscan.SignalSelectNode
import me.dvyy.nmr.ui.nodes.outputs.ExportNode
import me.dvyy.nmr.ui.nodes.outputs.Graph2DNode
import me.dvyy.nmr.ui.nodes.outputs.SSIMNode
import me.dvyy.nmr.ui.nodes.transformations.PhaseCorrectTransformation
import me.dvyy.nmr.ui.nodes.transformations.SVDCadzowFilter
import me.dvyy.nmr.ui.nodes.transformations.WaveletTransformation
import me.dvyy.nmr.ui.nodes.transformations.ZeroFillTransformation

fun ImGuiKt.NodeListScreen() {
    collapsingHeader("1D", defaultOpen = true) {
        treeNode("Data sources", defaultOpen = true) {
            DragDropTransformationSource("Dataset") { DatasetNode() }
            DragDropTransformationSource("Synthetic") { SyntheticDataset() }
        }
        treeNode("Outputs##1D", defaultOpen = true) {
            DragDropTransformationSource("Graph") { GraphNode() }
            DragDropTransformationSource("SSIM") { SSIMNode() }
            DragDropTransformationSource("Export") { ExportNode() }
        }
        treeNode("Transformations", defaultOpen = true) {
            DragDropTransformationSource("Apodization") { ApodizationNode() }
            DragDropTransformationSource("Zero-fill") { ZeroFillTransformation() }
            DragDropTransformationSource("Phase") { PhaseCorrectTransformation() }
            DragDropTransformationSource("Wavelet denoise") { WaveletTransformation() }
            DragDropTransformationSource("Cadzow filter") { SVDCadzowFilter() }
        }

    }
    collapsingHeader("2D") {
        treeNode("Outputs##2D") {
            DragDropTransformationSource("Graph 2D") { Graph2DNode() }
        }
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
        ImGui.setDragDropPayload("node", create)
        ImGui.text(name)
        ImGui.endDragDropSource()
    }
}
