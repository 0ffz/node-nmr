package me.dvyy.nmr.app.nodes.ui.screens

import imgui.ImGui
import me.dvyy.nmr.app.bindings.imgui.ImGuiKt
import me.dvyy.nmr.app.nodes.data.Node
import me.dvyy.nmr.app.nodes.data.inputs.DatasetNode
import me.dvyy.nmr.app.nodes.data.inputs.MultiDatasetNode
import me.dvyy.nmr.app.nodes.data.inputs.SyntheticDataset
import me.dvyy.nmr.app.nodes.data.outputs.GraphNode
import me.dvyy.nmr.app.nodes.data.transformations.ApodizationNode
import me.dvyy.nmr.app.nodes.data.multiscan.MultiScanAverage
import me.dvyy.nmr.app.nodes.data.multiscan.MultiScanWaveletNode
import me.dvyy.nmr.app.nodes.data.multiscan.NoiseAddingNode
import me.dvyy.nmr.app.nodes.data.multiscan.SignalSelectNode
import me.dvyy.nmr.app.nodes.data.outputs.ExportNode
import me.dvyy.nmr.app.nodes.data.outputs.Graph2DNode
import me.dvyy.nmr.app.nodes.data.outputs.SSIMNode
import me.dvyy.nmr.app.nodes.data.transformations.PhaseCorrectTransformation
import me.dvyy.nmr.app.nodes.data.transformations.SVDCadzowFilter
import me.dvyy.nmr.app.nodes.data.transformations.WaveletTransformation
import me.dvyy.nmr.app.nodes.data.transformations.ZeroFillTransformation

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
        DragDropTransformationSource("Multi Dataset") { MultiDatasetNode() }
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
