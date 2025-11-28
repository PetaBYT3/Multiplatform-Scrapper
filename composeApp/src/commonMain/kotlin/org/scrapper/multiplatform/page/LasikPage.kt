package org.scrapper.multiplatform.page

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.WebViewNavigator
import com.multiplatform.webview.web.WebViewState
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState
import com.valentinilk.shimmer.shimmer
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.scrapper.multiplatform.BackHandler
import org.scrapper.multiplatform.KeepScreenOn
import org.scrapper.multiplatform.action.DptAction
import org.scrapper.multiplatform.action.LasikAction
import org.scrapper.multiplatform.action.SiipBpjsAction
import org.scrapper.multiplatform.createXlsxLasik
import org.scrapper.multiplatform.dataclass.LasikResult
import org.scrapper.multiplatform.extension.awaitJavaScript
import org.scrapper.multiplatform.extension.getCurrentTime
import org.scrapper.multiplatform.extension.jvmPlatformId
import org.scrapper.multiplatform.extension.lasikInputUrl
import org.scrapper.multiplatform.extension.lasikPath
import org.scrapper.multiplatform.extension.quoteSafeString
import org.scrapper.multiplatform.extension.removeDoubleQuote
import org.scrapper.multiplatform.extension.waitWebViewToLoad
import org.scrapper.multiplatform.extension.webUserAgent
import org.scrapper.multiplatform.platformId
import org.scrapper.multiplatform.saveFile
import org.scrapper.multiplatform.state.LasikState
import org.scrapper.multiplatform.template.CustomBottomSheetMessageComposable
import org.scrapper.multiplatform.template.CustomIconButton
import org.scrapper.multiplatform.template.CustomTextContent
import org.scrapper.multiplatform.template.CustomTextTitle
import org.scrapper.multiplatform.template.HorizontalSpacer
import org.scrapper.multiplatform.template.Success
import org.scrapper.multiplatform.template.VerticalSpacer
import org.scrapper.multiplatform.template.Warning
import org.scrapper.multiplatform.viewmodel.LasikViewModel
import kotlin.collections.isNotEmpty
import kotlin.text.trimIndent

@Composable
fun LasikPage(
    navController: NavController,
    viewModel: LasikViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onAction = viewModel::onAction

    Scaffold(
        navController = navController,
        state = state,
        onAction = onAction
    )

    BackHandler(
        enabled = true,
        onBack = {
            if (state.isStarted) {
                onAction(LasikAction.MessageDialog(
                    color = Warning,
                    icon = Icons.Filled.Warning,
                    message = "Stop The Process First !"
                ))
            } else {
                navController.popBackStack()
            }
        }
    )

    KeepScreenOn(state.isStarted)

    if (state.questionBottomSheet) {
        CustomBottomSheetMessageComposable(
            title = "How To Use ?",
            content = {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(15.dp)
                    ) {
                        CustomTextContent(text = "How to start the LASIK auto check ?")
                        CustomTextContent(text = "1. Wait web to be loaded")
                        CustomTextContent(text = "2. Select .xlsx file")
                        CustomTextContent(text = "3. Click start button")
                        VerticalSpacer(10)
                        CustomTextContent(text = "Note !")
                        CustomTextContent(text = "For better result, before you start the automation you better close the banner or pop-up window that appear after at first load")
                        VerticalSpacer(10)
                        CustomTextContent(text = "Where the result saved ?")
                        CustomTextContent(text = "Android : Documents / Speed Runner / LASIK")
                        CustomTextContent(text = "Windows : C: / Users / (Users Name) / Documents / Speed Runner / LASIK")
                    }
                }
            },
            onDismiss = { onAction(LasikAction.QuestionBottomSheet) }
        )
    }
}

