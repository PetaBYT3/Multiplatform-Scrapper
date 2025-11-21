package org.scrapper.multiplatform.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.scrapper.multiplatform.getHardwareId
import org.scrapper.multiplatform.repository.SettingsRepository
import org.scrapper.multiplatform.route.Route
import org.scrapper.multiplatform.services.FirebaseServices

class AppViewModel(
    private val settingsRepository: SettingsRepository,
    private val firebaseServices: FirebaseServices
) : ViewModel() {

    private val _effect = MutableSharedFlow<Route>()
    val effect = _effect.asSharedFlow()

    fun initData() {
        viewModelScope.launch {
            settingsRepository.getUserId().collect { userId->
                checkUserExistence(userId)
                checkHardwareId(userId)
            }
        }
    }

    private fun checkUserExistence(userId: String) {
        viewModelScope.launch {
            firebaseServices.getUserById(userId).collect { userData ->
                if (userData == null) {
                    settingsRepository.setUserId("")
                    _effect.emit(Route.LoginPage)
                }
            }
        }
    }

    private fun checkHardwareId(userId: String) {
        viewModelScope.launch {
            firebaseServices.getUserById(userId).collect { userData ->
                if (userData != null) {
                    if (userData.androidId.isBlank() || userData.androidId != getHardwareId()) {
                        settingsRepository.setUserId("")
                        _effect.emit(Route.LoginPage)
                    }
                }
            }
        }
    }
}