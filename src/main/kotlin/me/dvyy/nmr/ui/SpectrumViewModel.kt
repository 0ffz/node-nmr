package me.dvyy.nmr.ui

import androidx.compose.runtime.*
import imgui.ImVec4
import imgui.extension.imnodes.ImNodes
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
import me.dvyy.nmr.phasecorrect.autoPhaseSpectrum
import me.dvyy.nmr.phasecorrect.phaseCorrect
import me.dvyy.nmr.signal.expApodized
import me.dvyy.nmr.signal.fftShift
import me.dvyy.nmr.svd.HankelOperator
import me.dvyy.nmr.svd.reconstructDiagonals
import me.dvyy.nmr.ui.graphs.GraphType
import me.dvyy.nmr.ui.graphs.SpectrumUiState
import me.dvyy.nmr.ui.nodes.Node
import me.dvyy.nmr.ui.processing.DenoiserControlsUiState
import me.dvyy.nmr.wavelet.WaveletHelpers.applySoftThreshold
import me.dvyy.nmr.wavelet.WaveletHelpers.waveletDenoise
import org.jetbrains.bio.viktor.asF64Array

class SpectrumViewModel(
    val scope: CoroutineScope,
) {
    init {
        ImNodes.createContext()
    }
    val editorContext = ImNodes.editorContextCreate()

    val nodes = mutableListOf(
        Node.Process(1, "Apodization", 2, 3),
        Node.Process(4, "SVD", 5, 6),
        Node.Input(7, "Dataset", 8)
    )
    val links = mutableMapOf<Int, Int>(
        3 to 5,
    )


    private var lastSpectrum = 0
    var spectra by mutableStateOf(persistentListOf<SpectrumUiState>())
    var svdResults = mutableStateListOf<DoubleArray>()
    val visibleSpectra by derivedStateOf { spectra.filter { it.visible } }
    val controls = DenoiserControlsUiState()
    var first = true
    var selectedSpectrum = 0
    var graphType by mutableStateOf<GraphType>(GraphType.FFT)

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

    fun copy(spectrum: SpectrumUiState, type: GraphType) {
        when (type) {
            GraphType.WAVELET -> {
                TODO()
//                val spec = ComplexDoubleArray.from(spectrum.waveletRe, spectrum.waveletIm)
//                loadSpectrum(spectrum.name, spec, spectrum.p0, spectrum.p1, spectrum.offset, spectrum.scale)
            }

            else -> {
                TODO()
            }
        }
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
        val specRe = spectrum.processing.real()
        val specIm = spectrum.processing.im()
        val waveletIm = StationaryWaveletTransform(waveletName = "db2", signalLength = specIm.size, level = 4).use { swt ->
            swt.forward(specIm)
        }
        val waveletRe = StationaryWaveletTransform(waveletName = "db2", signalLength = fft.size, level = 2).use { swt ->
            swt.forward(fft)
        }
//        WaveletShrinkage.denoise(waveletRe, D4Wavelet())
//        WaveletShrinkage.denoise(waveletIm, D4Wavelet())
//        D4Wavelet().transform(waveletRe)
//        D4Wavelet().transform(waveletIm)

        fft.asF64Array().let { it /= it.max() }
        fft.reverse()
//        waveletIm.asF64Array().let {
//            it /= it.max()
//        }
        waveletRe.asF64Array().let {
            it /= it.max()
        }


        spectrum.fft = fft
        spectrum.waveletRe = waveletRe
//        spectrum.waveletIm = waveletIm
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
            offset = offset,
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
                waveletRe = DoubleArray(data.size / 2),
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
