package me.dvyy.nmr.ui.menubar

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.openDirectoryPicker
import io.github.vinceglb.filekit.dialogs.openFilePicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.dvyy.nmr.parsing.BrukerDataset
import me.dvyy.nmr.ui.nodes.NodeGraphViewModel
import me.dvyy.nmr.ui.nodes.inputs.Dataset2DNode
import me.dvyy.nmr.ui.nodes.inputs.DatasetNode
import me.dvyy.nmr.ui.nodes.inputs.MultiDatasetNode
import kotlin.system.exitProcess

class MenuViewModel(
    val scope: CoroutineScope,
    val graph: NodeGraphViewModel,
) {
    fun exit() {
        exitProcess(0)
    }

    fun openFilePicker() {
        scope.launch {
            val file = FileKit.openDirectoryPicker(dialogSettings = FileKitDialogSettings(title = "Open Bruker dataset")) ?: return@launch
            val dataset = BrukerDataset(file.file.absolutePath)
            graph.addNode(if(dataset.is2D) Dataset2DNode(dataset) else DatasetNode(dataset))
        }
    }

    fun openMultiFilePicker() {
        scope.launch {
            val parentFolder = FileKit.openDirectoryPicker(dialogSettings = FileKitDialogSettings(title = "Open Bruker dataset")) ?: return@launch
            val files = parentFolder.file.listFiles()
            graph.addNode(MultiDatasetNode(files.map { BrukerDataset(it.absolutePath) }))
        }
    }
}