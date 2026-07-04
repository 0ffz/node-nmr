package me.dvyy.nmr.ui

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
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
import me.dvyy.nmr.complex.ComplexDoubleArray
import me.dvyy.nmr.parsing.BrukerDataset
import me.dvyy.nmr.parsing.removeDigitalFilter
import me.dvyy.nmr.phasecorrect.autoPhaseSpectrum
import me.dvyy.nmr.phasecorrect.phaseCorrect
import me.dvyy.nmr.signal.expApodized
import me.dvyy.nmr.signal.fftShift
import me.dvyy.nmr.svd.HankelOperator
import me.dvyy.nmr.svd.reconstructDiagonals
import me.dvyy.nmr.ui.graphs.SpectrumUiState
import me.dvyy.nmr.ui.processing.DenoiserControlsUiState
import org.jetbrains.bio.viktor.asF64Array

class SpectrumViewModel(
    val scope: CoroutineScope,
) {
    private var lastSpectrum = 0
    var spectra by mutableStateOf(persistentListOf<SpectrumUiState>())
    var svdResults = mutableStateListOf<DoubleArray>()
    val visibleSpectra by derivedStateOf { spectra.filter { it.visible } }
    val controls = DenoiserControlsUiState()
    var first = true
    var selectedSpectrum = 0

    fun update(
        spectrum: SpectrumUiState,
    ) {
        spectrum.spectrumUnprocessed.data.copyInto(spectrum.processing.data)
        spectrum.processing.expApodized(spectrum.lb)
//        if (spectrum.gauss != null)
//            spectrum.processing.gaussApodization(spectrum.gauss!!, spectrum.processing)
        for (i in spectrum.spectrum.indices) {
            spectrum.spectrum[i] = spectrum.processing.getRe(i)
        }
        spectrum.spectrum.asF64Array().let { it /= it.max() }
        calculateFFT(spectrum)

    }

    fun calculateFFT(
        spectrum: SpectrumUiState,
    ) {
        val fft = memScoped {
            val data = spectrum.processing.data
            val size = spectrum.processing.size
            val input = FftwComplexArray.alloc(size)
            val output = FftwComplexArray.alloc(size)
            val plan = FftwPlan1D(size, input, output, FftwDirection.FORWARD, FftwFlag.ESTIMATE)
            input.loadInterleaved(data)
            plan.execute()
            val unphased = ComplexDoubleArray(output.toInterleavedArray()).fftShift()
            val (p0, p1) = autoPhaseSpectrum(unphased)
            unphased
                .phaseCorrect(p0, p1)
                .real()
//                .reversedArray()
        }
        fft.asF64Array().let { it /= it.max() }
        spectrum.fft = fft
    }

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
        loadSpectrum(
            name,
            points,
            p0 = procs["PHC0"]?.toDouble() ?: 0.0,
            p1 = procs["PHC1"]?.toDouble() ?: 0.0,
            offset = -offset,
            scale = -scale,
            color = color
        )
    }

    fun deleteSpectrum(index: Int) {
        spectra = spectra.removeAt(index)
    }

    fun loadSpectrum(
        name: String,
        fid: ComplexDoubleArray,
        p0: Double,
        p1: Double,
        offset: Double,
        scale: Double,
        color: ImVec4? = null,
    ) {
        val data = fid.data.clone()
        spectra = spectra.add(
            SpectrumUiState(
                spectrumUnprocessed = fid,
                fft = DoubleArray(data.size / 2),
                name = name + " (${lastSpectrum++})",
                color = color,
                lb = 0.0,
                offset = offset,
                scale = scale,
                p0 = p0,
                p1 = p1
            ).apply {
                update(this)
            }
        )
    }

    fun generateSVD(spectrum: SpectrumUiState) {
        scope.launch {
            val fid = spectrum.processing
            val rows = fid.size / 2
            val cols = fid.size - rows + 1
            val denoised = memScoped {
//                val hankel = HankelOperatorBruteForce(fid.toMemorySegment())
                val hankel = HankelOperator(this, fid.toMemorySegment(), rows, cols)
                val result = propack(hankel, rows, cols, numWanted = controls.numSingularValues)
                svdResults += result.singularValues
                result.reconstructDiagonals()
            }
            denoised[0] /= 2
            loadSpectrum("Denoised", denoised, spectrum.p0, spectrum.p1, spectrum.offset, spectrum.scale)
        }
    }
}