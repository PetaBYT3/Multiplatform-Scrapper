package org.scrapper.multiplatform.extension

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import org.scrapper.multiplatform.template.Warning

@Composable
actual fun DptWebPageExtension(
    modifier: Modifier,
    state: DptState,
    onAction: (DptAction) -> Unit
) {
    val scope = remember { CoroutineScope(Dispatchers.IO + SupervisorJob()) }

    val advanceWebViewControl = rememberAdvanceViewControl()
    var isLoading by remember { mutableStateOf(true) }

    //WebView
    AdvanceWebViewComposable(
        modifier = modifier
            .fillMaxSize(),
        url = dptUrlInput,
        advanceWebViewControl = advanceWebViewControl,
        onPageStarted = {
            isLoading = true
        },
        onPageFinished = {
            isLoading = false
        },
        onError = {
            advanceWebViewControl.loadUrl(dptUrlInput)
        }
    )
    if (isLoading) {
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
                while (true) {
                    advanceWebViewControl.loadUrl(dptUrlInput)
                    val preloadWeb = advanceWebViewControl.waitWebToLoad(isLoading)
                    if (!preloadWeb) {
                        onAction(DptAction.MessageDialog(
                            color = Warning,
                            icon = Icons.Filled.RestartAlt,
                            message = "Web Fail To Load !"
                        ))
                        continue
                    }

                    val safeNik = quoteSafeString(rawList.nikNumber)
                    val inputNikElement = """
                    (function() {
                        const input = document.querySelector('form input[type="text"]');
                        if (input) {
                            input.value = '$safeNik';
                            input.dispatchEvent(new Event('input', {bubbles: true}));
                            return 'OK';
                        }
                        return 'NO_INPUT';
                    })();
                    """.trimIndent()
                    advanceWebViewControl.awaitJavaScript(inputNikElement)

                    delay(1_000)

                    val bypassCaptcha = """
                    window.grecaptcha = { execute: () => Promise.resolve('token') };
                    if (typeof findDptb === 'function') findDptb('${rawList.nikNumber}');
                    """.trimIndent()
                    advanceWebViewControl.awaitJavaScript(bypassCaptcha)

                    delay(1_000)

                    val elementFind = """
                    Array.from(document.querySelectorAll('div.wizard-buttons button'))
                    .find(b => b.textContent.trim().includes('Pencarian'))?.click();
                    """.trimIndent()
                    advanceWebViewControl.awaitJavaScript(elementFind)

                    val resultWebLoad = advanceWebViewControl.waitWebToLoad(isLoading)
                    if (!resultWebLoad) {
                        onAction(DptAction.MessageDialog(
                            color = Warning,
                            icon = Icons.Filled.RestartAlt,
                            message = "Web Fail To Load !"
                        ))
                        continue
                    }

                    val jsCheck = "document.querySelector('.watermarked') ? 'YES' : 'NO';"
                    val jsCheckResult = advanceWebViewControl.awaitJavaScript(jsCheck)
                    if (jsCheckResult.contains("YES")) {
                        isDataFound = true
                    } else {
                        isDataFound = false
                    }

                    if (!isDataFound) {
                        onAction(DptAction.Process)
                        onAction(DptAction.Failure)
                        continue
                    }

                    val fullNameElement = """
                    (function() {
                        const allElements = document.querySelectorAll('*');
                        const labelElement = Array.from(allElements).find(el => el.textContent.trim() === 'Nama Pemilih');
                        const parentElement = labelElement.parentElement;
                        
                        return parentElement.innerText?.trim();
                    })();
                    """.trimIndent()
                    val fullNameResult = advanceWebViewControl.awaitJavaScript(fullNameElement)
                    val removedQuoteFullName = removeDoubleQuote(fullNameResult)
                    val filteredFullName = getFullName(removedQuoteFullName)

                    val regencyElement = "document.querySelector('.row--left')?.textContent?.trim()"
                    val regencyResult = advanceWebViewControl.awaitJavaScript(regencyElement)
                    val removedQuoteRegency = removeDoubleQuote(regencyResult)
                    val filteredRegency = getRegencyName(removedQuoteRegency)

                    val subdistrictElement = "document.querySelector('.row--center')?.textContent?.trim()"
                    val subdistrictResult = advanceWebViewControl.awaitJavaScript(subdistrictElement)
                    val removedQuoteSubdistrict = removeDoubleQuote(subdistrictResult)
                    val filteredSubdistrict = getSubdistrictName(removedQuoteSubdistrict)

                    val wardElement = "document.querySelectorAll('.row--right')[2]?.textContent?.trim()"
                    val wardResult = advanceWebViewControl.awaitJavaScript(wardElement)
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
                    break
                }
            }
            onAction(DptAction.IsStarted)
        }
    }

    DisposableEffect(Unit) {
        onDispose { scope.cancel() }
    }
}