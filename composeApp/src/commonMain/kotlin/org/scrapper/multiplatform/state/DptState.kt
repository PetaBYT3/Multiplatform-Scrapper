package org.scrapper.multiplatform.state

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.vinceglb.filekit.core.PlatformFile
import org.scrapper.multiplatform.dataclass.DptResult
import org.scrapper.multiplatform.template.Warning

data class DptState(

    val questionBottomSheet: Boolean = false,

    val extendedMenu: Boolean = false,

    val xlsxFile: PlatformFile? = null,
    val xlsxName: String? = null,

    val rawList: List<DptResult> = emptyList(),

    val deleteXlsxBottomSheet: Boolean = false,

    val isStarted: Boolean = false,
    val stopBottomSheet: Boolean = false,

    val process: Int = 0,
    val success: Int = 0,
    val failure: Int = 0,

    val dptResult: List<DptResult> = emptyList(),

    val dialogVisibility: Boolean = false,
    val dialogColor: Color = Warning,
    val iconDialog: ImageVector = Icons.Filled.Warning,
    val messageDialog: String = "",

    val jsResult: String = ""

)
