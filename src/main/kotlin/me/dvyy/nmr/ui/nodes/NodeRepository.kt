package me.dvyy.nmr.ui.nodes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

class NodeRepository {
    var nodes by mutableStateOf<PersistentList<Node>>(persistentListOf())
        private set

    private val _links = mutableSetOf<NodeLink>()
    val links: Set<NodeLink> get() = _links

    /**
     * Adds a node to the repository.
     */
    fun <T : Node> addNode(node: T): T {
        nodes = nodes.add(node)
        return node
    }

    /**
     * Links the output of a [from] node to the input of an [into] node.
     */
    fun link(from: OutputAttribute<*>, into: InputAttribute<*>): Boolean {
        if (from.pipeInto(into)) {
            _links.removeIf { it.into.id == into.id }
            _links.add(NodeLink(NodeGraphViewModel.nextId(), from, into))
            return true
        }
        return false
    }

    /**
     * Removes a link by its link ID.
     */
    fun unlink(linkId: Int) {
        val link = _links.find { it.id == linkId } ?: return
        _links.remove(link)
        link.into.removePipe()
    }

    /**
     * Removes a node by its node ID and cleans up connected links.
     */
    fun removeNode(id: Int) {
        val index = nodes.indexOfFirst { it.id == id }.takeIf { it != -1 } ?: return
        val node = nodes[index]
        nodes = nodes.removeAt(index)
        node.attributes.forEach { attr ->
            val attrId = attr.id
            _links.toList().forEach { link ->
                if (link.from.id == attrId || link.into.id == attrId) {
                    unlink(link.id)
                }
            }
        }
    }

    /**
     * Finds an attribute across all active nodes by attribute ID.
     */
    fun findAttribute(id: Int): Attribute<*>? {
        nodes.forEach { node ->
            node.attributes.forEach { attr ->
                if (attr.id == id) return attr
            }
        }
        return null
    }

    /**
     * Clears all nodes and links in the repository.
     */
    fun clear() {
        _links.toList().forEach { unlink(it.id) }
        _links.clear()
        nodes = persistentListOf()
    }
}
