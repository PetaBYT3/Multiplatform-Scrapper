package org.scrapper.multiplatform.state

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import org.scrapper.multiplatform.dataclass.UserDataClass
import org.scrapper.multiplatform.template.Warning

data class AdminState(

    val userData: UserDataClass? = null,

    val isSearchActive: Boolean = false,
    val searchText: String = "",

    val initialUserList: List<UserDataClass> = emptyList(),
    val filteredUserList: List<UserDataClass> = emptyList(),

    val addBottomSheet: Boolean = false,

    val deleteUserBottomSheet: Boolean = false,
    val userToDelete: UserDataClass? = null,

    val deleteAndroidIdBottomSheet: Boolean = false,
    val androidIdToDelete: UserDataClass? = null,

    val userName: String = "",
    val userPassword: String = "",
    val userRole: String = "",

    val dialogVisibility: Boolean = false,
    val dialogColor: Color = Warning,
    val iconDialog: ImageVector = Icons.Filled.Warning,
    val messageDialog: String = ""
)