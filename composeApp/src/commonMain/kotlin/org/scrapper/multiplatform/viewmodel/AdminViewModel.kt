package org.scrapper.multiplatform.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.scrapper.multiplatform.action.AdminAction
import org.scrapper.multiplatform.dataclass.UserDataClass
import org.scrapper.multiplatform.repository.SettingsRepository
import org.scrapper.multiplatform.services.AddResult
import org.scrapper.multiplatform.services.FirebaseServices
import org.scrapper.multiplatform.state.AdminState
import org.scrapper.multiplatform.template.Success
import org.scrapper.multiplatform.template.Warning

class AdminViewModel(
    private val firebaseServices: FirebaseServices,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AdminState())
    val state = _state.asStateFlow()

    fun initData() {
        viewModelScope.launch {
            firebaseServices.getAllUser().collect { userList ->
                _state.update { it.copy(
                    initialUserList = userList,
                    filteredUserList = filteredUserList("", userList)
                ) }
            }
        }

        viewModelScope.launch {
            settingsRepository.getUserId().collect { userId ->
                firebaseServices.getUserById(userId).collect { userData ->
                    _state.update { it.copy(userData = userData) }
                }
            }
        }
    }

    fun onAction(action: AdminAction) {
        when (action) {
            AdminAction.IsSearchActive -> {
                _state.update { it.copy(isSearchActive = !it.isSearchActive) }
            }
            is AdminAction.SearchText -> {
                _state.update { it.copy(
                    searchText = action.text,
                    filteredUserList = filteredUserList(action.text, it.initialUserList)
                ) }
            }
            is AdminAction.UserName -> {
                _state.update { it.copy(userName = action.name) }
            }
            is AdminAction.UserPassword -> {
                _state.update { it.copy(userPassword = action.password) }
            }
            is AdminAction.UserRole -> {
                _state.update { it.copy(userRole = action.role) }
            }
            AdminAction.AddBottomSheet -> {
                _state.update { it.copy(addBottomSheet = !it.addBottomSheet) }

                val addBottomSheet = _state.value.addBottomSheet
                if (!addBottomSheet) {
                    _state.update { it.copy(
                        userName = "",
                        userPassword = "",
                        userRole = ""
                    ) }
                }
            }
            AdminAction.AddUser -> {
                viewModelScope.launch {
                    val userData = UserDataClass(
                        userId = "",
                        userName = _state.value.userName,
                        userPassword = _state.value.userPassword,
                        userRole = _state.value.userRole,
                        androidId = ""
                    )
                    val firebaseResult = firebaseServices.addUser(userData)
                    when (firebaseResult) {
                        AddResult.Success -> {
                            showDialog(
                                dialogColor = Success,
                                iconDialog = Icons.Filled.Check,
                                messageDialog = "User Successfully Added !"
                            )
                            _state.update { it.copy(
                                userName = "",
                                userPassword = "",
                                userRole = ""
                            ) }
                        }
                        AddResult.UsernameExist -> {
                            showDialog(
                                dialogColor = Warning,
                                iconDialog = Icons.Filled.Warning,
                                messageDialog = "Username Already Exist !"
                            )
                        }
                        AddResult.Fail -> {
                            showDialog(
                                dialogColor = Warning,
                                iconDialog = Icons.Filled.Warning,
                                messageDialog = "Something Went Wrong !"
                            )
                        }
                    }
                }
            }
            is AdminAction.DeleteBottomSheet -> {
                _state.update { it.copy(
                    deleteUserBottomSheet = !it.deleteUserBottomSheet,
                    userToDelete = action.userData
                ) }
            }
            AdminAction.DeleteUser -> {
                val userToDelete = _state.value.userToDelete
                if (userToDelete != null) {
                    firebaseServices.deleteUser(userToDelete)
                }
            }
            is AdminAction.DeleteAndroidIdBottomSheet -> {
                _state.update { it.copy(
                    deleteAndroidIdBottomSheet = !it.deleteAndroidIdBottomSheet,
                    androidIdToDelete = action.userData
                ) }
            }
            AdminAction.DeleteAndroidId -> {
                val userToDelete = _state.value.androidIdToDelete
                if (userToDelete != null) {
                    firebaseServices.deleteHardwareId(userToDelete)
                }
            }
            is AdminAction.MessageDialog -> {
                viewModelScope.launch {
                    _state.update { it.copy(
                        dialogVisibility = true,
                        dialogColor = action.color,
                        iconDialog = action.icon,
                        messageDialog = action.message
                    ) }
                    delay(5_000)
                    _state.update { it.copy(
                        dialogVisibility = false,
                    ) }
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

    private fun filteredUserList(
        searchText: String,
        initialUserList: List<UserDataClass>
    ) : List<UserDataClass> {
        return if (searchText.isBlank()) {
            initialUserList
        } else {
            initialUserList.filter {
                it.userName.contains(searchText, ignoreCase = true)
            }
        }
    }
}