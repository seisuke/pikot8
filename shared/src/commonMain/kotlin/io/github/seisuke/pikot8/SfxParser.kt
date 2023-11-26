package io.github.seisuke.pikot8

import io.github.seisuke.pikot8.ParseSfxResult.SfxAndPattern
import io.github.seisuke.pikot8.ParseSfxResult.SingleSfx
import io.github.seisuke.pikot8.Sfx.Companion.NOTES_MAX_SIZE

class SfxParser {

    @Throws(IllegalArgumentException::class)
    fun parse(sfxText: String): ParseSfxResult {
        //need escape for right square brackets in Kotlin/JS
        val matchGroup = Regex("""(\[sfx\])?([0-9a-f]*)(\[.sfx\])?""")
            .matchEntire(sfxText)?.groups?.get(2)
            ?: throw IllegalArgumentException("Invalid data format")
        val buffer = PikotBuffer(matchGroup.value)
        return when {
            buffer.size() < 150 -> throw IllegalArgumentException("Invalid data format")
            buffer.size() == 168 -> {
                val sfx = parseSingleSfx(buffer)
                SingleSfx(sfx)
            }
            else -> {
                val sfxNum = buffer.readUnsignedByte().toInt()
                val patternNum = buffer.readUnsignedByte().toInt()
                val sfxMap = (0..< sfxNum).associate {
                    parseSfxInMusic(buffer)
                }
                val sfxPatternList = (0 ..< patternNum).map {
                    parsePattern(buffer)
                }

                SfxAndPattern(
                    sfxMap,
                    sfxPatternList,
                )
            }
        }
    }

    private fun parseSingleSfx(buffer: PikotBuffer): Sfx {
        return Sfx(
            mode = buffer.readUnsignedByte(),
            speed = buffer.readUnsignedByte(),
            loopStart = buffer.readUnsignedByte(),
            loopEnd = buffer.readUnsignedByte(),
            notes = parseNotesInSingleSfx(buffer)
        )
    }

    private fun parseNotesInSingleSfx(buffer: PikotBuffer): List<Note> {
        return (0..< NOTES_MAX_SIZE).map { _ ->
            val note = Note(
                pitch = buffer.readUnsignedByte().toInt(),
                waveform = buffer.read4bit().toInt(),
                volume = buffer.read4bit().toInt(),
            )
            buffer.read4bit() // effect
            note
        }
    }

    private fun parseSfxInMusic(buffer: PikotBuffer): Pair<SfxId, Sfx> {
        val sfxId = SfxId(buffer.readUnsignedByte().toInt())
        val noteBuffer = buffer.readBuffer(64)
        return sfxId to Sfx(
            notes = parseNotesInMusic(noteBuffer),
            mode = buffer.readUnsignedByte(),
            speed = buffer.readUnsignedByte(),
            loopStart = buffer.readUnsignedByte(),
            loopEnd = buffer.readUnsignedByte(),
        )
    }

    private fun parseNotesInMusic(buffer: PikotBuffer): List<Note> {
        return (0..< NOTES_MAX_SIZE).map { _ ->
            val firstByte = buffer.readByte().toInt()
            val secondByte = buffer.readByte().toInt()
            Note(
                pitch = firstByte and 63,
                waveform = firstByte.rotateRight(6) and 3 + (secondByte and 1) * 4,
                volume = secondByte.rotateRight(1) and 7,
            )
        }
    }

    private fun parsePattern(buffer: PikotBuffer): SfxPattern {
        val id1 = SfxId(buffer.readByte().toInt())
        val id2 = SfxId(buffer.readByte().toInt())
        val id3 = SfxId(buffer.readByte().toInt())
        val id4 = SfxId(buffer.readByte().toInt())
        val flags = buffer.read4bit().toInt()
        val loopStart = flags and 0b0001 != 0
        val loopEnd = flags and 0b0010 != 0
        val stop = flags and 0b0100 != 0
        return SfxPattern(
            id1,
            id2,
            id3,
            id4,
            loopStart,
            loopEnd,
            stop
        )
    }
}
