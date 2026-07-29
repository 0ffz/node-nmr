package me.dvyy.nmr

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import imgui.ImFontConfig
import imgui.ImGui
import imgui.ImVec2
import imgui.ImVec4
import imgui.app.Application
import imgui.app.Configuration
import imgui.extension.implot.ImPlot
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiConfigFlags
import imgui.flag.ImGuiDir
import imgui.flag.ImGuiStyleVar
import imgui.type.ImInt
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.helpers.loadFromResources
import me.dvyy.nmr.ui.SpectrumViewModel
import me.dvyy.nmr.ui.graphs.Graph2DScreen
import me.dvyy.nmr.ui.graphs.GraphScreen
import me.dvyy.nmr.ui.graphs.GraphType
import me.dvyy.nmr.ui.menubar.AppMenuBar
import me.dvyy.nmr.ui.menubar.MenuViewModel
import me.dvyy.nmr.ui.nodes.NodeGraphViewModel
import me.dvyy.nmr.ui.nodes.NodeScreen
import me.dvyy.nmr.ui.processing.SingularValuesGraph
import me.dvyy.nmr.ui.spectra.NodeListScreen
import java.util.EnumSet
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
    val scope = CoroutineScope(Dispatchers.IO)
}

@OptIn(ExperimentalAtomicApi::class)
class Main : Application() {
    val scope = CoroutineScope(Dispatchers.IO)
    val state = SpectrumViewModel(scope)
    val nodeGraph by lazy { NodeGraphViewModel() }
    val menuViewModel by lazy { MenuViewModel(scope, nodeGraph) }
    val uiScope = CoroutineScope(Dispatchers.Main)

    private val applyScheduled = AtomicBoolean(false)
    private val snapshotHandle = Snapshot.registerGlobalWriteObserver {
        applyScheduled.compareAndSet(expectedValue = false, newValue = true)
    }

    override fun initImGui(config: Configuration?) {
        super.initImGui(config)
        val io = ImGui.getIO()
//        io.iniFilename = null                                // We don't want to save .ini file
        io.fonts.setFreeTypeRenderer(true)
        io.fonts.addFontFromMemoryTTF(loadFromResources("/NotoSans.ttf"), 24f, ImFontConfig())
        io.fonts.addFontFromMemoryTTF(loadFromResources("/MaterialSymbolsRounded_28pt-Regular.ttf"), 28f, ImFontConfig().apply {
            mergeMode = true
            glyphOffset = ImVec2(0f, 4f)
            glyphMinAdvanceX = 28f
            glyphRanges = shortArrayOf(0xE000.toShort(), 0xF8FF.toShort(), 0)
        })
        io.fonts.build()
        ImGui.styleColorsLight()
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard)  // Enable Keyboard Controls
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable)      // Enable Docking
        io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable)
        ImGui.pushStyleColor(ImGuiCol.Header, ImVec4(0.5f, 0.5f, 0.5f, 0.25f))
        val rounding = 4f
        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, rounding)
        ImGui.pushStyleVar(ImGuiStyleVar.ChildRounding, rounding)
        ImGui.pushStyleVar(ImGuiStyleVar.FrameRounding, rounding)
        ImGui.pushStyleVar(ImGuiStyleVar.PopupRounding, rounding)
        ImGui.pushStyleVar(ImGuiStyleVar.ScrollbarRounding, rounding)
        ImGui.pushStyleVar(ImGuiStyleVar.GrabRounding, rounding)
        ImGui.pushStyleVar(ImGuiStyleVar.TabRounding, rounding)
        ImPlot.createContext()
        init()
    }

    fun init() {
        menuViewModel.updateColors()
    }

    fun setupDocking(dockspaceId: Int) {
        val bottom = ImInt()
        val top = ImInt()
        val bottomRight = ImInt()
        val bottomLeft = ImInt()
        ImGuiInternal.dockBuilderSplitNode(dockspaceId, ImGuiDir.Down, 0.5f, bottom, top)
        ImGuiInternal.dockBuilderSplitNode(bottom.get(), ImGuiDir.Left, 0.2f, bottomLeft, bottomRight)
        ImGuiInternal.dockBuilderDockWindow("Graphs 2D", top.get())
        ImGuiInternal.dockBuilderDockWindow("Graphs", top.get())
        ImGuiInternal.dockBuilderDockWindow("Nodes", bottomRight.get())
        ImGuiInternal.dockBuilderDockWindow("Singular Values", bottomLeft.get())
        ImGuiInternal.dockBuilderDockWindow("Spectra", bottomLeft.get())
        ImGuiInternal.dockBuilderFinish(dockspaceId)

    }

    var plots by mutableStateOf(EnumSet.of(GraphType.FID, GraphType.FFT))

    override fun process() = with(ImGuiKt) {
        if (applyScheduled.compareAndSet(expectedValue = true, newValue = false)) {
            Snapshot.sendApplyNotifications()
        }
        AppDispatchers.Frontend.executeDispatchedTasks()

        val dockspaceId = ImGui.dockSpaceOverViewport()
        mainMenuBar {
            AppMenuBar(state, menuViewModel, nodeGraph)
        }
        if (state.first) {
            state.first = false
            setupDocking(dockspaceId)
        }

        withStyle(ImGuiStyleVar.WindowPadding, 0.0f, 0.0f) {
            window("Graphs") {
                GraphScreen(
                    nodeGraph.nodes, plots, onPlotsChange = {
                        plots = it
                    }
                )
            }
            window("Graphs 2D") {
                Graph2DScreen(
                    nodes = nodeGraph.nodes,
                )
            }
        }

        window("Spectra") {
            NodeListScreen(state)
        }
        window("Singular Values") {
            SingularValuesGraph(state)
        }
        window("Nodes") {
            NodeScreen(nodeGraph)
        }
    }
}


fun main() {
    Application.launch(Main())
}
