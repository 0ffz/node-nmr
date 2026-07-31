package me.dvyy.nmr.app.nodes.data.outputs

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import imgui.ImGui
import imgui.flag.ImGuiColorEditFlags
import imgui.type.ImBoolean
import imgui.type.ImString
import kotlinx.serialization.builtins.nullable
import me.dvyy.nmr.app.bindings.imgui.ImGuiKt
import me.dvyy.nmr.app.bindings.imgui.ImNodeContext
import me.dvyy.nmr.app.graphs.data.GraphEmitting
import me.dvyy.nmr.app.graphs.ui.state.GraphUiState
import me.dvyy.nmr.app.nodes.data.Node
import me.dvyy.nmr.app.nodes.data.NodeInfo
import me.dvyy.nmr.app.nodes.data.parameters.nodeState
import me.dvyy.nmr.app.nodes.ui.state.SignalUiState
import me.dvyy.nmr.io.serializers.ColorSerializer
import me.dvyy.nmr.processing.transform.phase.findOptimalPhaseParameters
import java.awt.Color

class GraphNode : Node(), GraphEmitting {
    var title by nodeState("Untitled")
    var color by nodeState<Color?>(null, serializer = ColorSerializer.nullable)
    val string = ImString(title, 42)
    var autoPhase by nodeState(false)
    var yOffset by nodeState(0.0)
    val input = inputAttribute<SignalUiState?>()

    override fun ImGuiKt.draw() {
        string.set(title)
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
        override val category = "1D"
        override val subcategory = "Outputs"
        override val factory = ::GraphNode
    }
}
