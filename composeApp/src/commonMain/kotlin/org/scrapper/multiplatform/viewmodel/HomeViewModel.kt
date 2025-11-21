package org.scrapper.multiplatform.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.scrapper.multiplatform.action.HomeAction
import org.scrapper.multiplatform.repository.SettingsRepository
import org.scrapper.multiplatform.services.FirebaseServices
import org.scrapper.multiplatform.state.HomeState

class HomeViewModel(
    private val settingsRepository: SettingsRepository,
    private val firebaseServices: FirebaseServices
): ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    fun getUserId() {
        viewModelScope.launch {
            settingsRepository.getUserId().collect { userId ->
                firebaseServices.getUserById(userId).collect { userData ->
                    _state.update { it.copy(userData = userData) }
                }
            }
        }
    }

    fun onAction(action: HomeAction) {
        when (action) {
            HomeAction.LogoutBottomSheet -> {
                _state.update { it.copy(logoutBottomSheet = !it.logoutBottomSheet) }
            }
            HomeAction.ProfileBottomSheet -> {
                _state.update { it.copy(profileBottomSheet = !it.profileBottomSheet) }
            }
            HomeAction.Logout -> {
                viewModelScope.launch {
                    settingsRepository.setUserId("")
                }
            }
        }
    }

}