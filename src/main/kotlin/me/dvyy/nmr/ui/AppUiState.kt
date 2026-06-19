package me.dvyy.nmr.ui

import imgui.ImVec4
import me.dvyy.nmr.bindings.fftw.FftwComplexArray
import me.dvyy.nmr.bindings.fftw.FftwDirection
import me.dvyy.nmr.bindings.fftw.FftwFlag
import me.dvyy.nmr.bindings.fftw.FftwPlan1D
import me.dvyy.nmr.bindings.helpers.memScoped
import me.dvyy.nmr.complex.ComplexDoubleArray
import me.dvyy.nmr.signal.fftShift
import me.dvyy.nmr.ui.graphs.SpectrumUiState
import org.jetbrains.bio.viktor.asF64Array

data class DenoiserControlsUiState(
    var lb: Double = 0.005,
    var runSVD: Boolean = false,
    var numSingularValues: Int = 12,
)
class AppUiState {
    val spectra: MutableList<SpectrumUiState> = mutableListOf()
    val controls = DenoiserControlsUiState()

    fun loadSpectrum(
        name: String,
        fid: ComplexDoubleArray,
        color: ImVec4? = null,
    ) {
        val fft = memScoped {
            val output = FftwComplexArray.alloc(fid.size)
            val input = FftwComplexArray.alloc(fid.size)
            val plan = FftwPlan1D(fid.size, input, output, FftwDirection.FORWARD, FftwFlag.ESTIMATE.value)
            input.loadInterleaved(fid.data)
            plan.execute()
            ComplexDoubleArray(output.toInterleavedArray()).fftShift().abs().reversedArray()
        }
        val data = fid.data.clone()
        fft.asF64Array().let { it /= it.max() }
        data.asF64Array().let { it /= it.max() }
        spectra += SpectrumUiState(
            name = name,
            color = color,
            spectrum = data,
            fft = fft
        )
    }
}