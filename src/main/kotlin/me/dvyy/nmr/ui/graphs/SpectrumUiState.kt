package me.dvyy.nmr.ui.graphs

import imgui.ImVec4
import me.dvyy.nmr.complex.ComplexDoubleArray

/**
 * UI State for the real part of a complex NMR signal, processed or unprocessed.
 */
class SpectrumUiState(
    val spectrumUnprocessed: ComplexDoubleArray,
    var fft: DoubleArray,
    val name: String,
    val color: ImVec4? = null,
    var lb: Double,
    var p0: Double,
    var p1: Double,
    var gauss: Double? = null,
    var offset: Double = 0.0,
    var scale: Double = 0.0,
) {
    val spectrum = DoubleArray(spectrumUnprocessed.size)
    val processing = ComplexDoubleArray(spectrumUnprocessed.size)
}