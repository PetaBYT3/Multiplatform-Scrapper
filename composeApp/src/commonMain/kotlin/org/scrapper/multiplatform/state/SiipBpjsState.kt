package org.scrapper.multiplatform.state

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.vinceglb.filekit.core.PlatformFile
import org.scrapper.multiplatform.dataclass.SiipResult
import org.scrapper.multiplatform.template.Warning

data class SiipBpjsState(

    val isLoggedIn: Boolean = true,

    val resetWebViewStateKey: Int = 0,

    val questionBottomSheet: Boolean = false,

    val extendedMenu: Boolean = false,

    val xlsxFile: PlatformFile? = null,
    val xlsxName: String? = null,

    val rawList: List<String> = emptyList(),

    val deleteXlsxBottomSheet: Boolean = false,

    val isStarted: Boolean = false,
    val stopBottomSheet: Boolean = false,

    val process: Int = 0,
    val success: Int = 0,
    val failure: Int = 0,

    val siipResult: List<SiipResult> = emptyList(),

    val dialogVisibility: Boolean = false,
    val dialogColor: Color = Warning,
    val iconDialog: ImageVector = Icons.Filled.Warning,
    val messageDialog: String = "",

    val getGmail: Boolean = true,
    val getYmail: Boolean = true,
    val quickMode: Boolean = false,

    val settingsBottomSheet: Boolean = false,

    val debugging: String = ""

)
