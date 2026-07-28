package me.dvyy.nmr.ui.nodes.multiscan

import imgui.ImGui
import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.bindings.imgui.ImNodeContext
import me.dvyy.nmr.ui.nodes.InputAttribute
import me.dvyy.nmr.ui.nodes.OutputAttribute

fun ImGuiKt.inputOutput(input: InputAttribute<*>, output: OutputAttribute<*>) {
    with(ImNodeContext) {
        inputAttribute(input.id) { text("In") }
        ImGui.sameLine()
        outputAttribute(output.id) { text("Out") }
    }

}