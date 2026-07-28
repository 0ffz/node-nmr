package me.dvyy.nmr.ui.nodes.multiscan

import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.signal.Signal
import me.dvyy.nmr.signal.SignalSet
import me.dvyy.nmr.signal.SignalUiState
import me.dvyy.nmr.synthetic.addGaussianNoise
import me.dvyy.nmr.ui.nodes.Node
import me.dvyy.nmr.ui.nodes.NodeInfo
import me.dvyy.nmr.ui.nodes.nodeState

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
        override val factory = ::NoiseAddingNode
    }
}

