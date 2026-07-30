package me.dvyy.nmr.app.graphs.data

import me.dvyy.nmr.bindings.common.memScoped
import me.dvyy.nmr.bindings.fftw.FftwComplexArray
import me.dvyy.nmr.bindings.fftw.FftwDirection
import me.dvyy.nmr.bindings.fftw.FftwFlag
import me.dvyy.nmr.bindings.fftw.FftwPlan1D
import me.dvyy.nmr.bindings.fftw.fftShift
import me.dvyy.nmr.common.math.ComplexDoubleArray

fun List<ComplexDoubleArray>.fftEachRow(): List<ComplexDoubleArray> {
    val first = first()
    return memScoped {
        val size = first.size
        val input = FftwComplexArray.Companion(size)
        val output = FftwComplexArray.Companion(size)
        val plan = FftwPlan1D.Companion(size, input, output, FftwDirection.FORWARD, FftwFlag.ESTIMATE)
        map { fid ->
            val data = fid.data
            input.loadInterleaved(data)
            plan.execute()
            ComplexDoubleArray(output.toInterleavedArray()).fftShift()
        }
    }
}