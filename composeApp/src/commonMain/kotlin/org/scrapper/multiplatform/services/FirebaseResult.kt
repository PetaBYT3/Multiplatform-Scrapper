package org.scrapper.multiplatform.services

sealed class LoginResult {
    data class Success(val userId: String) : LoginResult()
    data object HardwareIdConflict : LoginResult()
    data object Fail : LoginResult()
}

sealed class AddResult {
    data object Success : AddResult()
    data object UsernameExist : AddResult()
    data object Fail : AddResult()
}