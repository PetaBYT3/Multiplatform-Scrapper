package org.scrapper.multiplatform.extension

import android.graphics.Bitmap
import android.view.View
import android.webkit.WebSettings.LOAD_CACHE_ELSE_NETWORK
import android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import im.delight.android.webview.AdvancedWebView
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

class AdvanceWebViewControl {
    var webView: AdvancedWebView? = null

    fun evaluateJavascript(script: String, onResult: (String) -> Unit) {
        webView?.evaluateJavascript(script) {
            onResult(it)
        }
    }

    fun loadUrl(url: String) {
        webView?.loadUrl(url)
    }

    suspend fun waitWebToLoad(onLoaded: Boolean, timeoutMills: Long = 5_000) : Boolean {
        val result = withTimeoutOrNull(timeoutMills) {
            delay(500)
            snapshotFlow { onLoaded }
                .filter { loading -> !loading }
                .first()
            delay(500)
        }

        return result != null
    }

    fun navigateBack() {
        webView?.goBack()
    }
}

@Composable
fun rememberAdvanceViewControl(): AdvanceWebViewControl {
    return remember { AdvanceWebViewControl() }
}

suspend fun AdvanceWebViewControl.awaitJavaScript(script: String): String {
    return suspendCancellableCoroutine { continuation ->
        evaluateJavascript(script) { result ->
            if (continuation.isActive) {
                continuation.resume(result, null)
            }
        }
    }
}

@Composable
fun AdvanceWebViewComposable(
    modifier: Modifier = Modifier,
    url: String,
    advanceWebViewControl: AdvanceWebViewControl,
    onPageStarted: () -> Unit,
    onPageFinished: () -> Unit,
    onError: (String) -> Unit
) {
    val context = LocalContext.current

    AndroidView(
        modifier = modifier
            .background(Color.Transparent),
        factory = {
            AdvancedWebView(context).apply {
                advanceWebViewControl.webView = this

                setListener(
                    context as ComponentActivity,
                    object : AdvancedWebView.Listener {
                        override fun onPageStarted(
                            url: String?,
                            favicon: Bitmap?
                        ) {
                            onPageStarted()
                        }

                        override fun onPageFinished(url: String?) {
                            onPageFinished()
                        }

                        override fun onPageError(
                            errorCode: Int,
                            description: String?,
                            failingUrl: String?
                        ) {
                            onError(description.toString())
                        }

                        override fun onDownloadRequested(
                            url: String?,
                            suggestedFilename: String?,
                            mimeType: String?,
                            contentLength: Long,
                            contentDisposition: String?,
                            userAgent: String?
                        ) {

                        }

                        override fun onExternalPageRequest(url: String?) {

                        }
                    }
                )

                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.userAgentString = webUserAgent
                settings.mixedContentMode = MIXED_CONTENT_ALWAYS_ALLOW
                settings.cacheMode = LOAD_CACHE_ELSE_NETWORK
                settings.databaseEnabled = true
                settings.setSupportZoom(false)
                setLayerType(View.LAYER_TYPE_HARDWARE, null)

                loadUrl(url)
            }
        },
        update = {
        }
    )
}