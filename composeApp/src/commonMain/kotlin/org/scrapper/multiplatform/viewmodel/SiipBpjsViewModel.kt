package org.scrapper.multiplatform.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.scrapper.multiplatform.action.SiipBpjsAction
import org.scrapper.multiplatform.readXlsxSiip
import org.scrapper.multiplatform.repository.SettingsRepository
import org.scrapper.multiplatform.state.SiipBpjsState

class SiipBpjsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SiipBpjsState())
    val state = _state.asStateFlow()

    fun initData() {
        viewModelScope.launch {
            settingsRepository.getGmailOption().collect { isActive ->
                _state.update { it.copy(getGmail = isActive) }
            }
        }
    }

    fun onAction(action: SiipBpjsAction) {
        when (action) {
            is SiipBpjsAction.IsLoggedIn -> {
                _state.update { it.copy(isLoggedIn = action.isLoggedIn) }
            }
            SiipBpjsAction.QuestionBottomSheet -> {
                _state.update { it.copy(questionBottomSheet = !it.questionBottomSheet) }
            }
            SiipBpjsAction.ExtendedMenu -> {
                _state.update { it.copy(extendedMenu = !it.extendedMenu) }
            }
            is SiipBpjsAction.XlsxFile -> {
                viewModelScope.launch {
                    if (action.file != null) {
                        _state.update { it.copy(
                            xlsxName = action.file.name,
                            xlsxFile = action.file
                        ) }

                        val fileByte = action.file.readBytes()
                        readXlsxSiip(fileByte).collect { rawString ->
                            _state.update { it.copy(rawList = it.rawList + rawString) }
                        }
                    }
                }
            }
            is SiipBpjsAction.XlsxName -> {
                _state.update { it.copy(xlsxName = action.name) }
            }
            SiipBpjsAction.DeleteXlsx -> {
                _state.update { it.copy(
                    xlsxName = null,
                    xlsxFile = null,
                    process = 0,
                    success = 0,
                    failure = 0,
                    rawList = emptyList(),
                ) }
            }
            SiipBpjsAction.Success -> {
                _state.update { it.copy(success = it.success + 1) }
            }
            SiipBpjsAction.Failure -> {
                _state.update { it.copy(failure = it.failure + 1) }
            }
            is SiipBpjsAction.AddResult -> {
                _state.update { it.copy(siipResult = it.siipResult + action.result) }
            }
            SiipBpjsAction.Process -> {
                _state.update { it.copy(process = it.process + 1) }
            }
            SiipBpjsAction.IsStarted -> {
                _state.update { it.copy(isStarted = !it.isStarted) }
                if (_state.value.isStarted) {
                    _state.update {
                        it.copy(
                            process = 0,
                            success = 0,
                            failure = 0,
                            siipResult = emptyList(),
                        )
                    }
                }
            }
            is SiipBpjsAction.MessageDialog -> {
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
            SiipBpjsAction.GetGmail -> {
                viewModelScope.launch {
                    settingsRepository.setGmailOption(!_state.value.getGmail)
                }
            }
            SiipBpjsAction.GetYmail -> {
                _state.update { it.copy(getYmail = !it.getYmail) }
            }
            SiipBpjsAction.SettingsBottomSheet -> {
                _state.update { it.copy(settingsBottomSheet = !it.settingsBottomSheet) }
            }
            is SiipBpjsAction.Debugging -> {
                _state.update { it.copy(debugging = action.message) }
            }
        }
    }
}