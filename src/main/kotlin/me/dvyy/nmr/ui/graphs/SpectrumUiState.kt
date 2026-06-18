package me.dvyy.nmr.ui.graphs

/**
 * UI State for the real part of a complex NMR signal, processed or unprocessed.
 */
class SpectrumUiState(
    val name: String,
    val spectrum: DoubleArray,
    val fft: DoubleArray,
) {

}