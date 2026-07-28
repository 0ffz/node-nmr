package me.dvyy.nmr.ui.menubar

import imgui.ImGui.*
import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.ui.SpectrumViewModel

fun ImGuiKt.AppMenuBar(
    state: SpectrumViewModel,
    menu: MenuViewModel
) {
    menu("File") {
        if (menuItem("Open")) menu.openFilePicker()
        if (menuItem("Open Multiple")) menu.openMultiFilePicker()
        if (menuItem("Exit")) menu.exit()
    }
    menu("View") {
        if (menuItem("Reset layout")) state.first = true
        if (menuItem("Dark Mode", "", menu.isDarkMode)) menu.toggleDarkMode()
    }
}