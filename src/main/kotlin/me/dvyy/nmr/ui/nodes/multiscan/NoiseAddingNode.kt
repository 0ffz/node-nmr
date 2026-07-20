package me.dvyy.nmr.ui.nodes.multiscan

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import imgui.ImGui
import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.bindings.imgui.ImNodeContext
import me.dvyy.nmr.signal.Signal
import me.dvyy.nmr.signal.SignalSet
import me.dvyy.nmr.signal.SignalUiState
import me.dvyy.nmr.synthetic.addGaussianNoise
import me.dvyy.nmr.ui.nodes.InputAttribute
import me.dvyy.nmr.ui.nodes.Node
import me.dvyy.nmr.ui.nodes.OutputAttribute

class NoiseAddingNode: Node() {
    override val name: String = "Add noise"
    val input = inputAttribute<SignalUiState>()
    var noise by mutableStateOf(1.0)
    var numOutputs by mutableStateOf(10)

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
}

fun ImGuiKt.inputOutput(input: InputAttribute<*>, output: OutputAttribute<*>) {
    with(ImNodeContext) {
        inputAttribute(input.id) { text("In") }
        ImGui.sameLine()
        outputAttribute(output.id) { text("Out") }
    }
}
class SignalSelectNode: Node() {
    override val name: String = "Select signal"
    var selected by mutableStateOf(0)
    val input = inputAttribute<SignalSet?>()
    val output = outputAttribute<SignalUiState?> {
        input.value?.signals?.getOrNull(selected)?.let { SignalUiState(it) }
    }
    override fun ImGuiKt.draw() {
        inputOutput(input, output)
        val size = input.value?.signals?.size ?: return
        sliderInt("select", selected, min = 0, max = size - 1, onChange = { selected = it })
    }
}