package me.dvyy.nmr.app.nodes.data.inputs

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.openDirectoryPicker
import kotlinx.coroutines.launch
import me.dvyy.nmr.app.bindings.imgui.ImGuiKt
import me.dvyy.nmr.app.bindings.imgui.ImNodeContext
import me.dvyy.nmr.app.core.dispatchers.AppDispatchers
import me.dvyy.nmr.app.nodes.data.Node
import me.dvyy.nmr.app.nodes.data.NodeInfo
import me.dvyy.nmr.app.nodes.data.parameters.nodeState
import me.dvyy.nmr.common.math.ComplexDoubleArray
import me.dvyy.nmr.io.bruker.BrukerDataset
import me.dvyy.nmr.io.bruker.removeDigitalFilter
import me.dvyy.nmr.processing.model.Signal
import me.dvyy.nmr.processing.model.SignalSet
import org.jetbrains.bio.viktor.asF64Array
import java.io.File

class MultiDatasetNode : Node() {
    var path by nodeState<String?>(null)

    val datasets: List<BrukerDataset> by derivedStateOf {
        val parentPath = path ?: return@derivedStateOf emptyList()
        val parentFolder = File(parentPath)
        if (!parentFolder.exists() || !parentFolder.isDirectory) return@derivedStateOf emptyList()
        val files = parentFolder.listFiles() ?: return@derivedStateOf emptyList()
        files.filter { it.isDirectory }
            .sortedBy { it.name.toIntOrNull() ?: Int.MAX_VALUE }
            .mapNotNull { runCatching { BrukerDataset(it.absolutePath) }.getOrNull() }
    }

    val numDatasets by derivedStateOf { datasets.size }

    val output = outputAttribute<SignalSet?> {
        if (datasets.isEmpty()) return@outputAttribute null
        val data = datasets.map {
            it.readFid().removeDigitalFilter(it.acqus).apply {
                data.asF64Array().let { array -> array /= array.max() }
            }
        }
        SignalSet(data.map { Signal.Fid(it) })
    }

    override fun ImGuiKt.draw() {
        with(ImNodeContext) {
            outputAttribute(output.id) {
                val folderName = path?.let { File(it).name } ?: "Not loaded"
                text(folderName)
            }
        }
        text("Datasets: $numDatasets")

        button("Open") {
            AppDispatchers.scope.launch {
                val file = FileKit.openDirectoryPicker(dialogSettings = FileKitDialogSettings(title = "Open Bruker dataset")) ?: return@launch
                path = file.file.absolutePath
            }
        }
    }

    companion object : NodeInfo<MultiDatasetNode> {
        override val name = "Multi Dataset"
        override val factory = ::MultiDatasetNode
    }
}

class Dataset2DNode(
    val dataset: BrukerDataset,
) : Node() {
    val output = outputAttribute<List<ComplexDoubleArray>> {
        dataset.readSer().map { it.removeDigitalFilter(dataset.acqus) }
    }

    override fun ImGuiKt.draw() {
        with(ImNodeContext) {
            outputAttribute(output.id) {
                text("Out")
            }
        }
    }
}