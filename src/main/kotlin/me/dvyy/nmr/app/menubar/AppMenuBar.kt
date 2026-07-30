package me.dvyy.nmr.app.menubar

import imgui.ImGui.*
import imgui.flag.ImGuiKey
import me.dvyy.nmr.app.bindings.imgui.ImGuiKt
import me.dvyy.nmr.app.nodes.ui.NodeGraphViewModel

fun ImGuiKt.AppMenuBar(
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

    menu("File") {
        if (menuItem("Save", "Ctrl+S")) nodeGraph.saveProject()
        if (menuItem("Load", "Ctrl+O")) nodeGraph.loadProject()
        if (menuItem("Exit")) menu.exit()
    }
    menu("View") {
        if (menuItem("Reset layout")) menu.first = true
        if (menuItem("Dark Mode", "", menu.isDarkMode)) menu.toggleDarkMode()
    }
}