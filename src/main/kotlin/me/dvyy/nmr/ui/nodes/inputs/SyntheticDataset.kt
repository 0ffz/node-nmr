package me.dvyy.nmr.ui.nodes.inputs

import androidx.compose.runtime.*
import imgui.ImGui
import imgui.flag.ImGuiTreeNodeFlags
import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.bindings.imgui.ImNodeContext
import me.dvyy.nmr.signal.Signal
import me.dvyy.nmr.signal.SignalUiState
import me.dvyy.nmr.synthetic.Resonance
import me.dvyy.nmr.synthetic.addGaussianNoise
import me.dvyy.nmr.synthetic.generateNmrSignal
import me.dvyy.nmr.ui.nodes.Node
import kotlin.random.Random

class SyntheticDataset : Node() {
    override val name: String = "Synthetic"
    var dwellTime by mutableStateOf(0.001)
    var peaks = mutableStateListOf(
        Resonance(amplitude = 5.0, frequencyHz = -50.0, phaseRadians = 0.0, t2StarSeconds = 0.1)
    )
    var noise by mutableStateOf(0.0)
    var seed by mutableStateOf(Random.nextLong())
    val fid by derivedStateOf {
        generateNmrSignal(
            16384,
            dwellTime,
            peaks,
        ).addGaussianNoise(noise, seed)
    }

    val output = outputAttribute {
        SignalUiState(Signal.Fid(fid))
    }

    fun ImGuiKt.resonance(resonance: Resonance, onChange: (Resonance) -> Unit) {
        dragDouble("amplitude", resonance.amplitude, onChange = { onChange(resonance.copy(amplitude = it)) })
        dragDouble("freq", resonance.frequencyHz, scaleNearZero = false, onChange = { onChange(resonance.copy(frequencyHz = it)) })
        dragDouble("phase", resonance.phaseRadians, onChange = { onChange(resonance.copy(phaseRadians = it)) })
        dragDouble("t2*", resonance.t2StarSeconds, scaleNearZero = false, onChange = { onChange(resonance.copy(t2StarSeconds = it)) })
    }

    override fun ImGuiKt.draw() {
        with(ImNodeContext) {
            outputAttribute(output.id) { text("Out") }
        }
        peaks.forEachIndexed { index, resonance ->
            treeNode("Peak $index", flags = ImGuiTreeNodeFlags.SpanTextWidth, header = {
                ImGui.sameLine()
                button(" - ##$index") { peaks.removeAt(index) }
            }) {
                resonance(resonance) {
                    peaks[index] = it
                }
            }
        }
        button("+", onClick = {
            peaks.add(
                Resonance(
                    amplitude = 5.0,
                    frequencyHz = -50.0,
                    phaseRadians = 0.0,
                    t2StarSeconds = 0.1
                )
            )
        })
        dragDouble("dwellTime", dwellTime, onChange = { dwellTime = it })
        dragDouble("noise", noise, onChange = { noise = it })
    }
}