package io.github.seisuke.pikot8

actual typealias PlatformWave = AudioBuffer

actual class PlatformWaveBuilder(val context: AudioContext) {

    fun convert(wave: Wave): PlatformWave {
        val audioBuffer = context.createBuffer(
            1,
            wave.size,
            WaveGenerator.SAMPLE_RATE
        )
        val channel = audioBuffer.getChannelData(0)
        channel.set(wave.toTypedArray())
        return audioBuffer
    }
}
