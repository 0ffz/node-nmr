package me.dvyy.nmr

import imgui.ImFontConfig
import imgui.ImGui
import imgui.app.Application
import imgui.app.Configuration
import imgui.extension.implot.ImPlot
import imgui.flag.ImGuiConfigFlags
import me.dvyy.nmr.bindings.fftw.FftwComplexArray
import me.dvyy.nmr.bindings.fftw.FftwDirection
import me.dvyy.nmr.bindings.fftw.FftwFlag
import me.dvyy.nmr.bindings.fftw.FftwPlan1D
import me.dvyy.nmr.bindings.helpers.memScoped
import me.dvyy.nmr.bindings.propack.propack
import me.dvyy.nmr.complex.ComplexDoubleArray
import me.dvyy.nmr.complex.takeComplex
import me.dvyy.nmr.parsing.BrukerDataset
import me.dvyy.nmr.parsing.removeDigitalFilter
import me.dvyy.nmr.signal.expApodization
import me.dvyy.nmr.signal.fftShift
import me.dvyy.nmr.svd.HankelOperator
import me.dvyy.nmr.svd.HankelOperatorBruteForce
import me.dvyy.nmr.svd.reconstructDiagonals
import me.dvyy.nmr.ui.AppUiState
import me.dvyy.nmr.ui.graphs.GraphScreen
import org.jetbrains.bio.viktor.asF64Array

class Main : Application() {
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

    }

    fun init() {
        val brukerData = BrukerDataset("/var/home/offz/projects/nmr-kotlin/data/1d_carbon_ML/10")
        val cleanData = BrukerDataset("/var/home/offz/projects/nmr-kotlin/data/1d_carbon_ML/5")
        val cleanFid = cleanData.readFid()
            .removeDigitalFilter(brukerData.acqus)
            .takeComplex(2048)
            .expApodization(0.00008)

//            .expApodization(0.0005)
        // 1. You can freely inspect text parameters
        println("Pulse Program: ${brukerData.acqus["PULPROG"]}")

        println("Spectrometer Frequency: ${brukerData.acqus["SFO1"]} MHz")
        // 2. Load the 1D FID
        val fid = brukerData.readFid()
            .removeDigitalFilter(brukerData.acqus)
            .takeComplex(2048)
            .expApodization(0.00005)
        val rows = fid.size / 2
        val cols = fid.size - rows + 1
        val denoised = memScoped {
//            val hankel = HankelOperator(this, fid.toMemorySegment(), rows, cols)
            val hankel = HankelOperatorBruteForce(fid.toMemorySegment())
            val result = propack(hankel, rows, cols, numWanted = 15)
            result.reconstructDiagonals()
        }
        state.loadSpectrum("Dirty", fid)
        state.loadSpectrum("Clean", cleanFid)
        state.loadSpectrum("Denoised", denoised)
    }

    val state = AppUiState()
    override fun process() {
        GraphScreen(state.spectra)
    }

}

fun main() {
    ImPlot.createContext()
    Application.launch(Main())
}