@Composable
private fun Scaffold(
    navController: NavController,
    state: LasikState,
    onAction: (LasikAction) -> Unit
) {
    val webViewNavigator = rememberWebViewNavigator()

    Scaffold(
        topBar = {
            TopBar(
                navController = navController,
                webViewNavigator = webViewNavigator,
                state = state,
                onAction = onAction
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Content(
                    webViewNavigator = webViewNavigator,
                    state = state,
                    onAction = onAction
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    navController: NavController,
    webViewNavigator: WebViewNavigator,
    state: LasikState,
    onAction: (LasikAction) -> Unit
) {
    TopAppBar(
        navigationIcon = {
            CustomIconButton(
                imageVector = Icons.Filled.ArrowBack,
                onClick = {
                    if (state.isStarted) {
                        onAction(LasikAction.MessageDialog(
                            color = Warning,
                            icon = Icons.Filled.Warning,
                            message = "Stop The Process First !"
                        ))
                    } else {
                        navController.popBackStack()
                    }
                }
            )
        },
        title = { Text(text = "LASIK") },
        actions = {
            Row() {
                CustomIconButton(
                    imageVector = Icons.Filled.QuestionMark,
                    onClick = { onAction(LasikAction.QuestionBottomSheet) }
                )
            }
        }
    )
}

@Composable
private fun Content(
    webViewNavigator: WebViewNavigator,
    state: LasikState,
    onAction: (LasikAction) -> Unit
) {
    val filePicker = rememberFilePickerLauncher(
        type = PickerType.File(listOf("xlsx")),
        onResult = { uri ->
            onAction(LasikAction.XlsxFile(uri))
        }
    )
    val webState = rememberWebViewState(lasikInputUrl)

    val webViewModifier =
        if (state.questionBottomSheet)
            Modifier.size(0.dp)
        else
            Modifier.fillMaxSize()

    LaunchedEffect(Unit) {
        webState.webSettings.apply {
            isJavaScriptEnabled = true
            customUserAgentString = webUserAgent
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 15.dp),
    ) {
        Card(
            modifier = Modifier
                .weight(1f),
        ) {
            Box(
                modifier = if (platformId == jvmPlatformId) webViewModifier else Modifier.fillMaxSize()
            ) {
                WebView(
                    modifier = Modifier
                        .fillMaxSize(),
                    state = webState,
                    navigator = webViewNavigator
                )
                if (webState.loadingState is LoadingState.Loading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .shimmer()
                    ) { Column(Modifier.fillMaxSize().background(Color.LightGray)) {} }
                }
                AutoCheck(
                    webViewNavigator = webViewNavigator,
                    webViewState = webState,
                    state = state,
                    onAction = onAction
                )
                LaunchedEffect(state.isStarted) {
                    if (!state.isStarted && state.lasikResult.isNotEmpty()) {
                        val xlsxFile = createXlsxLasik(state.lasikResult)
                        if (xlsxFile != null) {
                            saveFile(
                                fileName = "LASIK Result ${getCurrentTime()}.xlsx",
                                fileBytes = xlsxFile,
                                filePath = lasikPath
                            )
                            onAction(LasikAction.MessageDialog(
                                color = Success,
                                icon = Icons.Filled.Check,
                                message = "File Saved !"
                            ))
                        }
                    }
                }
            }
        }
        VerticalSpacer(10)
        Card() {
            Column(
                modifier = Modifier
                    .padding(horizontal = 15.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CustomTextTitle(text = "Auto Checker")
                    Spacer(Modifier.weight(1f))
                    val animateRotation by animateFloatAsState(
                        targetValue = if (state.extendedMenu) 0f else 180f,
                        animationSpec = tween(500)
                    )
                    IconButton(
                        onClick = { onAction(LasikAction.ExtendedMenu) }
                    ) {
                        Icon(
                            modifier = Modifier
                                .rotate(animateRotation),
                            imageVector = Icons.Filled.ArrowDownward,
                            contentDescription = null
                        )
                    }
                }
                AnimatedVisibility(
                    visible = state.extendedMenu,
                    content = {
                        Column() {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.InsertDriveFile,
                                    contentDescription = null
                                )
                                HorizontalSpacer(10)
                                CustomTextContent(state.xlsxName ?: "No .xlsx File Selected !")
                                Spacer(Modifier.weight(1f))
                                IconButton(
                                    onClick = {
                                        if (state.xlsxFile != null) {
                                            if (state.isStarted) {
                                                onAction(LasikAction.MessageDialog(
                                                    color = Warning,
                                                    icon = Icons.Filled.Warning,
                                                    message = "Stop The Process First !"
                                                ))
                                            } else {
                                                onAction(LasikAction.DeleteXlsx)
                                            }
                                        } else {
                                            filePicker.launch()
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (state.xlsxFile != null) Icons.Filled.Delete else Icons.Filled.AttachFile,
                                        contentDescription = null
                                    )
                                }
                            }
                            AnimatedVisibility(
                                visible = state.xlsxFile != null,
                                content = {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.List,
                                                    contentDescription = null
                                                )
                                                HorizontalSpacer(10)
                                                CustomTextContent(text = "${state.rawList.size} Data Detected")
                                                Spacer(Modifier.weight(1f))
                                            }
                                            VerticalSpacer(5)
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Timelapse,
                                                    contentDescription = null
                                                )
                                                HorizontalSpacer(10)
                                                CustomTextContent(text = "${state.process} / ${state.rawList.size} Process")
                                            }
                                            VerticalSpacer(5)
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Check,
                                                    contentDescription = null
                                                )
                                                HorizontalSpacer(10)
                                                CustomTextContent(text = "${state.success} Success")
                                            }
                                            VerticalSpacer(5)
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Close,
                                                    contentDescription = null
                                                )
                                                HorizontalSpacer(10)
                                                CustomTextContent(text = "${state.failure} Failed")
                                            }
                                        }
                                        AnimatedVisibility(
                                            enter = fadeIn(),
                                            exit = fadeOut(),
                                            visible = state.dialogVisibility,
                                            content = {
                                                Column(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(25))
                                                        .background(state.dialogColor)
                                                ) {
                                                    Column(
                                                        modifier = Modifier
                                                            .padding(10.dp)
                                                            .width(100.dp),
                                                        verticalArrangement = Arrangement.Center,
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {
                                                        Icon(
                                                            imageVector = state.iconDialog,
                                                            contentDescription = null,
                                                            tint = Color.Black
                                                        )
                                                        VerticalSpacer(10)
                                                        Text(
                                                            text = state.messageDialog,
                                                            textAlign = TextAlign.Center,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            color = Color.Black
                                                        )
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }
                            )
                            VerticalSpacer(10)
                            Button(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                onClick = { onAction(LasikAction.IsStarted) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (state.isStarted) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    },
                                    contentColor = if (state.isStarted) {
                                        MaterialTheme.colorScheme.onError
                                    } else {
                                        MaterialTheme.colorScheme.onPrimary
                                    }
                                ),
                                enabled = state.rawList.isNotEmpty()
                            ) {
                                Icon(
                                    imageVector = if (state.isStarted) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                                    contentDescription = null
                                )
                                Text(text = if (state.isStarted) "Stop" else "Start")
                            }
                        }
                    }
                )
            }
        }
        VerticalSpacer(10)
    }
}

