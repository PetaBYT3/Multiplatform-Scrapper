package org.scrapper.multiplatform.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.scrapper.multiplatform.action.DptAction
import org.scrapper.multiplatform.readXlsxDpt
import org.scrapper.multiplatform.state.DptState
import kotlin.collections.emptyList
import kotlin.collections.plus

class DptViewModel: ViewModel() {

    private val _state = MutableStateFlow(DptState())
    val state = _state.asStateFlow()

    fun onAction(action: DptAction) {
        when (action) {
            DptAction.QuestionBottomSheet -> {
                _state.update { it.copy(questionBottomSheet = !it.questionBottomSheet) }
            }
            DptAction.ExtendedMenu -> {
                _state.update { it.copy(extendedMenu = !it.extendedMenu) }
            }
            is DptAction.XlsxFile -> {
                viewModelScope.launch {
                    if (action.file != null) {
                        _state.update { it.copy(
                            xlsxFile = action.file,
                            xlsxName = action.file.name
                        ) }
                        val fileByte = action.file.readBytes()
                        readXlsxDpt(fileByte).collect { rawList ->
                            _state.update { it.copy(rawList = it.rawList + rawList) }
                        }
                    }
                }
            }
            is DptAction.XlsxName -> {
                _state.update { it.copy(xlsxName = action.name) }
            }
            DptAction.DeleteXlsx -> {
                _state.update { it.copy(
                    xlsxName = null,
                    xlsxFile = null,
                    process = 0,
                    success = 0,
                    failure = 0,
                    rawList = emptyList(),
                ) }
            }
            DptAction.Process -> {
                _state.update { it.copy(process = it.process + 1) }
            }
            DptAction.Success -> {
                _state.update { it.copy(success = it.success + 1) }
            }
            DptAction.Failure -> {
                _state.update { it.copy(failure = it.failure + 1) }
            }
            is DptAction.AddResult -> {
                _state.update { it.copy(dptResult = it.dptResult + action.result) }
            }
            DptAction.IsStarted -> {
                _state.update { it.copy(isStarted = !it.isStarted) }
                if (_state.value.isStarted) {
                    _state.update { it.copy(
                        process = 0,
                        success = 0,
                        failure = 0,
                        dptResult = emptyList(),
                    ) }
                }
            }
            is DptAction.MessageDialog -> {
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
            is DptAction.JsResult -> {
                _state.update { it.copy(jsResult = action.result) }
            }
        }
    }
}