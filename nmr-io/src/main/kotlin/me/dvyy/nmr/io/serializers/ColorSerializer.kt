package me.dvyy.nmr.io.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.awt.Color

object ColorSerializer: KSerializer<Color> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("java.awt.Color", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Color) {
        val hex = String.format("#%02x%02x%02x%02x", value.red, value.green, value.blue, value.alpha)
        encoder.encodeString(hex)
    }

    override fun deserialize(decoder: Decoder): Color {
        val hex = decoder.decodeString()
        return Color(Integer.parseUnsignedInt(hex.removePrefix("#"), 16), true)
    }
}