package me.dvyy.nmr.ui.nodes.inputs

import kotlinx.io.files.Path
import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.bindings.imgui.ImNodeContext
import me.dvyy.nmr.parsing.BrukerDataset
import me.dvyy.nmr.parsing.removeDigitalFilter
import me.dvyy.nmr.phasecorrect.findOptimalPhaseParameters
import me.dvyy.nmr.signal.Signal
import me.dvyy.nmr.signal.SignalUiState
import me.dvyy.nmr.ui.nodes.Node
import me.dvyy.nmr.ui.nodes.nodeState
import org.jetbrains.bio.viktor.asF64Array
import kotlin.io.path.absolutePathString

class DatasetNode(
    val dataset: BrukerDataset,
) : Node() {
    override val name: String = dataset.name
    val path by nodeState<String>(dataset.path.absolutePathString())
    val numSamples = dataset.numSamples
    //TODO correctly store dataset reference
    var offset by nodeState(dataset.offset)
    val output = outputAttribute {
        val data = dataset.readFid().removeDigitalFilter(dataset.acqus)
        data.data.asF64Array().let { it /= it.max() }
        val fid = Signal.Fid(data)
        SignalUiState(
            signal = fid,
            offset = offset,
            widthPPM = dataset.widthPPM,
            phaseParams = data.findOptimalPhaseParameters()
        )
    }

    override fun ImGuiKt.draw() {
        with(ImNodeContext) {
            outputAttribute(output.id) {
                text("Out")
            }
        }
        text("Samples: $numSamples")
        dragDouble("offset", offset, onChange = { offset = it })
    }
}