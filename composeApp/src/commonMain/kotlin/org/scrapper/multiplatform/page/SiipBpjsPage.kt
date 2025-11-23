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
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.scrapper.multiplatform.BackHandler
import org.scrapper.multiplatform.action.SiipBpjsAction
import org.scrapper.multiplatform.createXlsxSiip
import org.scrapper.multiplatform.dataclass.SiipResult
import org.scrapper.multiplatform.extension.SiipBPJSInput
import org.scrapper.multiplatform.extension.SiipBPJSLoginUrl
import org.scrapper.multiplatform.extension.awaitJavaScript
import org.scrapper.multiplatform.extension.getCurrentTime
import org.scrapper.multiplatform.extension.jvmPlatformId
import org.scrapper.multiplatform.extension.removeDoubleQuote
import org.scrapper.multiplatform.extension.siipPath
import org.scrapper.multiplatform.extension.waitWebViewToLoad
import org.scrapper.multiplatform.extension.webUserAgent
import org.scrapper.multiplatform.platformId
import org.scrapper.multiplatform.saveFile
import org.scrapper.multiplatform.state.SiipBpjsState
import org.scrapper.multiplatform.template.CustomAlertDialogMessage
import org.scrapper.multiplatform.template.CustomBottomSheetMessageComposable
import org.scrapper.multiplatform.template.CustomIconButton
import org.scrapper.multiplatform.template.CustomTextContent
import org.scrapper.multiplatform.template.CustomTextTitle
import org.scrapper.multiplatform.template.HorizontalSpacer
import org.scrapper.multiplatform.template.Success
import org.scrapper.multiplatform.template.VerticalSpacer
import org.scrapper.multiplatform.template.Warning
import org.scrapper.multiplatform.viewmodel.SiipBpjsViewModel

@Composable
fun SiipBpjsPage(
    navController: NavController,
    viewModel: SiipBpjsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onAction = viewModel::onAction

    LaunchedEffect(Unit) {
        viewModel.initData()
    }

    BackHandler(
        enabled = true,
        onBack = {
            if (state.isStarted) {
                onAction(SiipBpjsAction.MessageDialog(
                    color = Warning,
                    icon = Icons.Filled.Warning,
                    message = "Stop The Process First !"
                ))
            } else {
                navController.popBackStack()
            }
        }
    )

    Scaffold(
        navController = navController,
        state = state,
        onAction = onAction
    )

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
                        CustomTextContent(text = "How to start the SIIP BPJS auto check ?")
                        CustomTextContent(text = "1. Wait web page to be loaded")
                        CustomTextContent(text = "2. Login into SIIP BPJS web")
                        CustomTextContent(text = "3. Select .xlsx file")
                        CustomTextContent(text = "4. Click start button")
                        VerticalSpacer(10)
                        CustomTextContent(text = "Where the result saved ?")
                        CustomTextContent(text = "Android : Documents / Speed Runner / SIIP BPJS")
                        CustomTextContent(text = "Windows : C: / Users / (Users Name) / Documents / Speed Runner / SIIP BPJS")
                    }
                }
            },
            onDismiss = { onAction(SiipBpjsAction.QuestionBottomSheet) }
        )
    }

    if (state.settingsBottomSheet) {
        CustomBottomSheetMessageComposable(
            title = "Settings",
            content = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = state.getGmail,
                        onCheckedChange = { onAction(SiipBpjsAction.GetGmail) }
                    )
                    CustomTextContent(text = "Get Gmail ( @gmail.com )")
                }
            },
            onDismiss = { onAction(SiipBpjsAction.SettingsBottomSheet) }
        )
    }
}

