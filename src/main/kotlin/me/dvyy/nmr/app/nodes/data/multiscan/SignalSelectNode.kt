package me.dvyy.nmr.app.nodes.data.multiscan

import me.dvyy.nmr.app.bindings.imgui.ImGuiKt
import me.dvyy.nmr.app.nodes.data.Node
import me.dvyy.nmr.app.nodes.data.NodeInfo
import me.dvyy.nmr.app.nodes.data.parameters.nodeState
import me.dvyy.nmr.app.nodes.ui.components.inputOutput
import me.dvyy.nmr.processing.model.SignalSet
import me.dvyy.nmr.app.nodes.ui.state.SignalUiState

class SignalSelectNode : Node() {
    var selected by nodeState(0)
    val input = inputAttribute<SignalSet?>()
    val output = outputAttribute<SignalUiState?> {
        input.value?.signals?.getOrNull(selected)?.let { SignalUiState(it) }
    }

    override fun ImGuiKt.draw() {
        inputOutput(input, output)
        val size = input.value?.signals?.size ?: return
        sliderInt("select", selected, min = 0, max = size - 1, onChange = { selected = it })
    }

    companion object : NodeInfo<SignalSelectNode> {
        override val name = "Select signal"
        override val factory = ::SignalSelectNode
    }
}