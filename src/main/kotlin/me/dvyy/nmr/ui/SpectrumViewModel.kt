package me.dvyy.nmr.ui

import androidx.compose.runtime.*
import imgui.ImVec4
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope
import me.dvyy.nmr.parsing.BrukerDataset
import me.dvyy.nmr.parsing.removeDigitalFilter
import me.dvyy.nmr.ui.graphs.GraphType
import me.dvyy.nmr.ui.graphs.SpectrumUiState

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
    var first = true
    var graphType by mutableStateOf<GraphType>(GraphType.FFT)

    fun loadSpectrum(
        name: String,
        dataset: BrukerDataset,
        color: ImVec4? = null,
    ) {
        val procs = dataset.procs
        val totalSpectralWidth = dataset.totalSpectralWidth
        val spectrometerFrequency = dataset.spectrometerFrequency
        val offset = dataset.offset
        val points = dataset.readFid().removeDigitalFilter(dataset.acqus)
        // start at offset
        val widthPPM = totalSpectralWidth / spectrometerFrequency
        val end = offset - widthPPM
        // end at offset - SW_p / SF
        val scale = widthPPM / points.size
        TODO()
    }

}
