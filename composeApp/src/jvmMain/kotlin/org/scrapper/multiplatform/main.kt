package org.scrapper.multiplatform

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.datlag.kcef.KCEF
import dev.datlag.kcef.KCEFBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.painterResource
import org.koin.core.context.startKoin
import org.scrapper.multiplatform.extension.webUserAgent
import org.scrapper.multiplatform.module.Module
import org.scrapper.multiplatform.page.App
import org.scrapper.multiplatform.template.CustomTextContent
import org.scrapper.multiplatform.template.CustomTextHint
import org.scrapper.multiplatform.template.CustomTextMedium
import org.scrapper.multiplatform.template.CustomTextTitle
import org.scrapper.multiplatform.template.HorizontalSpacer
import org.scrapper.multiplatform.template.VerticalSpacer
import org.scrapper.multiplatform.theme.AppTheme
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

    AppTheme {
        Window(
            onCloseRequest = {
                exitApplication()
            },
            title = "Speed Runner",
            icon = painterResource("speed_runner.png")
        ) {
            val scope = rememberCoroutineScope()

            var alertVisibility by remember { mutableStateOf(false) }
            var alertMessage by remember { mutableStateOf("") }

            var title by remember { mutableStateOf("Speed Runner") }

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

            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CustomTextTitle(text = title)
                    VerticalSpacer(10)
                    CustomTextContent(text = "${downloading}%")
                    VerticalSpacer(10)
                    val animateLoadingState by animateFloatAsState(
                        targetValue = downloading,
                        animationSpec = tween()
                    )
                    LinearProgressIndicator(
                        progress = animateLoadingState / 100,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        trackColor = MaterialTheme.colorScheme.primary
                    )
                }
                if (restartRequired) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                    ) {
                        AnimatedVisibility(
                            visible = alertVisibility,
                            content = {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(15))
                                        .background(org.scrapper.multiplatform.template.Warning),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Warning,
                                        contentDescription = null
                                    )
                                    HorizontalSpacer(10)
                                    CustomTextHint(text = alertMessage)
                                }
                            }
                        )
                        HorizontalSpacer(10)
                        Button(
                            onClick = {
                                val baseDir = File(System.getProperty("user.home"), "Speed Runner")
                                if (baseDir.exists()) {
                                    baseDir.deleteRecursively()
                                    scope.launch {
                                        alertMessage = "Success"
                                        alertVisibility = true
                                        delay(5000)
                                        alertVisibility = false
                                    }
                                } else {
                                    scope.launch {
                                        alertMessage = "No Cache Or Corrupted Files Detected"
                                        alertVisibility = true
                                        delay(5000)
                                        alertVisibility = false
                                    }
                                }
                            }
                        ) {
                            Text(text = "Delete Cache & Remove Corrupted Files")
                        }
                    }
                }
            }

            if (restartRequired) {
                title = "Please Restart The App !"
            } else {
                if (initialized) {
                    App()
                } else {
                    title = "Downloading KCEF Web View"
                }
            }

            DisposableEffect(Unit) {
                onDispose {
                    KCEF.disposeBlocking()
                }
            }
        }
    }
}