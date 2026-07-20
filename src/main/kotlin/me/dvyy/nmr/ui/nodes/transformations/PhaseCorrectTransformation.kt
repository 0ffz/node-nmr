package me.dvyy.nmr.ui.nodes.transformations

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import kotlinx.coroutines.Deferred
import me.dvyy.nmr.complex.ComplexDoubleArray
import me.dvyy.nmr.phasecorrect.PhaseParams
import me.dvyy.nmr.phasecorrect.findOptimalPhaseParameters
import me.dvyy.nmr.signal.Signal
import me.dvyy.nmr.signal.SignalUiState

class PhaseCorrectTransformation : SignalTransformationNode() {
    override val name: String = "Phase"
    private val size by derivedStateOf { input?.fid?.size ?: 0 }
//    private val cache by derivedStateOf { ComplexDoubleArray(size) }
//    val p0 = mutableStateOf(0.0)
//    val p1 = mutableStateOf(0.0)
//
//    override val parameters: List<NodeAttribute> = listOf(
//        NodeAttribute("p0", p0),
//        NodeAttribute("p1", p1)
//    )

    override fun transformUiState(state: SignalUiState?): SignalUiState? {
        val cache = ComplexDoubleArray(size)
        input?.fft?.data?.copyInto(cache.data)
        val (p0, p1) = cache.findOptimalPhaseParameters()
        return state?.copy(phaseParams = PhaseParams(p0, p1))
    }

    override fun transform(): Deferred<Signal>? {
        val input = input
        return compute { input ?: Signal.Empty }
    }
//    override fun transform(): Deferred<Signal>? {
//        if (size == 0) return null
////        this.p0.value = p0
////        this.p1.value = p1
//        val fft = input?.fft?.data ?: return null
//        return compute {
//            val cache = ComplexDoubleArray(size)
//            fft?.copyInto(cache.data)
//            val (p0, p1) = cache.findOptimalPhaseParameters()
//            Signal.Fft(cache.phaseCorrect(p0, p1))
//        }
//    }
}