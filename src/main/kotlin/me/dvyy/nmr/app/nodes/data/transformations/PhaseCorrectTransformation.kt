package me.dvyy.nmr.app.nodes.data.transformations

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import kotlinx.coroutines.Deferred
import me.dvyy.nmr.app.nodes.data.NodeInfo
import me.dvyy.nmr.app.nodes.data.SignalTransformationNode
import me.dvyy.nmr.app.nodes.ui.state.SignalUiState
import me.dvyy.nmr.common.math.ComplexDoubleArray
import me.dvyy.nmr.processing.model.Signal
import me.dvyy.nmr.processing.transform.phase.PhaseParams
import me.dvyy.nmr.processing.transform.phase.findOptimalPhaseParameters

class PhaseCorrectTransformation : SignalTransformationNode() {
    private val size by derivedStateOf { input?.fid?.size ?: 0 }

    override fun transformUiState(state: SignalUiState?): SignalUiState? {
        val cache = ComplexDoubleArray(size)
        input?.fft?.data?.copyInto(cache.data)
        val (p0, p1) = cache.findOptimalPhaseParameters()
        return state?.copy(phaseParams = PhaseParams(p0, p1))
    }

    override fun transform(): Deferred<Signal> {
        val input = input
        return compute { input ?: Signal.Empty }
    }


    companion object : NodeInfo<PhaseCorrectTransformation> {
        override val name = "Phase"
        override val factory = ::PhaseCorrectTransformation
    }
}