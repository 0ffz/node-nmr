package me.dvyy.nmr.ui.nodes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import imgui.extension.imnodes.ImNodes
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import me.dvyy.nmr.parsing.BrukerDataset
import me.dvyy.nmr.ui.nodes.transformations.*

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
        val graph = addTransform(GraphNode())
        link(dataset, apod)
        link(apod, graph)
    }

    /**
     * Links the output of an [input] node to the input of an [output] node
     */
    fun link(input: Node, output: Node) {
        val outId = input.outputId ?: return
        val inId = output.inputId ?: return
        _links.removeIf { it.to == output.inputId }
        _links.add(NodeLink(id++, outId, inId))
        (input.signalStep as SignalProviding).pipeInto(output.signalStep as SignalInput)
    }

    fun unlink(linkId: Int) {
        val link = _links.find { it.id == linkId } ?: return
        _links.remove(link)
        (nodeForAttribute(link.to)?.signalStep as? SignalInput)?.removePipe()
    }

    fun nodeForAttribute(id: Int): Node? {
        return nodes.find { it.outputId == id || it.inputId == id }
    }

    fun removeNode(id: Int) {
        val index = nodes.indexOfFirst { it.id == id }.takeIf { it != -1 } ?: return
        val node = nodes[index]
        nodes = nodes.removeAt(index)
        _links.removeIf { it.from == node.outputId || it.to == node.inputId }
    }

    fun loadDataset(dataset: BrukerDataset, name: String): Node {
        val node = Node(id++, name, dataset, outputId = id++, inputId = null)
        nodes = nodes.add(node)
        return node
    }

    fun addTransform(transform: SignalNode): Node {
        val node = Node(
            id++,
            transform.name,
            transform,
            inputId = if (transform is SignalInput) id++ else null,
            outputId = if (transform is SignalProviding) id++ else null
        )
        nodes = nodes.add(node)
        return node
    }
}
