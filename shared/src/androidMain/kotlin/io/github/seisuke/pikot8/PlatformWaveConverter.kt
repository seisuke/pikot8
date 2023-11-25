package io.github.seisuke.pikot8

import com.ditchoom.buffer.toArray
import java.nio.ByteBuffer
import java.nio.ByteOrder

actual typealias PlatformWave = ByteArray

actual class PlatformWaveConverter {
    fun convert(wave: Wave): PlatformWave {
        val byteBuffer = ByteBuffer.allocate(wave.size * 2)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        wave.map {
            (it * Short.MAX_VALUE).toInt().toShort()
        }.forEach {
            byteBuffer.putShort(it)
        }
        return (byteBuffer.flip() as ByteBuffer).toArray()
    }
}
