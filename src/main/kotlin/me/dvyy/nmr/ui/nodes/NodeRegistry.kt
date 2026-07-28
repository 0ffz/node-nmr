package me.dvyy.nmr.ui.nodes

import me.dvyy.nmr.ui.nodes.inputs.SyntheticDataset
import me.dvyy.nmr.ui.nodes.multiscan.MultiScanAverage
import me.dvyy.nmr.ui.nodes.multiscan.MultiScanWaveletNode
import me.dvyy.nmr.ui.nodes.multiscan.NoiseAddingNode
import me.dvyy.nmr.ui.nodes.multiscan.SignalSelectNode
import me.dvyy.nmr.ui.nodes.outputs.ExportNode
import me.dvyy.nmr.ui.nodes.outputs.GraphNode
import me.dvyy.nmr.ui.nodes.outputs.SSIMNode
import me.dvyy.nmr.ui.nodes.transformations.ApodizationNode
import me.dvyy.nmr.ui.nodes.transformations.PhaseCorrectTransformation
import me.dvyy.nmr.ui.nodes.transformations.SVDCadzowFilter
import me.dvyy.nmr.ui.nodes.transformations.SavitzkyGolayApodization
import me.dvyy.nmr.ui.nodes.transformations.WaveletTransformation
import me.dvyy.nmr.ui.nodes.transformations.ZeroFillTransformation

object NodeRegistry {
    private val registry = mutableMapOf<String, NodeInfo<*>>()

    fun register(info: NodeInfo<*>) {
        registry[info.name] = info
    }

    fun create(type: String): Node? {
        return registry[type]?.factory?.invoke()
    }

    val availableNodes: List<NodeInfo<*>> get() = registry.values.toList()

    init {
        register(ApodizationNode)
        register(SavitzkyGolayApodization)
        register(ZeroFillTransformation)
        register(PhaseCorrectTransformation)
        register(WaveletTransformation)
        register(SVDCadzowFilter)
        register(SyntheticDataset)
        register(GraphNode)
        register(ExportNode)
        register(SSIMNode)
        register(NoiseAddingNode)
        register(SignalSelectNode)
        register(MultiScanAverage)
        register(MultiScanWaveletNode)
    }
}
