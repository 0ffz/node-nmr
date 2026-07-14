package me.dvyy.nmr

import androidx.compose.runtime.snapshots.Snapshot
import imgui.ImFontConfig
import imgui.ImGui
import imgui.ImGuiStyle
import imgui.ImVec2
import imgui.app.Application
import imgui.app.Configuration
import imgui.extension.implot.ImPlot
import imgui.flag.ImGuiConfigFlags
import imgui.flag.ImGuiDir
import imgui.flag.ImGuiStyleVar
import imgui.type.ImInt
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.parsing.BrukerDataset
import me.dvyy.nmr.synthetic.Resonance
import me.dvyy.nmr.ui.SpectrumViewModel
import me.dvyy.nmr.ui.graphs.GraphScreen
import me.dvyy.nmr.ui.menubar.AppMenuBar
import me.dvyy.nmr.ui.menubar.MenuViewModel
import me.dvyy.nmr.ui.nodes.NodeGraphViewModel
import me.dvyy.nmr.ui.nodes.NodeScreen
import me.dvyy.nmr.ui.processing.SingularValuesGraph
import me.dvyy.nmr.ui.spectra.SpectraScreen
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.coroutines.CoroutineContext
import imgui.internal.ImGui as ImGuiInternal

class TriggeredCoroutineDispatcher(val name: String) : CoroutineDispatcher() {
    private val taskQueue = ConcurrentLinkedQueue<Runnable>()

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        taskQueue.add(block)
    }

    internal fun executeDispatchedTasks() {
        while (taskQueue.isNotEmpty()) {
            val task = taskQueue.poll()
            task.run()
        }
    }
}

object AppDispatchers {
    val Frontend = TriggeredCoroutineDispatcher("Frontend")
}

@OptIn(ExperimentalAtomicApi::class)
class Main : Application() {
    val context = ImPlot.createContext()
    val scope = CoroutineScope(Dispatchers.IO)
    val state = SpectrumViewModel(scope)
    val nodeGraph = NodeGraphViewModel(
        dataset = BrukerDataset("data/13C_lowsignal/28")
    )
    val menuViewModel = MenuViewModel(scope, nodeGraph)
    val uiScope = CoroutineScope(Dispatchers.Main)

    private val applyScheduled = AtomicBoolean(false)
    private val snapshotHandle = Snapshot.registerGlobalWriteObserver {
        applyScheduled.compareAndSet(expectedValue = false, newValue = true)
    }

    override fun initImGui(config: Configuration?) {
        super.initImGui(config)
        val io = ImGui.getIO()
        io.iniFilename = null                                // We don't want to save .ini file
        io.fonts.setFreeTypeRenderer(true)
        io.fonts.addFontFromMemoryTTF(loadFromResources("/NotoSans.ttf"), 24f, ImFontConfig())
        io.fonts.addFontFromMemoryTTF(loadFromResources("/MaterialSymbolsRounded_28pt-Regular.ttf"), 28f, ImFontConfig().apply {
            mergeMode = true
            glyphOffset = ImVec2(0f, 4f)
            glyphMinAdvanceX = 28f
            glyphRanges = shortArrayOf(0xE000.toShort(), 0xF8FF.toShort(), 0)
        })
        io.fonts.build()
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard)  // Enable Keyboard Controls
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable)      // Enable Docking
        io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable)
        init()
        ImGui.styleColorsDark(ImGuiStyle().apply {
            framePadding = ImVec2()
        })

    }

    fun init() {
        val brukerData = BrukerDataset("data/13C_lowsignal/27")
        val cleanData = BrukerDataset("data/13C_lowsignal/28")
        println("Pulse Program: ${brukerData.acqus["PULPROG"]}")
        println("Spectrometer Frequency: ${brukerData.acqus["SFO1"]} MHz")

        val sampleRate = 10000.0 // 10 kHz
        val dwellTime = 1.0 / sampleRate
        val peaks = listOf(
//            Resonance(amplitude = 10.0, frequencyHz = 150.0, phaseRadians = 0.0, t2StarSeconds = 0.05),
            Resonance(amplitude = 5.0, frequencyHz = -50.0, phaseRadians = 0.0, t2StarSeconds = 0.1)
        )
//        state.loadSpectrum(
//            "Synthetic",
//            generateNmrSignal(
//                16384,
//                dwellTime,
//                peaks
//            ).addGaussianNoise(1.0),
//            0.0, 0.0, 0.0,1.0
//        )
//        state.loadSpectrum(
//            "Synthetic",
//            generateNmrSignal(
//                16384,
//                dwellTime,
//                peaks
//            ),
//            0.0, 0.0, 0.0,1.0
//        )
//        state.loadSpectrum("Dirty", brukerData, color = Colors.backgroundGray)
//        state.loadSpectrum("Clean", cleanData)
    }


    fun setupDocking(dockspaceId: Int) {
        val right = ImInt()
        val left = ImInt()
        val leftTop = ImInt()
        val leftBottom = ImInt()
        ImGuiInternal.dockBuilderSplitNode(dockspaceId, ImGuiDir.Left, 0.5f, left, right)
        ImGuiInternal.dockBuilderSplitNode(left.get(), ImGuiDir.Down, 0.2f, leftBottom, leftTop)
        ImGuiInternal.dockBuilderDockWindow("Graphs", right.get())
        ImGuiInternal.dockBuilderDockWindow("Nodes", leftTop.get())
        ImGuiInternal.dockBuilderDockWindow("Singular Values", leftTop.get())
        ImGuiInternal.dockBuilderDockWindow("Spectra", leftBottom.get())
        ImGuiInternal.dockBuilderFinish(dockspaceId)

    }

    override fun process() = with(ImGuiKt) {
        if (applyScheduled.compareAndSet(expectedValue = true, newValue = false)) {
            Snapshot.sendApplyNotifications()
        }
        AppDispatchers.Frontend.executeDispatchedTasks()

        val dockspaceId = ImGui.dockSpaceOverViewport()
        mainMenuBar {
            AppMenuBar(state, menuViewModel)
        }
        if (state.first) {
            state.first = false
            setupDocking(dockspaceId)
        }

        withStyle(ImGuiStyleVar.WindowPadding, 0.0f, 0.0f) {
            window("Graphs") {
                GraphScreen(
                    state.graphType,
                    onGraphChange = { state.graphType = it },
                    nodeGraph.nodes,
                    state.visibleSpectra,
                )
            }
        }

        window("Singular Values") {
            SingularValuesGraph(state)
        }
        window("Spectra") {
            SpectraScreen(state)
        }
        window("Nodes") {
            NodeScreen(nodeGraph)
        }
    }
}


fun main() {
    Application.launch(Main())
}
