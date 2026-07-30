package me.dvyy.nmr.app.nodes.data.inputs

import me.dvyy.nmr.app.bindings.imgui.ImGuiKt
import me.dvyy.nmr.app.bindings.imgui.ImNodeContext
import me.dvyy.nmr.app.nodes.data.Node
import me.dvyy.nmr.common.math.ComplexDoubleArray
import me.dvyy.nmr.io.bruker.BrukerDataset
import me.dvyy.nmr.io.bruker.removeDigitalFilter
import me.dvyy.nmr.processing.model.Signal
import me.dvyy.nmr.processing.model.SignalSet
import org.jetbrains.bio.viktor.asF64Array

class MultiDatasetNode(
    val datasets: List<BrukerDataset>,
) : Node() {
    val output = outputAttribute<SignalSet> {
        val data = datasets.map {
            it.readFid().removeDigitalFilter(it.acqus).apply {
                data.asF64Array().let { it /= it.max() }
            }
        }
        SignalSet(data.map { Signal.Fid(it) })
    }

    override fun ImGuiKt.draw() {
        with(ImNodeContext) {
            outputAttribute(output.id) {
                text("Out")
            }
        }
    }
}

class Dataset2DNode(
    val dataset: BrukerDataset,
) : Node() {
    val output = outputAttribute<List<ComplexDoubleArray>> {
        dataset.readSer().map { it.removeDigitalFilter(dataset.acqus) }
    }

    override fun ImGuiKt.draw() {
        with(ImNodeContext) {
            outputAttribute(output.id) {
                text("Out")
            }
        }
    }
}