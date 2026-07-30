package me.dvyy.nmr.ui.nodes.inputs

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import imgui.ImGui
import imgui.flag.ImGuiTreeNodeFlags
import kotlinx.collections.immutable.persistentListOf
import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.bindings.imgui.ImNodeContext
import me.dvyy.nmr.processing.model.Signal
import me.dvyy.nmr.processing.synthetic.Resonance
import me.dvyy.nmr.processing.synthetic.SyntheticSpectrum
import me.dvyy.nmr.signal.SignalUiState
import me.dvyy.nmr.processing.transform.addGaussianNoise
import me.dvyy.nmr.ui.nodes.Node
import me.dvyy.nmr.ui.nodes.NodeInfo
import me.dvyy.nmr.ui.nodes.PersistentListSerializer
import me.dvyy.nmr.ui.nodes.nodeState
import kotlin.random.Random

class SyntheticDataset : Node() {
    var dwellTime by nodeState(0.001)
    var peaks by nodeState(
        persistentListOf(Resonance(amplitude = 5.0, frequencyHz = -50.0, phaseRadians = 0.0, t2StarSeconds = 0.1)),
        serializer = PersistentListSerializer(Resonance.serializer())
    )
    var noise by nodeState(0.0)
    var seed by nodeState(Random.nextLong())

    val fid by derivedStateOf {
        SyntheticSpectrum.of(
            numPoints = 16384,
            dwellTimeSeconds = dwellTime,
            resonances = peaks,
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
                button(" - ##$index") { peaks = peaks.removeAt(index) }
            }) {
                resonance(resonance, onChange = {
                    peaks = peaks.set(index, it)
                })
            }
        }
        button("+", onClick = {
            peaks = peaks.add(
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

    companion object : NodeInfo<SyntheticDataset> {
        override val name = "Synthetic"
        override val factory = ::SyntheticDataset
    }
}