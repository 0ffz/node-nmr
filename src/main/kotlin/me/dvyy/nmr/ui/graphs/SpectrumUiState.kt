package me.dvyy.nmr.ui.graphs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import imgui.ImVec4
import me.dvyy.nmr.complex.ComplexDoubleArray

/**
 * UI State for the real part of a complex NMR signal, processed or unprocessed.
 */
data class SpectrumUiState(
    val spectrumUnprocessed: ComplexDoubleArray,
    var fft: DoubleArray,
    var waveletRe: DoubleArray = doubleArrayOf(),
    var waveletIm: DoubleArray = doubleArrayOf(),
    val name: String,
    val color: ImVec4? = null,
    var lb: Double,
    var p0: Double,
    var p1: Double,
    var gauss: Double? = null,
    var offset: Double = 0.0,
    var scale: Double = 0.0,
) {
    var visible: Boolean by mutableStateOf(true)

}

//class SpectrumGraph(state: SpectrumUiState) {
//    var uiState by mutableStateOf(state)
//    private val processing = ComplexDoubleArray(state.spectrumUnprocessed.size)
//    private val spectrumGraph = ComplexDoubleArray(state.spectrumUnprocessed.size)
//    val spectrum by derivedStateOf {
//        state.spectrumUnprocessed.data.copyInto(processing.data)
//        state.processing.expApodized(state.lb)
//////        if (spectrum.gauss != null)
//////            spectrum.processing.gaussApodization(spectrum.gauss!!, spectrum.processing)
////        spectrum.spectrum.asF64Array().let { it /= it.max() }
////        calculateFFT(spectrum)
//        processing
//    }
//}
