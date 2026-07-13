package me.dvyy.nmr.ui.nodes.transformations

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import me.dvyy.nmr.bindings.helpers.memScoped
import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.bindings.propack.Propack
import me.dvyy.nmr.complex.ComplexDoubleArray
import me.dvyy.nmr.signal.Signal
import me.dvyy.nmr.signal.expApodized
import me.dvyy.nmr.signal.gaussApodized
import me.dvyy.nmr.svd.HankelOperator
import me.dvyy.nmr.svd.MathHelpers
import me.dvyy.nmr.svd.reconstructDiagonals
import me.dvyy.nmr.ui.nodes.NodeAttribute

class ApodizationTransformation() : SignalTransformation() {
    override val name: String = "Apodization"
    var lb = mutableStateOf(0.0001)
    var gauss = mutableStateOf(0.0)

    override val parameters: List<NodeAttribute> = listOf(
        NodeAttribute("lb", lb),
        NodeAttribute("gauss", gauss)
    )

    private val size by derivedStateOf { input?.fid?.size ?: 0 }
    private val cache by derivedStateOf { ComplexDoubleArray(size) }
    private val inputFid by derivedStateOf { input?.fid?.data }

    override fun transform(): Signal {
        if (size == 0) return Signal.Empty
        inputFid?.copyInto(cache.data)
        cache.expApodized(lb.value).gaussApodized(gauss.value)
        return Signal.Fid(cache)
    }
}


class ZeroFillTransformation : SignalTransformation() {
    override val name: String = "Zero-fill"
    private val size by derivedStateOf { input?.fid?.size ?: 0 }
    private val cache by derivedStateOf {
        val target = MathHelpers.nextPowerOfTwo(size + 1000)
        ComplexDoubleArray(target)
    }

    override fun transform(): Signal {
        if (size == 0) return Signal.Empty
        cache.data.fill(0.0)
        input?.fid?.data?.copyInto(cache.data)
        return Signal.Fid(cache)
    }
}

//class PhaseCorrectTransformation : SignalTransformation() {
//    override val name: String = "Phase"
//    private val size by derivedStateOf { input?.fid?.size ?: 0 }
//    private val cache by derivedStateOf { ComplexDoubleArray(size) }
//    val p0 = mutableStateOf(0.0)
//    val p1 = mutableStateOf(0.0)
//
//    override val parameters: List<NodeAttribute> = listOf(
//        NodeAttribute("p0", p0),
//        NodeAttribute("p1", p1)
//    )
//
//    override val output: State<Signal> = derivedStateOf {
//        if (size == 0) return@derivedStateOf Signal.Empty
//        input?.fft?.data?.copyInto(cache.data)
//        val (p0, p1) = cache.findOptimalPhaseParameters()
//        this.p0.value = p0
//        this.p1.value = p1
//        Signal.Fft(cache.phaseCorrect(p0, p1))
//    }
//}


class SVDTransformation : SignalTransformation() {
    override val name: String = "SVD"
    var numValues by mutableStateOf(10)

    override fun ImGuiKt.drawParams() {
        sliderInt("numValues", numValues, min = 1, max = 100, onChange = { numValues = it })
    }

    // TODO long-running background calculations
    override fun transform(): Signal {
        val fid = input?.fid ?: return Signal.Empty
        val rows = fid.size / 2
        val cols = fid.size - rows + 1
        val denoised = memScoped {
//                val hankel = HankelOperatorBruteForce(fid.toMemorySegment())
            val hankel = HankelOperator(this, fid.toMemorySegment(), rows, cols)
            val result = Propack.partialComplexSVD(hankel, rows, cols, numWanted = numValues)
//            svdResults += result.singularValues
            result.reconstructDiagonals()
        }
        return Signal.Fid(denoised)
//        denoised[0] /= 2
    }
}
