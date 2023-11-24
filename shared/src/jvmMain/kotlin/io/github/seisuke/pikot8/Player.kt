package io.github.seisuke.pikot8

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem

class Player {
    fun playMusic(
        platformWaveMap: Map<SfxId, PlatformWave>,
        transposedPattern: List<List<SfxId>>,
        emptyWave: PlatformWave
    ) {
        val audioFormat = AudioFormat(
            WaveGenerator.SAMPLE_RATE.toFloat(),
            16,
            1,
            true,
            false
        )

        (0 ..< 4).map { index ->
            AudioSystem.getSourceDataLine(audioFormat) to transposedPattern[index]
        }.forEach { (line, pattern) ->
            CoroutineScope(Dispatchers.IO).launch {
                line.use { line ->
                    line.open(audioFormat)
                    line.start()
                    pattern.forEach { sfxId ->
                        val byteArray = platformWaveMap[sfxId] ?: emptyWave
                        line.write(byteArray, 0, byteArray.size)
                        line.drain()
                    }
                }
            }
        }
    }
}
