package me.dvyy.nmr.ui.nodes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import imgui.extension.imnodes.ImNodes
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import me.dvyy.nmr.parsing.BrukerDataset

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
    var nodes by mutableStateOf<PersistentList<Node>>(
        persistentListOf(
            Node.Process(id++, "Apodization", ApodizationTransformation(), id++, id++),
            Node.Process(id++, "Wavelet", WaveletTransformation(), id++, id++),
            Node.Input(id++, "Dataset", dataset, id++)
        )
    )
    private val _links = mutableSetOf<NodeLink>()
    private val linkIds = mutableListOf<Int>()
    val links: Set<NodeLink> = _links

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
        nodes = nodes.removeAt(index)
        _links.removeIf { it.from == id || it.to == id }
    }

    fun loadDataset(dataset: BrukerDataset, name: String) {
        nodes = nodes.add(Node.Input(id++, name, dataset, id++))
    }

    fun addTransform(transform: SignalTransformation): Node {
        val node = Node.Process(id++, transform::class.simpleName ?: "Untitled", transform, id++, id++)
        nodes = nodes.add(node)
        return node
    }
}
