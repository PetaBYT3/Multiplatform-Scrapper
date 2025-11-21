package org.scrapper.multiplatform

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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

        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                KCEF.init(builder = {
                    installDir(File("kcef-bundle"))
                    progress {
                        onDownloading {
                            downloading = max(it, 0F)
                        }
                        onInitialized {
                            initialized = true
                        }
                    }
                    settings {
                        val userHome = System.getProperty("user.home")
                        val cacheDir = Paths.get(userHome, ".my-app-cache").toString()
                        this.cachePath = cacheDir
                        userAgent = webUserAgent

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
                    CustomTextContent(text = "Downloading $downloading %")
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