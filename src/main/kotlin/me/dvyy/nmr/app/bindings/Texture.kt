package me.dvyy.nmr.app.bindings

import me.dvyy.nmr.common.math.ComplexDoubleArray
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.nio.ByteBuffer

class Texture(
    val id: Int,
    val width: Int,
    val height: Int,
) {
    val pixelBuffer = BufferUtils.createByteBuffer(width * height * 4)
    fun upload()/* = withContext(AppDispatchers.Frontend)*/ {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id)
//        glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, pixelBuffer)
        GL11.glTexImage2D(
            GL11.GL_TEXTURE_2D,
            0,
            GL11.GL_RGBA,
            width,
            height,
            0,
            GL11.GL_RGBA,
            GL11.GL_UNSIGNED_BYTE,
            pixelBuffer
        );
        val error = GL11.glGetError()
        if (error != GL11.GL_NO_ERROR) {
            println("OpenGL Error during texture upload: $error")
        }
    }

    fun updateBuffer(block: ByteBuffer.() -> Unit) {
        pixelBuffer.clear()
        block(pixelBuffer)
        pixelBuffer.flip()
        upload()
    }

    fun uploadHeatmap(data: List<ComplexDoubleArray>) = updateBuffer {
        val max = data.flatMap { it.abs().toList() }.max()
        for (y in 0 until height) {
            for (x in 0 until width) {
                val value = data[y][x].abs()

                val color = valueToColor(value, max)

                put(color.red.toByte())
                put(color.green.toByte())
                put(color.blue.toByte())
                put(255.toByte())
            }
        }
    }

    fun valueToColor(value: Double, max: Double): Color {
        val normalized = (value / max).coerceIn(0.0, 1.0)
        val r = (normalized * 255).toInt()
        val g = ((normalized * normalized) * 255).toInt()
        val b = (Math.sin(normalized * Math.PI) * 255).toInt()
        return Color(r, g, b)
    }

    companion object {
        fun newTexture(
            width: Int, height: Int,
        ): Texture {
            val textureId =
                GL11.glGenTextures()// Setup filtering (NEAREST keeps the sharp "pixelated" heatmap look, LINEAR blurs it)
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST)
            GL11.glTexImage2D(
                GL11.GL_TEXTURE_2D,
                0,
                GL11.GL_RGBA8,
                width,
                height,
                0,
                GL11.GL_RGBA,
                GL11.GL_UNSIGNED_BYTE,
                0L
            )
            return Texture(textureId, width, height)
        }
    }
}