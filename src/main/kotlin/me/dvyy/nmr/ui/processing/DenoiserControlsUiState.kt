package me.dvyy.nmr.ui.processing

data class DenoiserControlsUiState(
    var lb: Double = 0.005,
    var gauss: Double = 0.0,
    var runSVD: Boolean = false,
    var numSingularValues: Int = 12,
)