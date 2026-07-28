package me.dvyy.nmr.ui.nodes.outputs

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import imgui.ImGui
import imgui.flag.ImGuiColorEditFlags
import imgui.type.ImBoolean
import imgui.type.ImString
import kotlinx.serialization.builtins.nullable
import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.bindings.imgui.ImNodeContext
import me.dvyy.nmr.phasecorrect.findOptimalPhaseParameters
import me.dvyy.nmr.signal.SignalUiState
import me.dvyy.nmr.ui.nodes.*
import java.awt.Color

import me.dvyy.nmr.ui.nodes.NodeInfo

class GraphNode : Node(), GraphEmitting {
    var title by nodeState("Untitled")
    var color by nodeState<Color?>(null, serializer = ColorSerializer.nullable)
    val string = ImString(title, 42)
    var autoPhase by nodeState(false)
    var yOffset by nodeState(0.0)
    val input = inputAttribute<SignalUiState?>()

    override fun ImGuiKt.draw() {
        if (ImGui.inputText("Title", string)) {
            title = string.get()
        }
        with(ImNodeContext) {
            inputAttribute(input.id) { text("Input") }
        }
        colorEdit4("Color", color ?: Color.BLACK, onChange = { color = it }, flags = ImGuiColorEditFlags.NoInputs)
        val bool = ImBoolean(autoPhase)
        if (ImGui.checkbox("Auto Phase", bool)) {
            autoPhase = bool.get()
        }
        dragDouble("Y Offset", yOffset, scaleNearZero = false, onChange = { yOffset = it })
    }

    override val graph: GraphUiState? by derivedStateOf {
        val input = input.value ?: return@derivedStateOf null
        val phased = if (autoPhase) {
            input.signal.fft.findOptimalPhaseParameters()
        } else input.phaseParams
        GraphUiState(title, input.copy(phaseParams = phased, yOffset = yOffset), color)
    }

    companion object : NodeInfo<GraphNode> {
        override val name = "Graph"
        override val factory = ::GraphNode
    }
}
