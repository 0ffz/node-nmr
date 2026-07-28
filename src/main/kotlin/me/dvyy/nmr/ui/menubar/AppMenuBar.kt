package me.dvyy.nmr.ui.menubar

import imgui.ImGui.*
import imgui.flag.ImGuiKey
import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.ui.SpectrumViewModel
import me.dvyy.nmr.ui.nodes.NodeGraphViewModel

fun ImGuiKt.AppMenuBar(
    state: SpectrumViewModel,
    menu: MenuViewModel,
    nodeGraph: NodeGraphViewModel
) {
    val io = getIO()
    if (io.keyCtrl && isKeyPressed(ImGuiKey.S)) {
        nodeGraph.saveProject()
    }
    if (io.keyCtrl && isKeyPressed(ImGuiKey.O)) {
        nodeGraph.loadProject()
    }

    menu("Project") {
        if (menuItem("Save", "Ctrl+S")) nodeGraph.saveProject()
        if (menuItem("Load", "Ctrl+O")) nodeGraph.loadProject()
    }
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