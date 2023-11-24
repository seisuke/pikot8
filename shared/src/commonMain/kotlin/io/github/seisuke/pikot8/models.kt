package io.github.seisuke.pikot8

import kotlin.jvm.JvmInline

@JvmInline
value class SfxId(val id: Int)

data class Music(
    val sfxMap: Map<SfxId, Sfx>,
    val sfxPatternList: List<SfxPattern>,
) {
    fun transposePattern(): List<List<SfxId>> =
        (0..< 4).map { index ->
            sfxPatternList.map { pattern ->
                pattern.ids()[index]
            }
        }

    private fun SfxPattern.ids() = listOf(this.id1, this.id2, this.id3, this.id4)
}

data class SfxPattern(
    val id1: SfxId,
    val id2: SfxId,
    val id3: SfxId,
    val id4: SfxId,
    val loopStart: Boolean,
    val loopEnd: Boolean,
    val stop: Boolean,
)

data class Sfx(
    val notes: List<Note>,
    val mode: UByte,
    val speed: UByte,
    val loopStart: UByte,
    val loopEnd: UByte,
) {
    companion object {
        const val NOTES_MAX_SIZE = 32
    }
}

data class Note(
    val pitch: Int,
    val waveform: Int,
    val volume: Int,
)

expect class PlatformWave
