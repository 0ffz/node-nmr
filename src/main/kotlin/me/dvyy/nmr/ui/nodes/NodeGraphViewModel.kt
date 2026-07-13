package me.dvyy.nmr.ui.nodes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import imgui.extension.imnodes.ImNodes
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import me.dvyy.nmr.parsing.BrukerDataset
import me.dvyy.nmr.ui.nodes.transformations.ApodizationTransformation
import me.dvyy.nmr.ui.nodes.transformations.SignalTransformation
import me.dvyy.nmr.ui.nodes.transformations.WaveletTransformation

data class NodeLink(
    val id: Int,
    val from: Int,
    val to: Int,
)

class NodeGraphViewModel(
    val dataset: BrukerDataset,
) {
    init {
        ImNodes.createContext()
    }

    val editorContext = ImNodes.editorContextCreate()

    var id = 1
    var nodes by mutableStateOf<PersistentList<Node>>(persistentListOf())
    private val _links = mutableSetOf<NodeLink>()
    private val linkIds = mutableListOf<Int>()
    val links: Set<NodeLink> = _links

    init {
        val dataset = loadDataset(dataset, "Example dataset")
        val apod = addTransform(ApodizationTransformation())
        addTransform(WaveletTransformation())
        link(dataset, apod)
    }

    /**
     * Links the output of an [input] node to the input of an [output] node
     */
    fun link(input: Node, output: Node.Process) {
        _links.add(NodeLink(id++, input.outputId, output.inputId))
        input.signalStep.pipeInto(output.signalStep)
    }

    fun unlink(linkId: Int) {
        val link = _links.find { it.id == linkId } ?: return
        _links.remove(link)
        (nodeForAttribute(link.to) as? Node.Process)?.signalStep?.removePipe()
    }

    fun nodeForAttribute(id: Int): Node? {
        return nodes.find { it.outputId == id || (it as? Node.Process)?.inputId == id }
    }

    fun removeNode(id: Int) {
        val index = nodes.indexOfFirst { it.id == id }.takeIf { it != -1 } ?: return
        val node = nodes[index]
        nodes = nodes.removeAt(index)
        _links.removeIf { it.from == node.outputId || it.to == (node as? Node.Process)?.inputId }
    }

    fun loadDataset(dataset: BrukerDataset, name: String): Node.Input {
        val node = Node.Input(id++, name, dataset, id++)
        nodes = nodes.add(node)
        return node
    }

    fun addTransform(transform: SignalTransformation): Node.Process {
        val node = Node.Process(id++, transform.name, transform, id++, id++)
        nodes = nodes.add(node)
        return node
    }
}
