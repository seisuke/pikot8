package io.github.seisuke.pikot8

import io.github.seisuke.pikot8.ParseSfxResult.*
import kotlin.jvm.JvmInline

@JvmInline
value class SfxId(val id: Int)

sealed interface ParseMusicResult {
    companion object {
        fun parse(sfxText: String): ParseMusicResult = try {
            when (val parseSfxResult = SfxParser().parse(sfxText)) {
                is SfxAndPattern -> music(parseSfxResult)
                is SingleSfx -> {
                    val map = mapOf(SfxId(1) to parseSfxResult.sfx)
                    val generator = WaveGenerator(map)
                    val wave = generator.generateWaveMap().getValue(SfxId(1))
                    SingleSfxWave(wave)
                }
            }
        } catch (e: IllegalArgumentException) {
            Error
        }

        private fun music(sfxAndPattern: SfxAndPattern): Music {
            val generator = WaveGenerator(sfxAndPattern.sfxMap)
            val waveMap = generator.generateWaveMap()
            val emptyWave = generator.createEmptyNote()
            return Music(
                waveMap = waveMap,
                transposedPattern = sfxAndPattern.transposePattern(),
                emptyWave = emptyWave,
            )
        }
    }

    data class Music(
        val waveMap: Map<SfxId, Wave>,
        val transposedPattern: List<List<SfxId>>,
        val emptyWave: Wave
    ): ParseMusicResult

    data class SingleSfxWave(val wave: Wave): ParseMusicResult

    data object Error: ParseMusicResult
}

sealed interface ParseSfxResult {
    data class SfxAndPattern(
        val sfxMap: Map<SfxId, Sfx>,
        val sfxPatternList: List<SfxPattern>,
    ): ParseSfxResult {
        fun transposePattern(): List<List<SfxId>> =
            (0..< 4).map { index ->
                sfxPatternList.map { pattern ->
                    pattern.ids()[index]
                }
            }

        private fun SfxPattern.ids() = listOf(this.id1, this.id2, this.id3, this.id4)
    }
    data class SingleSfx(val sfx: Sfx): ParseSfxResult
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

