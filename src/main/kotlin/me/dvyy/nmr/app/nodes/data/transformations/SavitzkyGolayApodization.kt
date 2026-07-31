package me.dvyy.nmr.app.nodes.data.transformations

import kotlinx.coroutines.Deferred
import me.dvyy.nmr.app.nodes.data.NodeInfo
import me.dvyy.nmr.app.nodes.data.SignalTransformationNode
import me.dvyy.nmr.processing.model.Signal

class SavitzkyGolayApodization : SignalTransformationNode() {
    override fun transform(): Deferred<Signal> {
        return compute { TODO() }
    }

    companion object : NodeInfo<SavitzkyGolayApodization> {
        override val name = "Savitzky-Golay apod"
        override val category = "1D"
        override val subcategory = "Transformations"
        override val factory = ::SavitzkyGolayApodization
    }
}