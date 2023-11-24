package io.github.seisuke.pikot8

internal actual fun String.parseHexByte() = this.toInt(16).toByte()

