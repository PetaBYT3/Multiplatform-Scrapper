package org.scrapper.multiplatform

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.datlag.kcef.KCEF
import dev.datlag.kcef.KCEFBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.painterResource
import org.koin.core.context.startKoin
import org.scrapper.multiplatform.extension.webUserAgent
import org.scrapper.multiplatform.module.Module
import org.scrapper.multiplatform.page.App
import org.scrapper.multiplatform.template.CustomTextContent
import org.scrapper.multiplatform.template.CustomTextMedium
import org.scrapper.multiplatform.template.CustomTextTitle
import org.scrapper.multiplatform.template.VerticalSpacer
import sun.jvmstat.monitor.MonitoredVmUtil.commandLine
import java.io.File
import java.nio.file.Paths
import kotlin.math.max
import kotlin.random.Random
import kotlin.time.Clock

fun main() = application {
    startKoin {
        modules(Module.getModules())
    }

    Window(
        onCloseRequest = {
            exitApplication()
        },
        title = "Speed Runner",
        icon = painterResource("speed_runner.png")
    ) {
        var restartRequired by remember { mutableStateOf(false) }
        var downloading by remember { mutableStateOf(0F) }
        var initialized by remember { mutableStateOf(false) }

        val baseSessionDir = File(System.getProperty("user.home"), "Speed Runner/webview-data")
        val uniqueSessionName = "session_${System.currentTimeMillis()}_${Random.nextInt(1000)}"
        val sessionDataDir = File(baseSessionDir, uniqueSessionName)

        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                KCEF.init(builder = {
                    installDir(File(System.getProperty("user.home"), "Speed Runner/kcef-bundle"))
                    progress {
                        onDownloading {
                            downloading = max(it, 0F)
                        }
                        onInitialized {
                            initialized = true
                        }
                    }
                    settings {
                        userAgentProduct = webUserAgent
                        userAgent = webUserAgent
                        cachePath = sessionDataDir.absolutePath
                    }
                }, onError = {
                    it?.printStackTrace()
                }, onRestartRequired = {
                    restartRequired = true
                })
            }
        }

        if (restartRequired) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CustomTextTitle(text = "Please Restart The App !")
            }
        } else {
            if (initialized) {
                App()
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CustomTextTitle(text = "Downloading KCEF Web View")
                    VerticalSpacer(10)
                    CustomTextContent(text = "Downloading ${(downloading * 100).toInt()}%")
                    VerticalSpacer(10)
                    LinearProgressIndicator(
                        modifier = Modifier,
                        progress = downloading
                    )
                }
            }
        }

        DisposableEffect(Unit) {
            onDispose {
                KCEF.disposeBlocking()
            }
        }
    }
}