@Composable
private fun AutoCheck(
    webViewNavigator: WebViewNavigator,
    webViewState: WebViewState,
    state: LasikState,
    onAction: (LasikAction) -> Unit
) {
    val scope = remember { CoroutineScope(Dispatchers.IO + SupervisorJob()) }

    LaunchedEffect(state.isStarted) {
        if (!state.isStarted) {
            scope.coroutineContext.cancelChildren()
            return@LaunchedEffect
        }

        scope.coroutineContext.cancelChildren()

        while (true) {
            try {
                webViewNavigator.loadUrl(lasikInputUrl)
                waitWebViewToLoad(webViewState)
                break
            } catch (e: TimeoutCancellationException) {
                onAction(LasikAction.MessageDialog(
                    color = Warning,
                    icon = Icons.Filled.RestartAlt,
                    message = "Timeout Loading Web, Retrying..."
                ))
                continue
            } catch (e: Exception) {
                onAction(LasikAction.MessageDialog(
                    color = Warning,
                    icon = Icons.Filled.RestartAlt,
                    message = "Error Loading Web, Retrying..."
                ))
                continue
            }
        }

        scope.launch {
            for (rawString in state.rawList) {
                while (true) {
                    try {
                        val safeNik = quoteSafeString(rawString.nikNumber)
                        val nikElement = """
                        (function() {
                            var nikInput = document.querySelector('input[placeholder="Isi Nomor E-KTP"]');
                            if (nikInput) {
                                nikInput.value = '$safeNik';
                                nikInput.dispatchEvent(new Event('input', { bubbles: true }));
                                nikInput.dispatchEvent(new Event('change', { bubbles: true }));
                                return true;
                            }
                            return false;
                        })();
                        """.trimIndent()
                        webViewNavigator.awaitJavaScript(nikElement)

                        val safeKpj = quoteSafeString(rawString.kpjNumber)
                        val kpjElement = """
                        (function() {
                            var kpjInput = document.querySelector('input[placeholder="Isi Nomor KPJ"]');
                            if (kpjInput) {
                                kpjInput.value = '$safeKpj';
                                kpjInput.dispatchEvent(new Event('input', { bubbles: true }));
                                kpjInput.dispatchEvent(new Event('change', { bubbles: true }));
                                return true;
                            }
                            return false;
                        })();
                        """.trimIndent()
                        webViewNavigator.awaitJavaScript(kpjElement)

                        val safeName = quoteSafeString(rawString.fullName)
                        onAction(LasikAction.Debugging(safeName))
                        val nameElement = """
                        (function() {
                            var namaInput = document.querySelector('input[placeholder="Isi Nama sesuai KTP"]');
                            if (namaInput) {
                                namaInput.value = '$safeName';
                                namaInput.dispatchEvent(new Event('input', { bubbles: true }));
                                namaInput.dispatchEvent(new Event('change', { bubbles: true }));
                                return true;
                            }
                            return false;
                        })();
                        """.trimIndent()
                        webViewNavigator.awaitJavaScript(nameElement)

                        delay(1000)

                        val btnNextElement = """
                        Array.from(document.querySelectorAll('div.wizard-buttons button'))
                        .find(b => b.textContent.trim().includes('BERIKUTNYA'))?.click();
                        """.trimIndent()
                        webViewNavigator.awaitJavaScript(btnNextElement)

                        delay(4_000)

                        val resultElement = """
                        document.querySelector('.swal2-content').innerText;
                        """.trimIndent()
                        val resultDetection = webViewNavigator.awaitJavaScript(resultElement)

                        if (resultDetection.contains("JMO", ignoreCase = true)) {
                            onAction(LasikAction.Debugging("Berhasil"))
                            onAction(LasikAction.Process)
                            onAction(LasikAction.Success)
                        } else {
                            onAction(LasikAction.Debugging("Gagal"))
                            onAction(LasikAction.Process)
                            onAction(LasikAction.Failure)
                            break
                        }

                        val result = LasikResult(
                            kpjNumber = rawString.kpjNumber,
                            nikNumber = rawString.nikNumber,
                            fullName = rawString.fullName,
                            birthDate = rawString.birthDate,
                            email = rawString.email,
                            regencyName = rawString.regencyName,
                            subdistrictName = rawString.subdistrictName,
                            wardName = rawString.wardName,
                            lasikResult = "Berhasil"
                        )
                        onAction(LasikAction.AddResult(result))
                        break
                    } catch (e: TimeoutCancellationException) {
                        onAction(LasikAction.MessageDialog(
                            color = Warning,
                            icon = Icons.Filled.RestartAlt,
                            message = "Timeout !, Retrying..."
                        ))
                        continue
                    } catch (e: Exception) {
                        onAction(LasikAction.MessageDialog(
                            color = Warning,
                            icon = Icons.Filled.RestartAlt,
                            message = "Error !. Retrying..."
                        ))
                        continue
                    }
                }
            }
            onAction(LasikAction.IsStarted)
        }
    }

    DisposableEffect(Unit) {
        onDispose { scope.cancel() }
    }
}