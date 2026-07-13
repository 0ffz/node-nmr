package me.dvyy.nmr.ui.nodes

import me.dvyy.nmr.bindings.fftw.FftwComplexArray
import me.dvyy.nmr.bindings.fftw.FftwDirection
import me.dvyy.nmr.bindings.fftw.FftwFlag
import me.dvyy.nmr.bindings.fftw.FftwPlan1D
import me.dvyy.nmr.bindings.helpers.memScoped
import me.dvyy.nmr.complex.ComplexDoubleArray
import me.dvyy.nmr.signal.fftShift
import me.dvyy.nmr.signal.inverseFftShift
import org.jetbrains.bio.viktor.asF64Array

sealed class Signal {
    abstract val fid: ComplexDoubleArray
    abstract val fft: ComplexDoubleArray
    abstract val wavelet: ComplexDoubleArray

    val graphFid: DoubleArray by lazy {
        fid.real().also { it.asF64Array().let { it /= it.max() } }
    }
    val graphFft: DoubleArray by lazy {
        fft.real()
    }
    val graphWavelet: DoubleArray by lazy { wavelet.real() }

    data class Fid(val data: ComplexDoubleArray) : Signal() {
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

    data class Fft(val data: ComplexDoubleArray) : Signal() {
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
}
