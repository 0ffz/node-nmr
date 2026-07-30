package me.dvyy.nmr.app.menubar

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import imgui.ImGui
import imgui.extension.imnodes.ImNodes
import imgui.extension.implot.ImPlot
import kotlin.system.exitProcess

class MenuViewModel {
    var first by mutableStateOf(true)
    var isDarkMode by mutableStateOf(false)

    fun toggleDarkMode() {
        isDarkMode = !isDarkMode
        updateColors()
    }

    fun updateColors() {
        if (isDarkMode) {
            ImGui.styleColorsDark()
            ImPlot.styleColorsDark()
            ImNodes.styleColorsDark()
        } else {
            ImGui.styleColorsLight()
            ImNodes.styleColorsLight()
            ImPlot.styleColorsAuto()
        }
    }

    fun exit() {
        exitProcess(0)
    }
}