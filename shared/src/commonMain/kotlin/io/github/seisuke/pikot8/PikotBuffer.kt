package io.github.seisuke.pikot8

internal expect fun String.parseHexByte(): Byte

class PikotBuffer(private val sfxByteArrayText: String) {

    private var position: Int = 0

    fun size() = sfxByteArrayText.length

    fun readBuffer(byte: Int): PikotBuffer {
        val subString = addPosition(byte * 2)
        return PikotBuffer(subString)
    }

    fun readUnsignedByte(): UByte = addPosition(2).toUByte(16)

    fun readByte(): Byte = addPosition(2).parseHexByte()

    fun read4bit(): UByte = addPosition(1).toUByte()

    private fun addPosition(length: Int): String {
        val value = sfxByteArrayText.substring(this.position..<this.position + length)
        position += length
        return value
    }
}
