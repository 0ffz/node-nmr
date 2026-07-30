package me.dvyy.nmr.app.nodes.data.multiscan

import me.dvyy.nmr.app.bindings.imgui.ImGuiKt
import me.dvyy.nmr.app.nodes.data.Node
import me.dvyy.nmr.app.nodes.data.NodeInfo
import me.dvyy.nmr.app.nodes.ui.components.inputOutput
import me.dvyy.nmr.app.nodes.ui.state.SignalUiState
import me.dvyy.nmr.common.math.ComplexDoubleArray
import me.dvyy.nmr.common.math.j
import me.dvyy.nmr.processing.model.Signal
import me.dvyy.nmr.processing.model.SignalSet

class MultiScanAverage : Node() {
    val input = inputAttribute<SignalSet?>()
    val output = outputAttribute<SignalUiState?> {
//        val sampleSignal = Array(10) {
//            input.addGaussianNoise(5.0).real()
//        }.toList()
        val inputs = input.value?.signals?.map { it.fid } ?: return@outputAttribute null
        val average = ComplexDoubleArray(inputs[0].size) { index ->
            val sum = inputs.fold(0.j) { acc, array -> acc + array[index] }
            sum / inputs.size
        }
        val signal = Signal.Fid(average)
        SignalUiState(signal)
    }

    override fun ImGuiKt.draw() {
        inputOutput(input, output)
    }

    companion object : NodeInfo<MultiScanAverage> {
        override val name = "Average"
        override val factory = ::MultiScanAverage
    }
}