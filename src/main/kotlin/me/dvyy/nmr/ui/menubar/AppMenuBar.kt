package me.dvyy.nmr.ui.menubar

import imgui.ImGui.*
import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.ui.SpectrumViewModel

fun ImGuiKt.AppMenuBar(
    state: SpectrumViewModel,
    menu: MenuViewModel
) {
    menu("File") {
        if (menuItem("Exit")) menu.exit()
        if (menuItem("Open")) menu.openFilePicker()
    }
    menu("View") {
        if (menuItem("Reset layout")) state.first = true
    }
}