@Composable
private fun Scaffold(
    navController: NavController,
    state: (SiipBpjsState),
    onAction: (SiipBpjsAction) -> Unit
) {
    val webViewNavigator = rememberWebViewNavigator()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
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
                    onAction = onAction,
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
    state: (SiipBpjsState),
    onAction: (SiipBpjsAction) -> Unit
) {
    TopAppBar(
        navigationIcon = {
            CustomIconButton(
                imageVector = Icons.Filled.ArrowBack,
                onClick = {
                    if (state.isStarted) {
                        onAction(SiipBpjsAction.MessageDialog(
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
        title = { Text(text = "SIIP BPJS") },
        actions = {
            Row() {
                CustomIconButton(
                    imageVector = Icons.Filled.RestartAlt,
                    onClick = { webViewNavigator.loadUrl(SiipBPJSLoginUrl) }
                )
                CustomIconButton(
                    imageVector = Icons.Filled.QuestionMark,
                    onClick = { onAction(SiipBpjsAction.QuestionBottomSheet) }
                )
            }
        }
    )
}

@Composable
private fun Content(
    webViewNavigator: WebViewNavigator,
    state: (SiipBpjsState),
    onAction: (SiipBpjsAction) -> Unit
) {
    val filePicker = rememberFilePickerLauncher(
        type = PickerType.File(listOf("xlsx")),
        onResult = { uri ->
            onAction(SiipBpjsAction.XlsxFile(uri))
        }
    )
    val webState = rememberWebViewState(SiipBPJSLoginUrl)

    val webViewModifier =
        if (state.settingsBottomSheet || state.questionBottomSheet)
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
                    if (!state.isStarted && state.siipResult.isNotEmpty()) {
                        val xlsxFile = createXlsxSiip(state.siipResult)
                        if (xlsxFile != null) {
                            saveFile(
                                fileName = "SIIP Result ${getCurrentTime()}.xlsx",
                                fileBytes = xlsxFile,
                                filePath = siipPath
                            )
                            onAction(SiipBpjsAction.MessageDialog(
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
                    Row {
                        CustomIconButton(
                            imageVector = Icons.Filled.Settings,
                            onClick = { onAction(SiipBpjsAction.SettingsBottomSheet) }
                        )
                        IconButton(
                            onClick = { onAction(SiipBpjsAction.ExtendedMenu) }
                        ) {
                            Icon(
                                modifier = Modifier
                                    .rotate(animateRotation),
                                imageVector = Icons.Filled.ArrowDownward,
                                contentDescription = null
                            )
                        }
                    }
                }
                AnimatedVisibility(
                    visible = state.extendedMenu,
                    content = {
                        if (state.isLoggedIn) {
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
                                                    onAction(SiipBpjsAction.MessageDialog(
                                                        color = Warning,
                                                        icon = Icons.Filled.Warning,
                                                        message = "Stop The Process First !"
                                                    ))
                                                } else {
                                                    onAction(SiipBpjsAction.DeleteXlsx)
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
                                    onClick = { onAction(SiipBpjsAction.IsStarted) },
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
                        } else {
                            Row(
                                modifier = Modifier
                                    .padding(10.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Warning,
                                    contentDescription = null
                                )
                                HorizontalSpacer(10)
                                CustomTextContent(text = "Please Login Into SIIP BPJS First")
                            }
                        }
                    }
                )
            }
        }
        Text(text = state.debugging)
        VerticalSpacer(10)
    }
}

@Composable
private fun LoginDetection(
    webViewNavigator: WebViewNavigator,
    state: (SiipBpjsState),
    onAction: (SiipBpjsAction) -> Unit
) {
    val loggedDetection = "document.getElementById('form-login') != null"
    webViewNavigator.evaluateJavaScript(script = loggedDetection) {
        onAction(SiipBpjsAction.IsLoggedIn(it.toBoolean()))
    }
    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) {
            webViewNavigator.loadUrl("https://sipp.bpjsketenagakerjaan.go.id/tenaga-kerja/baru/form-tambah-tk-individu")
        }
        delay(1000)
    }
}

@Composable
private fun AutoCheck(
    webViewNavigator: WebViewNavigator,
    webViewState: WebViewState,
    state: (SiipBpjsState),
    onAction: (SiipBpjsAction) -> Unit
) {
    val scope = remember { CoroutineScope(Dispatchers.IO + SupervisorJob()) }

    var kpjNumber by remember { mutableStateOf("") }
    var nikNumber by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    var isKpjDetected by remember { mutableStateOf(false) }

    LaunchedEffect(state.isStarted) {
        if (!state.isStarted) {
            scope.coroutineContext.cancelChildren()
            return@LaunchedEffect
        }

        scope.coroutineContext.cancelChildren()

        scope.launch {
            for (rawString in state.rawList) {

                while (true) {
                    kpjNumber = ""
                    nikNumber = ""
                    birthDate = ""
                    email = ""

                    webViewNavigator.loadUrl(SiipBPJSInput)
                    waitWebViewToLoad(webViewState = webViewState)

                    val doneButton = "Array.from(document.querySelectorAll('button')).find(el => el.textContent.includes('Sudah'))?.click();"
                    webViewNavigator.evaluateJavaScript(doneButton)
                    onAction(SiipBpjsAction.Debugging("Klik Tombol Sudah"))

                    delay(500)

                    val kpjTextField = "document.querySelector('input[placeholder=\"Input No KPJ\"]').value = '$rawString';"
                    webViewNavigator.evaluateJavaScript(kpjTextField)
                    onAction(SiipBpjsAction.Debugging("Input KPJ"))

                    delay(500)

                    val btnNext = "Array.from(document.querySelectorAll('button')).find(el => el.textContent.includes('Lanjut'))?.click();"
                    webViewNavigator.evaluateJavaScript(btnNext)
                    onAction(SiipBpjsAction.Debugging("Klik Tombol Lanjut"))

                    delay(500)

                    waitWebViewToLoad(webViewState = webViewState)

                    val resultElement = "document.querySelector('.swal2-content').textContent;"
                    val resultDialog = webViewNavigator.awaitJavaScript(resultElement)

                    if (resultDialog.contains("Terlalu banyak percobaan yang gagal dalam waktu singkat", ignoreCase = true)) {
                        onAction(SiipBpjsAction.MessageDialog(
                            color = Warning,
                            icon = Icons.Filled.RestartAlt,
                            message = "Retrying..."
                        ))
                        onAction(SiipBpjsAction.Debugging("Coldown Terdeteksi"))
                        continue
                    }
                    onAction(SiipBpjsAction.Debugging("Coldown Tidak Terdeteksi"))

                    val successDetection = "document.querySelector('.swal2-title').textContent;"
                    val successResult = webViewNavigator.awaitJavaScript(successDetection)
                    onAction(SiipBpjsAction.Debugging("Deteksi Hasil KPJ"))

                    delay(500)

                    if (successResult.contains("Berhasil!", ignoreCase = true)) {
                        isKpjDetected = true
                    } else {
                        isKpjDetected = false
                    }

                    if (!isKpjDetected) {
                        onAction(SiipBpjsAction.Failure)
                        onAction(SiipBpjsAction.Process)
                        onAction(SiipBpjsAction.Debugging("KPJ Gagal"))
                        delay(10_000)
                        break
                    }

                    onAction(SiipBpjsAction.Debugging("KPJ Berhasil"))
                    delay(10_000)

                    kpjNumber = rawString

                    val continueButton = "document.querySelector('.swal2-confirm').click();"
                    webViewNavigator.awaitJavaScript(continueButton)
                    onAction(SiipBpjsAction.Debugging("Klik Tombol Konfirmasi"))

                    delay(500)

                    waitWebViewToLoad(webViewState = webViewState)

                    onAction(SiipBpjsAction.Debugging("Mengekstrak Data"))

                    val nikElement = "document.getElementById('no_identitas').value;"
                    val nikResult = webViewNavigator.awaitJavaScript(nikElement)

                    val removedQuoteNik = removeDoubleQuote(nikResult)
                    nikNumber = removedQuoteNik

                    val birthDateElement = "document.getElementById('tgl_lahir').value;"
                    val birthDateResult = webViewNavigator.awaitJavaScript(birthDateElement)

                    val removedQuoteBirthDate = removeDoubleQuote(birthDateResult)
                    birthDate = removedQuoteBirthDate

                    val emailElement = "document.getElementById('email').value;"
                    val emailResult = webViewNavigator.awaitJavaScript(emailElement)

                    val removedQuoteEmail = removeDoubleQuote(emailResult)
                    if (state.getGmail) {
                        email = removedQuoteEmail
                    } else {
                        if (removedQuoteEmail.contains("@gmail.com", ignoreCase = true)) {
                            email = ""
                        } else {
                            email = removedQuoteEmail
                        }
                    }

                    delay(500)

                    val result = SiipResult(
                        kpjNumber = kpjNumber,
                        fullName = "",
                        nikNumber = nikNumber,
                        birthDate = birthDate,
                        email = email
                    )

                    onAction(SiipBpjsAction.AddResult(result = result))
                    onAction(SiipBpjsAction.Success)
                    onAction(SiipBpjsAction.Process)
                    break
                }
            }
            onAction(SiipBpjsAction.IsStarted)
        }
    }

    DisposableEffect(Unit) {
        onDispose { scope.cancel() }
    }
}

