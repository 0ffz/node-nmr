package me.dvyy.nmr.app.dispatchers

import kotlinx.coroutines.CoroutineDispatcher
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.coroutines.CoroutineContext

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