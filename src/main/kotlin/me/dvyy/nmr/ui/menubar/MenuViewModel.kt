package me.dvyy.nmr.ui.menubar

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import imgui.extension.imnodes.ImNodes
import imgui.extension.implot.ImPlot
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
    var isDarkMode by mutableStateOf(false)

    fun toggleDarkMode() {
        isDarkMode = !isDarkMode
        updateColors()
    }

    fun updateColors() {
        if (isDarkMode) {
            imgui.ImGui.styleColorsDark()
            ImPlot.styleColorsDark()
            ImNodes.styleColorsDark()
        } else {
            imgui.ImGui.styleColorsLight()
            ImNodes.styleColorsLight()
            ImPlot.styleColorsAuto()
        }
    }

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