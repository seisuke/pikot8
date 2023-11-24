package io.github.seisuke.pikot8

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Player(val context: AudioContext) {
    fun playMusic(
        platformWaveMap: Map<SfxId, PlatformWave>,
        transposedPattern: List<List<SfxId>>,
        emptyWave: PlatformWave
    ) {
        transposedPattern.map { pattern ->
            CoroutineScope(Dispatchers.Main).launch {
                val zippedId = pattern.zipWithNext() + (pattern.last() to null)
                val zippedAudioBufferWithHasNextId = zippedId.map { (currentId, nextId) ->
                    val audioBuffer = platformWaveMap[currentId] ?: emptyWave
                    audioBuffer to (nextId != null)
                }
                playWave(zippedAudioBufferWithHasNextId)
            }
        }
    }

    private fun playWave(
        zippedAudioBufferWithHasNextId: List<Pair<AudioBuffer, Boolean>>
    ) {
        val (audioBuffer, hasNextId) = zippedAudioBufferWithHasNextId.firstOrNull() ?: return
        val source = context.createBufferSource()
        source.buffer = audioBuffer
        source.connect(context.destination)
        if (hasNextId) {
            source.onended = {
                playWave(zippedAudioBufferWithHasNextId.drop(1))
            }
        }
        source.start()
    }
}
