package org.scrapper.multiplatform.action

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.vinceglb.filekit.core.PlatformFile
import org.scrapper.multiplatform.dataclass.LasikResult

sealed interface LasikAction {

    data object QuestionBottomSheet : LasikAction

    data object ExtendedMenu : LasikAction

    data class XlsxFile(val file: PlatformFile?) : LasikAction

    data class XlsxName(val name: String) : LasikAction

    data object DeleteXlsx : LasikAction

    data object Success : LasikAction

    data object Failure : LasikAction

    data object Process : LasikAction

    data class AddResult(val result: LasikResult) : LasikAction

    data object IsStarted : LasikAction

    data class MessageDialog(val color: Color ,val icon: ImageVector, val message: String) : LasikAction

    data class Debugging(val result: String) : LasikAction

}