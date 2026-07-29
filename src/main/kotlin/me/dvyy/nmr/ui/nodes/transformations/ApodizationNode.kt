package me.dvyy.nmr.ui.nodes.transformations

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Deferred
import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.complex.ComplexDouble
import me.dvyy.nmr.complex.ComplexDoubleArray
import me.dvyy.nmr.signal.Signal
import me.dvyy.nmr.signal.SignalUiState
import me.dvyy.nmr.signal.expApodized
import me.dvyy.nmr.signal.gaussApodized
import me.dvyy.nmr.ui.nodes.GraphEmitting
import me.dvyy.nmr.ui.nodes.GraphUiState

import me.dvyy.nmr.ui.nodes.NodeInfo
import me.dvyy.nmr.ui.nodes.nodeState

class ApodizationNode : SignalTransformationNode(), GraphEmitting {
    var lb by nodeState(0.0001)
    var gauss by nodeState(0.0)
//    var beta by nodeState(0.0)
//    var lPrime by nodeState(4000)
    var graphApodizationLine by nodeState(false)

    override fun ImGuiKt.draw() {
        drawInput()
        dragDouble("lb", lb, onChange = { lb = it })
        dragDouble("gauss", gauss, onChange = { gauss = it })
//        dragDouble("beta", beta, onChange = { beta = it })
//        sliderInt("l'", lPrime, min = 0, max = size, onChange = { lPrime = it })
        checkbox("Show line", graphApodizationLine, onChange = { graphApodizationLine = it })
    }

    private val size by derivedStateOf { input?.fid?.size ?: 0 }

    //    private val cache by derivedStateOf { ComplexDoubleArray(size) }
    private val inputFid by derivedStateOf { input?.fid?.data }

    override fun transform(): Deferred<Signal>? {
        if (size == 0) return null
        val input = inputFid
        val lb = lb
        val gauss = gauss
//        val beta = beta
//        val lPrime = lPrime
        return compute {
            val cache = ComplexDoubleArray(size)
            input?.copyInto(cache.data)
            cache.expApodized(lb).gaussApodized(gauss)/*.applyMsgApodization(
                doubleArrayOf(
                    0.03497, 0.01399, -0.00233, -0.01399, -0.02098, -0.02331, -0.02098, -0.01399, -0.00233, 0.01399, 0.03497
                ), beta = beta, lPrime = lPrime
            )*/
            Signal.Fid(cache)
        }
    }

    override val graph: GraphUiState? by derivedStateOf {
        if (!graphApodizationLine) return@derivedStateOf null
        val signal = ComplexDoubleArray(size) { ComplexDouble(1.0, 0.0) }
        signal.expApodized(lb).gaussApodized(gauss)/*.applyMsgApodization(
            doubleArrayOf(
                0.03497, 0.01399, -0.00233, -0.01399, -0.02098, -0.02331, -0.02098, -0.01399, -0.00233, 0.01399, 0.03497
            ), beta = beta, lPrime = lPrime
        )*/
        GraphUiState("Apodization", SignalUiState(Signal.Fid(signal)))
    }

    companion object : NodeInfo<ApodizationNode> {
        override val name = "Apodization"
        override val factory = ::ApodizationNode
    }
}


