package org.scrapper.multiplatform.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.scrapper.multiplatform.repository.SettingsRepository
import org.scrapper.multiplatform.state.SplashState

class SplashViewModel(
    private val settingsRepository: SettingsRepository
): ViewModel() {

    private val _state = MutableStateFlow(SplashState())
    val state = _state.asStateFlow()

    fun initData() {
        viewModelScope.launch {
            settingsRepository.getUserId().collect { userId ->
                _state.update { it.copy(userId = userId) }
            }
        }
    }
}