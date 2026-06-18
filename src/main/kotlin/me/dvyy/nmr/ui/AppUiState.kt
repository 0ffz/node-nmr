package me.dvyy.nmr.ui

import me.dvyy.nmr.bindings.fftw.FftwComplexArray
import me.dvyy.nmr.bindings.fftw.FftwDirection
import me.dvyy.nmr.bindings.fftw.FftwFlag
import me.dvyy.nmr.bindings.fftw.FftwPlan1D
import me.dvyy.nmr.bindings.helpers.memScoped
import me.dvyy.nmr.complex.ComplexDoubleArray
import me.dvyy.nmr.signal.fftShift
import me.dvyy.nmr.ui.graphs.SpectrumUiState
import org.jetbrains.bio.viktor.asF64Array

class AppUiState {
    val spectra: MutableList<SpectrumUiState> = mutableListOf()

    fun loadSpectrum(name: String, fid: ComplexDoubleArray) {
        val fft = memScoped {
            val output = FftwComplexArray.alloc(fid.size)
            val input = FftwComplexArray.alloc(fid.size)
            val plan = FftwPlan1D(fid.size, input.segment, output.segment, FftwDirection.FORWARD, FftwFlag.ESTIMATE.value)
            input.loadInterleaved(fid.data)
            plan.execute()
            ComplexDoubleArray(output.toInterleavedArray()).fftShift().abs().reversedArray()
        }
        val data = fid.data.clone()
        fft.asF64Array().let { it /= it.max() }
        data.asF64Array().let { it /= it.max() }
        spectra += SpectrumUiState(
            name = name,
            spectrum = data,
            fft = fft
        )
    }
}