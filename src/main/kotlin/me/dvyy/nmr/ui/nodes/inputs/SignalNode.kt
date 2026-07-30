package me.dvyy.nmr.ui.nodes.inputs

import imgui.ImGui
import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.bindings.imgui.ImNodeContext
import me.dvyy.nmr.processing.model.Signal
import me.dvyy.nmr.ui.nodes.Node

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