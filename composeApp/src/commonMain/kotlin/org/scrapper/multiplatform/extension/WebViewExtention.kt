package org.scrapper.multiplatform.extension

import androidx.compose.runtime.snapshotFlow
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebViewNavigator
import com.multiplatform.webview.web.WebViewState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull

suspend fun waitWebViewToLoad(
    webViewState: WebViewState,
    timeoutMills: Long = 10_000L
) {
    withTimeout(timeoutMills) {
        delay(500)
        snapshotFlow { webViewState.loadingState }
            .filterIsInstance<LoadingState.Finished>()
            .first()
        delay(500)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun WebViewNavigator.awaitJavaScript(
    script: String,
    timeoutMills: Long = 10_000L
): String {
    return withTimeout(timeoutMills) {
        suspendCancellableCoroutine { continuation ->
            evaluateJavaScript(script) { result ->
                if (continuation.isActive) {
                    continuation.resume(result, null)
                }
            }
        }
    }
}