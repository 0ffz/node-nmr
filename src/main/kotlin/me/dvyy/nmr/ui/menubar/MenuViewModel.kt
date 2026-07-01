package me.dvyy.nmr.ui.menubar

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.openDirectoryPicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.dvyy.nmr.parsing.BrukerDataset
import me.dvyy.nmr.ui.SpectrumViewModel
import kotlin.system.exitProcess

class MenuViewModel(
    val scope: CoroutineScope,
    val spectra: SpectrumViewModel,
) {
    fun exit() {
        exitProcess(0)
    }

    fun openFilePicker() {
        scope.launch {
            val file = FileKit.openDirectoryPicker(dialogSettings = FileKitDialogSettings(title = "Open Bruker dataset")) ?: return@launch
            spectra.loadSpectrum(file.file.name, BrukerDataset(file.file.absolutePath))
        }
    }
}