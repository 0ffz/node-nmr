package me.dvyy.nmr

import imgui.ImFontConfig
import imgui.ImGui
import imgui.ImGuiStyle
import imgui.ImVec2
import imgui.app.Application
import imgui.app.Configuration
import imgui.extension.implot.ImPlot
import imgui.flag.ImGuiConfigFlags
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.parsing.BrukerDataset
import me.dvyy.nmr.ui.AppUiState
import me.dvyy.nmr.ui.Colors
import me.dvyy.nmr.ui.ControlScreen
import me.dvyy.nmr.ui.graphs.GraphScreen

class Main : Application() {
    val scope = CoroutineScope(Dispatchers.IO)
    val state = AppUiState(scope)

    override fun initImGui(config: Configuration?) {
        super.initImGui(config)
        val io = ImGui.getIO()
        io.iniFilename = null                                // We don't want to save .ini file
        io.fonts.setFreeTypeRenderer(true)
        io.fonts.addFontFromMemoryTTF(loadFromResources("/NotoSans.ttf"), 24f, ImFontConfig())
        io.fonts.build()
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard)  // Enable Keyboard Controls
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable)      // Enable Docking
        io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable)
        init()
        ImGui.styleColorsDark(ImGuiStyle().apply {
            framePadding = ImVec2()
        })

    }

    fun init() {
        val brukerData = BrukerDataset("/var/home/offz/projects/nmr-kotlin/data/13C_lowsignal/27")
        val cleanData = BrukerDataset("/var/home/offz/projects/nmr-kotlin/data/13C_lowsignal/28")
        println("Pulse Program: ${brukerData.acqus["PULPROG"]}")
        println("Spectrometer Frequency: ${brukerData.acqus["SFO1"]} MHz")


        state.loadSpectrum("Dirty", brukerData, color = Colors.backgroundGray)
        state.loadSpectrum("Clean", cleanData)
    }

    override fun process() = with(ImGuiKt) {
        GraphScreen(state.spectra.value)
        window("Controls") {
            ControlScreen(state)
        }
    }
}

fun main() {
    ImPlot.createContext()
    Application.launch(Main())
}
