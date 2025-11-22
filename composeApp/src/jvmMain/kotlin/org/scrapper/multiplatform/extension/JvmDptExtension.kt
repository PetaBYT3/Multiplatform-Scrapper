package org.scrapper.multiplatform.extension

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.scrapper.multiplatform.action.DptAction
import org.scrapper.multiplatform.dataclass.DptResult
import org.scrapper.multiplatform.state.DptState
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Composable
actual fun DptWebPageExtension(
    modifier: Modifier,
    state: DptState,
    onAction: (DptAction) -> Unit
) {
    val scope = remember { CoroutineScope(Dispatchers.IO + SupervisorJob()) }

    val webViewNavigator = rememberWebViewNavigator()
    val webState = rememberWebViewState(dptUrlInput)

    LaunchedEffect(Unit) {
        webState.webSettings.apply {
            isJavaScriptEnabled = true
            customUserAgentString = webUserAgent
        }
    }

    //WebView
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

    //Process
    var isDataFound by remember { mutableStateOf(false) }

    LaunchedEffect(state.isStarted) {
        if (!state.isStarted) {
            scope.coroutineContext.cancelChildren()
            return@LaunchedEffect
        }

        scope.coroutineContext.cancelChildren()

        scope.launch {
            for (rawList in state.rawList) {

                webViewNavigator.loadUrl(dptUrlInput)

                waitWebViewToLoad(webState)

                val inputNikElement = """
                (function() {
                    const input = document.querySelector('form input[type="text"]');
                    if (input) {
                        input.value = '${rawList.nikNumber}';
                        input.dispatchEvent(new Event('input', {bubbles: true}));
                        return 'OK';
                    }
                    return 'NO_INPUT';
                })();
                """.trimIndent()
                webViewNavigator.awaitJavaScript(inputNikElement)

                delay(1_000)

                val bypassCaptcha = """
                window.grecaptcha = { execute: () => Promise.resolve('token') };
                if (typeof findDptb === 'function') findDptb('${rawList.nikNumber}');
                """.trimIndent()
                webViewNavigator.awaitJavaScript(bypassCaptcha)

                delay(1_000)

                val elementFind = """
                Array.from(document.querySelectorAll('div.wizard-buttons button'))
                .find(b => b.textContent.trim().includes('Pencarian'))?.click();
                """.trimIndent()
                webViewNavigator.awaitJavaScript(elementFind)

                waitWebViewToLoad(webState)

                val jsCheck = "document.querySelector('.watermarked') ? 'YES' : 'NO';"
                val jsCheckResult = webViewNavigator.awaitJavaScript(jsCheck)
                if (jsCheckResult.contains("YES", ignoreCase = true)) {
                    isDataFound = true
                } else {
                    isDataFound = false
                }

                if (!isDataFound) {
                    onAction(DptAction.Process)
                    onAction(DptAction.Failure)
                    continue
                }

                val triggerNameExtraction = """
                (function() {
                    try {
                        var bodyText = document.body.innerText || "";
                        var lines = bodyText.split('\n');
                        var foundName = "NOT_FOUND";
                        
                        for (var i = 0; i < lines.length; i++) {
                            var line = lines[i].trim();
                            if (line.toLowerCase().includes('nama pemilih')) {
                                for (var j = i + 1; j < lines.length; j++) {
                                    var nextLine = lines[j].trim();
                                    if (nextLine.length > 2 && isNaN(nextLine.replace(/\s/g, ''))) {
                                        foundName = nextLine;
                                        break;
                                    }
                                }
                            }
                            if (foundName !== "NOT_FOUND") break;
                        }
                        
                        document.title = "HASIL_NAMA:" + foundName;
                        
                    } catch (e) {
                        document.title = "HASIL_ERROR:" + e.message;
                    }
                })();
                """.trimIndent()
                webViewNavigator.awaitJavaScript(triggerNameExtraction)

                var attempts = 0
                var extractedName = ""

                while (attempts < 10) {
                    delay(500)
                    val currentTitle = webState.pageTitle ?: ""

                    if (currentTitle.startsWith("HASIL_NAMA:")) {
                        extractedName = currentTitle.removePrefix("HASIL_NAMA:").trim()
                        webViewNavigator.evaluateJavaScript("document.title = 'Cek DPT Online';")
                        break
                    } else if (currentTitle.startsWith("HASIL_ERROR:")) {
                        println("JS Error via Title: " + currentTitle)
                        break
                    }
                    attempts++
                }
                val filteredFullName = getFullName(extractedName)

                val regencyElement = "document.querySelector('.row--left')?.textContent?.trim()"
                val regencyResult = webViewNavigator.awaitJavaScript(regencyElement)
                val removedQuoteRegency = removeDoubleQuote(regencyResult)
                val filteredRegency = getRegencyName(removedQuoteRegency)

                val subdistrictElement = "document.querySelector('.row--center')?.textContent?.trim()"
                val subdistrictResult = webViewNavigator.awaitJavaScript(subdistrictElement)
                val removedQuoteSubdistrict = removeDoubleQuote(subdistrictResult)
                val filteredSubdistrict = getSubdistrictName(removedQuoteSubdistrict)

                val wardElement = "document.querySelectorAll('.row--right')[2]?.textContent?.trim()"
                val wardResult = webViewNavigator.awaitJavaScript(wardElement)
                val removedQuoteWard = removeDoubleQuote(wardResult)
                val filteredWard = getWardName(removedQuoteWard)

                val result = DptResult(
                    kpjNumber = rawList.kpjNumber,
                    nikNumber = rawList.nikNumber,
                    fullName = filteredFullName,
                    birthDate = rawList.birthDate,
                    email = rawList.email,
                    regencyName = filteredRegency,
                    subdistrictName = filteredSubdistrict,
                    wardName = filteredWard
                )

                onAction(DptAction.JsResult(result.toString()))
                onAction(DptAction.AddResult(result))
                onAction(DptAction.Process)
                onAction(DptAction.Success)
            }
            onAction(DptAction.IsStarted)
        }
    }

    DisposableEffect(Unit) {
        onDispose { scope.cancel() }
    }
}