package io.github.seisuke.pikot8

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class Player {

    fun playMusic(
        platformWaveMap: Map<SfxId, PlatformWave>,
        transposedPattern: List<List<SfxId>>,
        emptyWave: PlatformWave
    ) {
        val bufferSize = AudioTrack.getMinBufferSize(
            WaveGenerator.SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        CoroutineScope(Dispatchers.IO).launch {
            val mutex = Mutex(true)
            (0 ..< 4).map { index ->
                val audioTrack = createAudioTrack(bufferSize)
                audioTrack.play()
                audioTrack to transposedPattern[index]
            }.map { (audioTrack, pattern) ->
                async {
                    mutex.withLock {  }
                    pattern.forEach { sfxId ->
                        val byteArray = platformWaveMap[sfxId] ?: emptyWave
                        audioTrack.write(byteArray, 0, byteArray.size)
                    }
                    audioTrack.stop()
                    audioTrack.release()
                }
            }
            mutex.unlock()
        }
    }

    private fun createAudioTrack(bufferSize: Int): AudioTrack {
        return AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(WaveGenerator.SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .build()
    }

}
