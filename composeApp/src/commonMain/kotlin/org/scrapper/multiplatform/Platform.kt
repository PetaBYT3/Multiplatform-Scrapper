package org.scrapper.multiplatform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.multiplatform.webview.web.WebViewNavigator
import com.multiplatform.webview.web.WebViewState
import jdk.jfr.Enabled
import kotlinx.coroutines.flow.Flow
import org.koin.core.module.Module
import org.scrapper.multiplatform.dataclass.DptResult
import org.scrapper.multiplatform.dataclass.LasikResult
import org.scrapper.multiplatform.dataclass.SiipResult

expect val platformId: String

expect fun getHardwareId(): String

expect val platformSettingsModule: Module

expect val platformFirebaseModule: Module

//Read Xlsx
expect fun readXlsxSiip(fileBytes: ByteArray): Flow<String>
expect fun readXlsxDpt(fileBytes: ByteArray) : Flow<DptResult>
expect fun readXlsxLasik(fileBytes: ByteArray) : Flow<LasikResult>

//Create Xlsx
expect suspend fun createXlsxSiip(siipResult: List<SiipResult>): ByteArray?
expect suspend fun createXlsxDpt(siipResult: List<DptResult>): ByteArray?
expect suspend fun createXlsxLasik(siipResult: List<LasikResult>): ByteArray?

//Save File
expect fun saveFile(fileName: String, fileBytes: ByteArray, filePath: String)

@Composable
expect fun BackHandler(enabled: Boolean, onBack: () -> Unit)

@Composable
expect fun KeepScreenOn(enabled: Boolean)