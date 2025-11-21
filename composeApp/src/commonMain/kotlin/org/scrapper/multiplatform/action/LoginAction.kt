package org.scrapper.multiplatform.action

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

sealed interface LoginAction {
    data class Username(val username: String) : LoginAction

    data class Password(val password: String) : LoginAction
    data object Login : LoginAction

    data class MessageDialog(val color: Color ,val icon: ImageVector, val message: String) : LoginAction

}