package me.dvyy.nmr.ui.nodes.outputs

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import imgui.ImGui
import imgui.flag.ImGuiColorEditFlags
import imgui.type.ImBoolean
import imgui.type.ImString
import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.bindings.imgui.ImNodeContext
import me.dvyy.nmr.signal.SignalUiState
import me.dvyy.nmr.ui.nodes.Node
import me.dvyy.nmr.ui.nodes.GraphEmitting
import me.dvyy.nmr.ui.nodes.GraphUiState
import java.awt.Color

class GraphNode : Node(), GraphEmitting {
    override val name: String = "Graph"
    var title by mutableStateOf("Untitled")
    var color by mutableStateOf<Color?>(null)
    val string = ImString(title, 42)
    var autoPhase by mutableStateOf(false)
    val input = inputAttribute<SignalUiState?>()

    override fun ImGuiKt.draw() {
        if (ImGui.inputText("Title", string)) {
            println("Text changed")
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
    }

    override val graph: GraphUiState? by derivedStateOf {
        val input = input.value ?: return@derivedStateOf null
        GraphUiState(title, input, color)
    }
}