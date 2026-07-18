package me.dvyy.nmr.signal

import me.dvyy.nmr.bindings.fftw.FftwComplexArray
import me.dvyy.nmr.bindings.fftw.FftwDirection
import me.dvyy.nmr.bindings.fftw.FftwFlag
import me.dvyy.nmr.bindings.fftw.FftwPlan1D
import me.dvyy.nmr.bindings.helpers.memScoped
import me.dvyy.nmr.complex.ComplexDoubleArray
import me.dvyy.nmr.complex.complexDoubleArrayOf
import me.dvyy.nmr.phasecorrect.PhaseParams
import me.dvyy.nmr.phasecorrect.phaseCorrect
import org.jetbrains.bio.viktor.asF64Array

data class SignalUiState(
    val signal: Signal,
    val offset: Double = 0.0,
    val phaseParams: PhaseParams = PhaseParams(0.0, 0.0),
) {
    val graphFid: DoubleArray by lazy {
        if(signal.fid.size == 0) return@lazy doubleArrayOf()
        signal.fid.real().also { it.asF64Array().let { it /= it.max() } }
    }
    val graphFft: DoubleArray by lazy {
        if(signal.fft.size == 0) return@lazy doubleArrayOf()
        val (p0, p1) = phaseParams
        signal.fft.phaseCorrect(p0, p1).real()
    }
    val graphWavelet: DoubleArray by lazy { signal.wavelet.real() }

}
/**
 * A 1D signal backed by an underlying type (ex. original fid, fourier-transformed, etc...)
 *
 * This allows transformations that work in fourier or wavelet spaces to keep their output in the same space
 * and save on operations/floating point errors.
 */
sealed class Signal {
    abstract val fid: ComplexDoubleArray
    abstract val fft: ComplexDoubleArray
    abstract val wavelet: ComplexDoubleArray

    class Fid(val data: ComplexDoubleArray) : Signal() {
        override val fid: ComplexDoubleArray = data
        override val fft: ComplexDoubleArray by lazy {
            if (fid.size == 0) fid
            else memScoped {
                val data = fid.data
                val size = fid.size
                val input = FftwComplexArray(size)
                val output = FftwComplexArray(size)
                val plan = FftwPlan1D(size, input, output, FftwDirection.FORWARD, FftwFlag.ESTIMATE)
                input.loadInterleaved(data)
                plan.execute()

                ComplexDoubleArray(output.toInterleavedArray()).fftShift()
            }.also { fft ->
                fft.data.asF64Array().let { it /= it.max() }
            }
        }
        override val wavelet: ComplexDoubleArray
            get() = TODO("Not yet implemented")
    }

    class Fft(val data: ComplexDoubleArray) : Signal() {
        override val fid: ComplexDoubleArray by lazy {
            if (fft.size == 0) fft
            else memScoped {
                val data = fft.data
                val size = fft.size
                val input = FftwComplexArray(size)
                val output = FftwComplexArray(size)
                val plan = FftwPlan1D(size, input, output, FftwDirection.BACKWARD, FftwFlag.ESTIMATE)
                input.loadInterleaved(fft.inverseFftShift().data)
                plan.execute()
                ComplexDoubleArray(output.toInterleavedArray())
            }
        }
        override val fft: ComplexDoubleArray = data
        override val wavelet: ComplexDoubleArray
            get() = TODO("Not yet implemented")
    }

    data object Empty: Signal() {
        override val fid: ComplexDoubleArray = complexDoubleArrayOf()
        override val fft: ComplexDoubleArray = complexDoubleArrayOf()
        override val wavelet: ComplexDoubleArray = complexDoubleArrayOf()
    }
}