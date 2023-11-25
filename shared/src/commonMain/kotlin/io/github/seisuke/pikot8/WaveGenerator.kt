package io.github.seisuke.pikot8

import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.pow
import kotlin.random.Random

typealias Wave = List<Float>

class WaveGenerator(
    private val sfxMap: Map<SfxId, Sfx>
) {
    private val noteLength: Int

    init {
        val firstSfx = sfxMap.firstNotNullOf { ( _, sfx) -> sfx } //TODO chose longest sfx
        noteLength = floor(SAMPLE_RATE * firstSfx.speed.toFloat() / BASE_SPEED).toInt()
    }

    fun generateWaveMap(): Map<SfxId, Wave> = sfxMap.mapValues { (_, sfx) ->
        sfxToWave(sfx, noteLength)
    }

    fun createEmptyNote(): Wave = (0..<Sfx.NOTES_MAX_SIZE).flatMap { _ ->
        createEmptyWave(noteLength)
    }

    private fun sfxToWave(sfx: Sfx, noteLength: Int): Wave {
        val audioValueList = sfx.notes.flatMap { note ->
            if (note.volume == 0) {
                createEmptyWave(noteLength)
            } else {
                noteToWave(note, noteLength)
            }
        }
        return audioValueList
    }

    private fun createEmptyWave(noteLength: Int): Wave = generateSequence { 0.0f }.take(noteLength).toList()

    private fun noteToWave(note: Note, noteLength: Int): Wave {
        //TODO cache phiList
        val freq = getFreq(note.pitch).toFloat()
        val phiList = generateSequence(0.0f) { prevValue ->
            prevValue + freq / SAMPLE_RATE
        }.take(noteLength).toList()
        val volume = note.volume.toFloat() / 8.0f
        val envelope = createEnvelopeList(noteLength) //TODO cache
        return when (note.waveform) {
            0 -> createTriangleWave(phiList)
            1 -> createTiltedSawWave(phiList)
            2 -> createSawWave(phiList)
            3 -> createSquareWave(phiList)
            4 -> createPulseWave(phiList)
            5 -> createOrganWave(phiList)
            6 -> createBrownNoiseWave(phiList.size)
            7 -> createPhaserWave(phiList)
            else -> createPhaserWave(phiList)
        }.zip(envelope).map { (wave, envelope) ->
            wave * envelope * volume
        }
    }

    private fun createEnvelopeList(noteLength: Int): Wave = (0..<noteLength).map { i ->
        val noteFactor = i.toFloat() / noteLength.toFloat()
        if (noteFactor > 1.0f - RELEASE) {
            (1.0f - noteFactor) / RELEASE
        } else {
            1.0f
        }
    }

    private fun genWhiteNoise() = Random.nextFloat() * 2 - 1

    private fun createBrownNoiseWave(length: Int): Wave {
        var lastNoise = 0.0f
        return (0..<length).map { _ ->
            val white = genWhiteNoise()
            val brown = (lastNoise + (0.02f * white)) / 1.02f
            lastNoise = brown
            brown * 3.5f
        }
    }

    private fun createTriangleWave(phiList: List<Float>): Wave = phiList.map {
        val t = it % 1
        abs(2 * t - 1) - 1.0f
    }

    private fun createTiltedSawWave(phiList: List<Float>): Wave {
        val a = 0.9f
        return phiList.map {
            val t = it % 1
            0.5f * if (t < a) {
                2.0f * t / a - 1.0f
            } else {
                2.0f * (1.0f - t) / (1.0f - a) - 1.0f
            }
        }
    }

    private fun createSawWave(phiList: List<Float>): Wave = phiList.map {
        val t = it % 1
        0.6f * if (t < 0.5) {
            t
        } else {
            t - 1.0f
        }
    }

    private fun createSquareWave(phiList: List<Float>): Wave = phiList.map {
        val t = it % 1
        if (t < 0.5) {
            0.5f
        } else {
            -0.5f
        }
    }

    private fun createPulseWave(phiList: List<Float>): Wave = phiList.map {
        val t = it % 1
        if (t < 0.3) {
            0.5f
        } else {
            -0.5f
        }
    }

    private fun createOrganWave(phiList: List<Float>): Wave = phiList.map {
        val t = it % 1
        if (t < 0.5) {
            3.0f - abs(24.0f * t - 6.0f)
        } else {
            1.0f - abs(16.0f * t - 12.0f)
        } / 9.0f
    }

    private fun createPhaserWave(phiList: List<Float>): Wave = phiList.map {
        val t = it % 1
        val k = abs(2.0 * ((it / 128.0) % 1.0) - 1.0);
        val u = (t + 0.5 * k) % 1.0;
        val ret = abs(4.0 * u - 2.0) - abs(8.0 * t - 4.0);
        (ret / 6.0).toFloat()
    }

    private fun getFreq(pitch: Int) = 65 * 2.0.pow(pitch.toDouble() / 12.0)

    companion object {
        const val SAMPLE_RATE = 22050
        private const val BASE_SPEED = 120
        private const val RELEASE = 0.07f
    }
}
