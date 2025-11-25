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
import kotlinx.coroutines.withTimeoutOrNull

suspend fun waitWebViewToLoad(
    webViewState: WebViewState,
    timeoutMills: Long = 5_000
): Boolean {
    val result = withTimeoutOrNull(timeoutMills) {
        delay(500)
        snapshotFlow { webViewState.loadingState }
            .filterIsInstance<LoadingState.Finished>()
            .first()
        delay(500)
        true
    }

    return result != null
}

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun WebViewNavigator.awaitJavaScript(script: String): String {
    return suspendCancellableCoroutine { continuation ->
        evaluateJavaScript(script) { result ->
            if (continuation.isActive) {
                continuation.resume(result, null)
            }
        }
    }
}