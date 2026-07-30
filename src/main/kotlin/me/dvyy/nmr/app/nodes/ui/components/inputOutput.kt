package me.dvyy.nmr.app.nodes.ui.components

import imgui.ImGui
import me.dvyy.nmr.app.bindings.imgui.ImGuiKt
import me.dvyy.nmr.app.bindings.imgui.ImNodeContext
import me.dvyy.nmr.ui.nodes.InputAttribute
import me.dvyy.nmr.ui.nodes.OutputAttribute

fun ImGuiKt.inputOutput(input: InputAttribute<*>, output: OutputAttribute<*>) {
    with(ImNodeContext) {
        inputAttribute(input.id) { text("In") }
        ImGui.sameLine()
        outputAttribute(output.id) { text("Out") }
    }

}