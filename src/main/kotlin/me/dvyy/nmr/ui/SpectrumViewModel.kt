package me.dvyy.nmr.ui

import androidx.compose.runtime.*
import imgui.ImVec4
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.dvyy.nmr.bindings.fftw.FftwComplexArray
import me.dvyy.nmr.bindings.fftw.FftwDirection
import me.dvyy.nmr.bindings.fftw.FftwFlag
import me.dvyy.nmr.bindings.fftw.FftwPlan1D
import me.dvyy.nmr.bindings.helpers.memScoped
import me.dvyy.nmr.bindings.propack.propack
import me.dvyy.nmr.bindings.wavelib.StationaryWaveletTransform
import me.dvyy.nmr.complex.ComplexDoubleArray
import me.dvyy.nmr.parsing.BrukerDataset
import me.dvyy.nmr.parsing.removeDigitalFilter
import me.dvyy.nmr.phasecorrect.findOptimalPhaseParameters
import me.dvyy.nmr.phasecorrect.phaseCorrect
import me.dvyy.nmr.signal.expApodized
import me.dvyy.nmr.signal.fftShift
import me.dvyy.nmr.svd.HankelOperator
import me.dvyy.nmr.svd.reconstructDiagonals
import me.dvyy.nmr.ui.graphs.GraphType
import me.dvyy.nmr.ui.graphs.SpectrumUiState
import me.dvyy.nmr.ui.processing.DenoiserControlsUiState
import org.jetbrains.bio.viktor.asF64Array

@Deprecated("")
class SpectrumViewModel(
    val scope: CoroutineScope,
) {
    private var lastSpectrum = 0
    var spectra by mutableStateOf(persistentListOf<SpectrumUiState>())
    var svdResults = mutableStateListOf<DoubleArray>()
    val visibleSpectra by derivedStateOf {
        println("Visible spectra updated!")
        spectra.filter { it.visible }
    }
    val controls = DenoiserControlsUiState()
    var first = true
    var selectedSpectrum = 0
    var graphType by mutableStateOf<GraphType>(GraphType.FFT)

    fun loadSpectrum(
        name: String,
        dataset: BrukerDataset,
        color: ImVec4? = null,
    ) {
        val procs = dataset.procs
        val totalSpectralWidth = procs["SW_p"]!!.toDouble()
        val spectrometerFrequency = procs["SF"]!!.toDouble()
        val points = dataset.readFid().removeDigitalFilter(dataset.acqus)
        // start at offset
        val offset = (procs["OFFSET"]?.toDouble() ?: 0.0)
        val widthPPM = totalSpectralWidth / spectrometerFrequency
        val end = offset - widthPPM
        // end at offset - SW_p / SF
        val scale = widthPPM / points.size
        TODO()
//        loadSpectrum(
//            name,
//            points,
//            p0 = procs["PHC0"]?.toDouble() ?: 0.0,
//            p1 = procs["PHC1"]?.toDouble() ?: 0.0,
//            offset = offset,
//            scale = -scale,
//            color = color
//        )
    }

}
