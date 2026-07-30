package me.dvyy.nmr.app.dispatchers

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

object AppDispatchers {
    val Frontend = TriggeredCoroutineDispatcher("Frontend")
    val scope = CoroutineScope(Dispatchers.IO)
}