package io.github.seisuke.pikot8

internal actual fun String.parseHexByte() = Integer.parseInt(this, 16).toByte()

