import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import io.github.seisuke.pikot8.ParseMusicResult
import io.github.seisuke.pikot8.PlatformWaveConverter
import io.github.seisuke.pikot8.Player
import io.github.seisuke.pikot8.TITLE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun main() = application {
    val windowState = rememberWindowState(width = 800.dp, height = 800.dp)

    Window(
        onCloseRequest = ::exitApplication,
        resizable = true,
        title = TITLE,
        state = windowState,
    ) {

        var sfxText by remember {
            mutableStateOf(
                "[sfx]040828405800580c500c508c590000405800580000000041580c009855000000000050405800580c500c508c5900004058005841504150985541589851a45341584c58011000002ad855da55db55df55d855da55db55df55da55db55df55e255da55db55df55e455d955dd55df55e255d955dd55df55e255d955dd55df55e255d955dd55df55e255011000002b645569556451645369536b55000000000000000000000000605562656457675569256b256e2569656955695567556751675565556051655564555f5164556255011000002c6451625564555f5500005f555d5558550000605100006251625564516957645169216b516e515d5569555f556b550000675565556051655569555f5169556b550110000028686868128686868028692a6b028692a6c028692a2b028692a2c028692a6b028692a2c2[/sfx]"
            )
        }
        var buttonEnabled by remember { mutableStateOf(true) }
        val parseMusicResult = ParseMusicResult.parse(sfxText)

        Column {
            OutlinedTextField(
                value = sfxText,
                onValueChange = { sfxText = it },
                label = { Text("sfx") }
            )

            Button(
                onClick = {
                    when (parseMusicResult) {
                        ParseMusicResult.Error -> {}
                        is ParseMusicResult.SingleSfxWave -> {
                            val platformWave = PlatformWaveConverter().convert(parseMusicResult.wave)
                            Player().playSfx(
                                platformWave
                            )
                        }
                        is ParseMusicResult.Music -> {
                            buttonEnabled = false
                            val converter = PlatformWaveConverter()
                            val platformWaveMap = parseMusicResult.waveMap.mapValues { (_, wave) ->
                                converter.convert(wave)
                            }
                            val platformEmptyWave = converter.convert(parseMusicResult.emptyWave)
                            CoroutineScope(Dispatchers.Default).launch {
                                Player().playMusic(
                                    platformWaveMap,
                                    parseMusicResult.transposedPattern,
                                    platformEmptyWave
                                )
                                buttonEnabled = true
                            }
                        }
                    }
                },
                enabled = parseMusicResult !is ParseMusicResult.Error && buttonEnabled
            ) {
                Text("▶")
            }
        }

    }
}



