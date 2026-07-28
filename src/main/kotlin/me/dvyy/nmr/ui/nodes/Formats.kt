package me.dvyy.nmr.ui.nodes

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

object Formats {
    val json = Json {
        prettyPrint = true
        serializersModule = SerializersModule {
        }
    }
}