package org.scrapper.multiplatform.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.scrapper.multiplatform.action.LoginAction
import org.scrapper.multiplatform.effect.LoginEffect
import org.scrapper.multiplatform.getHardwareId
import org.scrapper.multiplatform.repository.SettingsRepository
import org.scrapper.multiplatform.route.Route
import org.scrapper.multiplatform.services.FirebaseServices
import org.scrapper.multiplatform.services.LoginResult
import org.scrapper.multiplatform.state.LoginState
import org.scrapper.multiplatform.template.Success
import org.scrapper.multiplatform.template.Warning

class LoginViewModel(
    private val firebaseServices: FirebaseServices,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<LoginEffect>()
    val effect = _effect.asSharedFlow()

    fun initData() {
        viewModelScope.launch {
            settingsRepository.getUserId().collect { userId ->
                _state.update { it.copy(userId = userId) }
            }
        }
    }

    fun onAction(action: LoginAction) {
        when (action) {
            is LoginAction.Username -> {
                _state.update { it.copy(username = action.username) }
            }
            is LoginAction.Password -> {
                _state.update { it.copy(password = action.password) }
            }
            is LoginAction.Login -> {
                viewModelScope.launch {
                    _state.update { it.copy(isLoading = true) }

                    val userName = _state.value.username
                    val userPassword = _state.value.password

                    val loginResult = firebaseServices.userLogin(
                        userName = userName,
                        userPassword = userPassword,
                        hardwareId = getHardwareId()
                    )

                    when (loginResult) {
                        is LoginResult.Success -> {
                            showDialog(
                                dialogColor = Success,
                                iconDialog = Icons.Filled.Check,
                                messageDialog = "Login Success !"
                            )
                            settingsRepository.setUserId(loginResult.userId)
                            delay(1000)
                            _effect.emit(LoginEffect.Navigate(Route.HomePage))
                        }
                        LoginResult.HardwareIdConflict -> {
                            showDialog(
                                dialogColor = Warning,
                                iconDialog = Icons.Filled.Warning,
                                messageDialog = "Hardware ID Not Match, Please Contact Admin !"
                            )
                        }
                        LoginResult.Fail -> {
                            showDialog(
                                dialogColor = Warning,
                                iconDialog = Icons.Filled.Warning,
                                messageDialog = "Username Or Password Incorrect !"
                            )
                        }
                    }

                    _state.update { it.copy(isLoading = false) }
                }
            }
            is LoginAction.MessageDialog -> {
                viewModelScope.launch {
                    _state.update { it.copy(
                        dialogVisibility = true,
                        dialogColor = action.color,
                        iconDialog = action.icon,
                        messageDialog = action.message
                    ) }
                    delay(5_000)
                    _state.update { it.copy(dialogVisibility = false) }
                }
            }
        }
    }

    private fun showDialog(
        dialogColor: Color,
        iconDialog: ImageVector,
        messageDialog: String
    ) {
        viewModelScope.launch {
            _state.update { it.copy(
                dialogVisibility = true,
                dialogColor = dialogColor,
                iconDialog = iconDialog,
                messageDialog = messageDialog
            ) }
            delay(5_000)
            _state.update { it.copy(dialogVisibility = false) }
        }
    }
}