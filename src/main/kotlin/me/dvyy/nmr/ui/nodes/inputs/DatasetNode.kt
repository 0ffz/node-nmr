package me.dvyy.nmr.ui.nodes.inputs

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.openDirectoryPicker
import kotlinx.coroutines.launch
import me.dvyy.nmr.app.dispatchers.AppDispatchers
import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.bindings.imgui.ImNodeContext
import me.dvyy.nmr.io.bruker.BrukerDataset
import me.dvyy.nmr.io.bruker.removeDigitalFilter
import me.dvyy.nmr.processing.model.Signal
import me.dvyy.nmr.processing.transform.phase.findOptimalPhaseParameters
import me.dvyy.nmr.signal.SignalUiState
import me.dvyy.nmr.ui.nodes.Node
import me.dvyy.nmr.ui.nodes.NodeInfo
import me.dvyy.nmr.ui.nodes.nodeState
import org.jetbrains.bio.viktor.asF64Array

class DatasetNode : Node() {
    var path by nodeState<String?>(null)
    val dataset: BrukerDataset? by derivedStateOf {
        BrukerDataset(path ?: return@derivedStateOf null)
    }
    val numSamples by derivedStateOf { dataset?.numSamples ?: 0 }

    //TODO correctly store dataset reference
    val offset by derivedStateOf { dataset?.offset ?: 0.0 }
    val output = outputAttribute {
        val dataset = dataset ?: return@outputAttribute null
        val data = dataset.readFid().removeDigitalFilter(dataset.acqus)
        data.data.asF64Array().let { it /= it.max() }
        val fid = Signal.Fid(data)
        SignalUiState(
            signal = fid,
            offset = offset,
            widthPPM = dataset.widthPPM,
            phaseParams = data.findOptimalPhaseParameters()
        )
    }

    override fun ImGuiKt.draw() {
        with(ImNodeContext) {
            outputAttribute(output.id) {
                text(dataset?.name ?: "Not loaded")
            }
        }
        text("Samples: $numSamples")

        button("Open") {
            AppDispatchers.scope.launch {
                val file = FileKit.openDirectoryPicker(dialogSettings = FileKitDialogSettings(title = "Open Bruker dataset")) ?: return@launch
//                val dataset = BrukerDataset(file.file.absolutePath)
                path = file.file.absolutePath
            }
        }
//        dragDouble("offset", offset, onChange = { offset = it })
    }

    companion object : NodeInfo<DatasetNode> {
        override val name = "Dataset"
        override val factory = ::DatasetNode
    }
}