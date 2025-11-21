package org.scrapper.multiplatform.action

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.vinceglb.filekit.core.PlatformFile
import org.scrapper.multiplatform.dataclass.DptResult

sealed interface DptAction {

    data object QuestionBottomSheet : DptAction

    data object ExtendedMenu : DptAction

    data class XlsxFile(val file: PlatformFile?) : DptAction

    data class XlsxName(val name: String) : DptAction

    data object DeleteXlsx : DptAction

    data object Success : DptAction

    data object Failure : DptAction

    data object Process : DptAction

    data class AddResult(val result: DptResult) : DptAction

    data object IsStarted : DptAction

    data class MessageDialog(val color: Color ,val icon: ImageVector, val message: String) : DptAction

    data class JsResult(val result: String) : DptAction

}