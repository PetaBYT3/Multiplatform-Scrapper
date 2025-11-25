package org.scrapper.multiplatform.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.scrapper.multiplatform.action.LasikAction
import org.scrapper.multiplatform.readXlsxLasik
import org.scrapper.multiplatform.state.LasikState
import kotlin.collections.plus

class LasikViewModel: ViewModel() {

    private val _state = MutableStateFlow(LasikState())
    val state = _state.asStateFlow()

    fun onAction(action: LasikAction) {
        when (action) {
            LasikAction.QuestionBottomSheet -> {
                _state.update { it.copy(questionBottomSheet = !it.questionBottomSheet) }
            }
            LasikAction.ExtendedMenu -> {
                _state.update { it.copy(extendedMenu = !it.extendedMenu) }
            }
            is LasikAction.XlsxFile -> {
                viewModelScope.launch {
                    if (action.file != null) {
                        _state.update { it.copy(
                            xlsxName = action.file.name,
                            xlsxFile = action.file,
                        ) }
                        val fileByte = action.file.readBytes()
                        readXlsxLasik(fileByte).collect { rawList ->
                            _state.update { it.copy(rawList = it.rawList + rawList) }
                        }
                    }
                }
            }
            is LasikAction.XlsxName -> {
                _state.update { it.copy(xlsxName = action.name) }
            }
            LasikAction.DeleteXlsx -> {
                _state.update { it.copy(
                    xlsxName = null,
                    xlsxFile = null,
                    process = 0,
                    success = 0,
                    failure = 0,
                    rawList = emptyList(),
                ) }
            }
            LasikAction.Process -> {
                _state.update { it.copy(process = it.process + 1) }
            }
            LasikAction.Success -> {
                _state.update { it.copy(success = it.success + 1) }
            }
            LasikAction.Failure -> {
                _state.update { it.copy(failure = it.failure + 1) }
            }
            is LasikAction.AddResult -> {
                _state.update { it.copy(lasikResult = it.lasikResult + action.result) }
            }
            LasikAction.IsStarted -> {
                _state.update { it.copy(isStarted = !it.isStarted) }

                val isStarted = _state.value.isStarted
                if (isStarted) {
                    _state.update { it.copy(
                        process = 0,
                        success = 0,
                        failure = 0,
                        lasikResult = emptyList()
                    ) }
                }
            }
            is LasikAction.MessageDialog -> {
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

            is LasikAction.Debugging -> {
                _state.update { it.copy(debugging = action.result) }
            }
        }
    }
}