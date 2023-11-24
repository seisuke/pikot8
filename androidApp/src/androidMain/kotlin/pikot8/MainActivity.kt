package pikot8

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import io.github.seisuke.pikot8.Player
import io.github.seisuke.pikot8.SfxParser
import io.github.seisuke.pikot8.WaveGenerator

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val music = SfxParser().parseMusic(
            "040828405800580c500c508c590000405800580000000041580c009855000000000050405800580c500c508c5900004058005841504150985541589851a45341584c58011000002ad855da55db55df55d855da55db55df55da55db55df55e255da55db55df55e455d955dd55df55e255d955dd55df55e255d955dd55df55e255d955dd55df55e255011000002b645569556451645369536b55000000000000000000000000605562656457675569256b256e2569656955695567556751675565556051655564555f5164556255011000002c6451625564555f5500005f555d5558550000605100006251625564516957645169216b516e515d5569555f556b550000675565556051655569555f5169556b550110000028686868128686868028692a6b028692a6c028692a2b028692a2c028692a6b028692a2c2"
        )
        val generator = WaveGenerator(music.sfxMap)
        val waveMap = generator.generateWaveMap()

        setContent {
            Column {
                Button(
                    onClick = {
                        val builder = io.github.seisuke.pikot8.PlatformWaveBuilder()
                        val platformWaveMap = waveMap.mapValues { (_, wave) ->
                            builder.convert(wave)
                        }
                        val platformEmptyWave = builder.convert(generator.generateEmptyNote())
                        Player().playMusic(
                            platformWaveMap,
                            music.transposePattern(),
                            platformEmptyWave
                        )
                    }
                ) {
                    Text("play")
                }
            }
        }
    }
}
