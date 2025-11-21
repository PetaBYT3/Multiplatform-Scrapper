package org.scrapper.multiplatform.action

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import org.scrapper.multiplatform.dataclass.UserDataClass

sealed interface AdminAction {

    data object IsSearchActive : AdminAction

    data class SearchText(val text: String) : AdminAction

    data object AddBottomSheet : AdminAction

    data object AddUser : AdminAction

    data class DeleteBottomSheet(val userData: UserDataClass?) : AdminAction

    data object DeleteUser : AdminAction

    data class DeleteAndroidIdBottomSheet(val userData: UserDataClass?) : AdminAction

    data object DeleteAndroidId : AdminAction

    data class UserName(val name: String) : AdminAction

    data class UserPassword(val password: String) : AdminAction

    data class UserRole(val role: String) : AdminAction

    data class MessageDialog(val color: Color ,val icon: ImageVector, val message: String) : AdminAction

}