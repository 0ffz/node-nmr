package me.dvyy.nmr.ui

import imgui.ImVec4
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.dvyy.nmr.bindings.fftw.FftwComplexArray
import me.dvyy.nmr.bindings.fftw.FftwDirection
import me.dvyy.nmr.bindings.fftw.FftwFlag
import me.dvyy.nmr.bindings.fftw.FftwPlan1D
import me.dvyy.nmr.bindings.helpers.memScoped
import me.dvyy.nmr.complex.ComplexDoubleArray
import me.dvyy.nmr.phasecorrect.phaseCorrect
import me.dvyy.nmr.signal.fftShift
import me.dvyy.nmr.ui.graphs.SpectrumUiState
import org.jetbrains.bio.viktor.asF64Array

data class DenoiserControlsUiState(
    var lb: Double = 0.005,
    var runSVD: Boolean = false,
    var numSingularValues: Int = 12,
)

class AppUiState {
    val spectra = MutableStateFlow(persistentListOf<SpectrumUiState>())
    val controls = DenoiserControlsUiState()

    fun loadSpectrum(
        name: String,
        fid: ComplexDoubleArray,
        color: ImVec4? = null,
    ) {
        val fft = memScoped {
            val output = FftwComplexArray.alloc(fid.size)
            val input = FftwComplexArray.alloc(fid.size)
            val plan = FftwPlan1D(fid.size, input, output, FftwDirection.FORWARD, FftwFlag.ESTIMATE)
            input.loadInterleaved(fid.data)
            plan.execute()
            ComplexDoubleArray(output.toInterleavedArray())
                .fftShift()
                .phaseCorrect(p0 = -225.0, p1 = 275.0)
                .real()
                .reversedArray()
        }
        val data = fid.data.clone()
        fft.asF64Array().let { it /= it.max() }
        data.asF64Array().let { it /= it.max() }
        spectra.update {
            it.add(
                SpectrumUiState(
                    name = name,
                    color = color,
                    spectrum = data,
                    fft = fft
                )
            )
        }
    }
}