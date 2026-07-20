package me.dvyy.nmr.ui.nodes.multiscan

import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.complex.ComplexDoubleArray
import me.dvyy.nmr.complex.j
import me.dvyy.nmr.signal.Signal
import me.dvyy.nmr.signal.SignalSet
import me.dvyy.nmr.signal.SignalUiState
import me.dvyy.nmr.ui.nodes.Node

class MultiScanAverage(): Node() {
    override val name: String = "Average"
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
}