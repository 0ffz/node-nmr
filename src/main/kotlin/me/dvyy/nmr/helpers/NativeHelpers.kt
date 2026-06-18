package me.dvyy.nmr.helpers

import java.lang.foreign.Arena

inline fun <R> memScoped(block: Arena.() -> R): R {
    return Arena.ofConfined().use { arena ->
        arena.block()
    }
}
