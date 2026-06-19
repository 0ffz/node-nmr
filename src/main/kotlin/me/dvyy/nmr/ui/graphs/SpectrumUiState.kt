package me.dvyy.nmr.ui.graphs

import imgui.ImVec4

/**
 * UI State for the real part of a complex NMR signal, processed or unprocessed.
 */
class SpectrumUiState(
    val name: String,
    val spectrum: DoubleArray,
    val fft: DoubleArray,
    val color: ImVec4? = null,
) {

}