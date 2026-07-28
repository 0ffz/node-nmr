package me.dvyy.nmr.ui.nodes.inputs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.bindings.imgui.ImNodeContext
import me.dvyy.nmr.parsing.BrukerDataset
import me.dvyy.nmr.parsing.removeDigitalFilter
import me.dvyy.nmr.phasecorrect.findOptimalPhaseParameters
import me.dvyy.nmr.signal.Signal
import me.dvyy.nmr.signal.SignalUiState
import me.dvyy.nmr.ui.nodes.Node
import org.jetbrains.bio.viktor.asF64Array

class DatasetNode(
    val dataset: BrukerDataset,
) : Node() {
    override val name: String = dataset.name
    var offset by mutableStateOf(dataset.offset)
val numSamples = dataset.numSamples
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