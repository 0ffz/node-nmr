package me.dvyy.nmr.app.nodes.data.inputs

import imgui.ImGui
import me.dvyy.nmr.app.bindings.imgui.ImGuiKt
import me.dvyy.nmr.app.bindings.imgui.ImNodeContext
import me.dvyy.nmr.app.nodes.data.Node
import me.dvyy.nmr.processing.model.Signal

class SignalNode(val signal: Signal) : Node() {
    val input = inputAttribute<Signal>()
    val output = outputAttribute {
        input.value
        signal
    }

    override fun ImGuiKt.draw() {
        with(ImNodeContext) {
            inputAttribute(input.id) { text("In") }
            ImGui.sameLine()
            outputAttribute(output.id) { text("Out") }
        }
    }
}