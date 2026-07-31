package me.dvyy.nmr.app.nodes.data

import me.dvyy.nmr.app.nodes.data.inputs.DatasetNode
import me.dvyy.nmr.app.nodes.data.inputs.MultiDatasetNode
import me.dvyy.nmr.app.nodes.data.inputs.SyntheticDataset
import me.dvyy.nmr.app.nodes.data.multiscan.MultiScanAverage
import me.dvyy.nmr.app.nodes.data.multiscan.MultiScanWaveletNode
import me.dvyy.nmr.app.nodes.data.multiscan.NoiseAddingNode
import me.dvyy.nmr.app.nodes.data.multiscan.SignalSelectNode
import me.dvyy.nmr.app.nodes.data.outputs.ExportNode
import me.dvyy.nmr.app.nodes.data.outputs.Graph2DNode
import me.dvyy.nmr.app.nodes.data.outputs.GraphNode
import me.dvyy.nmr.app.nodes.data.outputs.SSIMNode
import me.dvyy.nmr.app.nodes.data.transformations.*

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
        // 1D Data sources
        register(DatasetNode)
        register(SyntheticDataset)

        // 1D Outputs
        register(GraphNode)
        register(SSIMNode)
        register(ExportNode)

        // 1D Transformations
        register(ApodizationNode)
        register(SavitzkyGolayApodization)
        register(ZeroFillTransformation)
        register(PhaseCorrectTransformation)
        register(WaveletTransformation)
        register(SVDCadzowFilter)

        // 2D Outputs
        register(Graph2DNode)

        // Multi signal
        register(MultiDatasetNode)
        register(NoiseAddingNode)
        register(SignalSelectNode)
        register(MultiScanWaveletNode)
        register(MultiScanAverage)
    }
}
