package me.dvyy.nmr.processing.model

import me.dvyy.nmr.bindings.common.memScoped
import me.dvyy.nmr.bindings.fftw.*
import me.dvyy.nmr.bindings.wavelib.StationaryWaveletTransform
import me.dvyy.nmr.common.math.ComplexDoubleArray
import me.dvyy.nmr.common.math.complexDoubleArrayOf
import org.jetbrains.bio.viktor.asF64Array

/**
 * A 1D signal backed by an underlying type (ex. original fid, fourier-transformed, etc...)
 *
 * This allows transformations that work in fourier or wavelet spaces to keep their output in the same space
 * and save on operations/floating point errors.
 */
sealed class Signal {
    abstract val fid: ComplexDoubleArray
    abstract val fft: ComplexDoubleArray
    val waveletLevels = 4
    val wavelet: ComplexDoubleArray by lazy {
        if (fft.size == 0) return@lazy fft
        val waveletRe = StationaryWaveletTransform(
            waveletName = "db2",
            signalLength = fft.size,
            level = waveletLevels
        ).use { swt ->
            swt.forward(fft.real())
        }
        val waveletIm = StationaryWaveletTransform(
            waveletName = "db2",
            signalLength = fft.size,
            level = waveletLevels
        ).use { swt ->
            swt.forward(fft.im())
        }
        ComplexDoubleArray.from(waveletRe, waveletIm)
    }

    class Fid(val data: ComplexDoubleArray) : Signal() {
        override val fid: ComplexDoubleArray = data
        override val fft: ComplexDoubleArray by lazy {
            if (fid.size == 0) fid
            else memScoped {
                val data = fid.data
                val size = fid.size
                val input = FftwComplexArray.Companion(size)
                val output = FftwComplexArray.Companion(size)
                val plan = FftwPlan1D.Companion(size, input, output, FftwDirection.FORWARD, FftwFlag.ESTIMATE)
                input.loadInterleaved(data)
                plan.execute()

                ComplexDoubleArray(output.toInterleavedArray()).fftShift()
            }.also { fft ->
                fft.data.asF64Array().let { it /= it.max() }
            }
        }
    }

    class Fft(val data: ComplexDoubleArray) : Signal() {
        override val fid: ComplexDoubleArray by lazy {
            if (fft.size == 0) fft
            else memScoped {
                val data = fft.data
                val size = fft.size
                val input = FftwComplexArray.Companion(size)
                val output = FftwComplexArray.Companion(size)
                val plan = FftwPlan1D.Companion(size, input, output, FftwDirection.BACKWARD, FftwFlag.ESTIMATE)
                input.loadInterleaved(fft.inverseFftShift().data)
                plan.execute()
                ComplexDoubleArray(output.toInterleavedArray())
            }
        }
        override val fft: ComplexDoubleArray = data
    }

    data object Empty : Signal() {
        override val fid: ComplexDoubleArray = complexDoubleArrayOf()
        override val fft: ComplexDoubleArray = complexDoubleArrayOf()
    }
}