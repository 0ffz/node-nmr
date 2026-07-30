package me.dvyy.nmr.ui.menubar

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import imgui.extension.imnodes.ImNodes
import imgui.extension.implot.ImPlot
import kotlinx.coroutines.CoroutineScope
import me.dvyy.nmr.ui.nodes.NodeGraphViewModel
import kotlin.system.exitProcess

class MenuViewModel(
    val scope: CoroutineScope,
    val graph: NodeGraphViewModel,
) {
    var first by mutableStateOf(true)
    var isDarkMode by mutableStateOf(false)

    fun toggleDarkMode() {
        isDarkMode = !isDarkMode
        updateColors()
    }

    fun updateColors() {
        if (isDarkMode) {
            imgui.ImGui.styleColorsDark()
            ImPlot.styleColorsDark()
            ImNodes.styleColorsDark()
        } else {
            imgui.ImGui.styleColorsLight()
            ImNodes.styleColorsLight()
            ImPlot.styleColorsAuto()
        }
    }

    fun exit() {
        exitProcess(0)
    }
}