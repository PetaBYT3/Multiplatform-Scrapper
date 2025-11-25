package org.scrapper.multiplatform.action

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.vinceglb.filekit.core.PlatformFile
import org.scrapper.multiplatform.dataclass.SiipResult

sealed interface SiipBpjsAction {

    data object ResetWebViewStateKey: SiipBpjsAction

    data class IsLoggedIn(val isLoggedIn: Boolean) : SiipBpjsAction

    data object QuestionBottomSheet : SiipBpjsAction

    data object ExtendedMenu : SiipBpjsAction

    data class XlsxFile(val file: PlatformFile?) : SiipBpjsAction

    data class XlsxName(val name: String) : SiipBpjsAction

    data object DeleteXlsx : SiipBpjsAction

    data object Success : SiipBpjsAction

    data object Failure : SiipBpjsAction

    data object Process : SiipBpjsAction

    data class AddResult(val result: SiipResult) : SiipBpjsAction

    data object IsStarted : SiipBpjsAction

    data class MessageDialog(val color: Color ,val icon: ImageVector, val message: String) : SiipBpjsAction

    data object GetGmail: SiipBpjsAction

    data object GetYmail: SiipBpjsAction

    data object SettingsBottomSheet: SiipBpjsAction

    data class Debugging(val message: String): SiipBpjsAction

}