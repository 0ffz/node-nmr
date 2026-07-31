package me.dvyy.nmr.app.nodes.data.multiscan

import me.dvyy.nmr.app.bindings.imgui.ImGuiKt
import me.dvyy.nmr.app.nodes.data.Node
import me.dvyy.nmr.app.nodes.data.NodeInfo
import me.dvyy.nmr.app.nodes.data.parameters.nodeState
import me.dvyy.nmr.app.nodes.ui.components.inputOutput
import me.dvyy.nmr.app.nodes.ui.state.SignalUiState
import me.dvyy.nmr.processing.model.Signal
import me.dvyy.nmr.processing.model.SignalSet
import me.dvyy.nmr.processing.transform.addGaussianNoise

class NoiseAddingNode : Node() {
    val input = inputAttribute<SignalUiState>()
    var noise by nodeState(1.0)
    var numOutputs by nodeState(10)

    val output = outputAttribute {
        val signal = input.value ?: return@outputAttribute null
        SignalSet(
            Array(numOutputs) { index ->
                Signal.Fid(signal.signal.fid.addGaussianNoise(noise, seed = index.toLong()))
            }.toList()
        )
    }

    override fun ImGuiKt.draw() {
        inputOutput(input, output)
        dragDouble("noise", noise, onChange = { noise = it })
        sliderInt("numOutputs", numOutputs, min = 1, max = 100, onChange = { numOutputs = it })

    }

    companion object : NodeInfo<NoiseAddingNode> {
        override val name = "Add noise"
        override val category = "Multi signal"
        override val factory = ::NoiseAddingNode
    }
}

