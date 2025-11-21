package org.scrapper.multiplatform.template

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomBottomSheetMessage(
    title: String,
    message: String,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = { onDismiss.invoke() },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )
                VerticalSpacer(15)
                CustomTextContent(text = message)
                VerticalSpacer(15)
                Row() {
                    Spacer(Modifier.weight(1f))
                    Button(
                        onClick = {
                            scope.launch {
                                sheetState.hide()
                            }.invokeOnCompletion {
                                onDismiss.invoke()
                            }
                        }
                    ) {
                        Text(text = "Ok")
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomBottomSheetMessageComposable(
    title: String,
    content: @Composable () -> Unit,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = { onDismiss.invoke() },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )
                VerticalSpacer(15)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    content.invoke()
                }
                VerticalSpacer(15)
                Row() {
                    Spacer(Modifier.weight(1f))
                    Button(
                        onClick = {
                            scope.launch {
                                sheetState.hide()
                            }.invokeOnCompletion {
                                onDismiss.invoke()
                            }
                        }
                    ) {
                        Text(text = "Ok")
                    }
                }
                VerticalSpacer(10)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomBottomSheetConfirmationComposable(
    title: String,
    content: @Composable () -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = { onCancel.invoke() },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )
                VerticalSpacer(15)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(tween(500))
                ) {
                    content.invoke()
                }
                VerticalSpacer(15)
                Row() {
                    Spacer(Modifier.weight(1f))
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                sheetState.hide()
                            }.invokeOnCompletion {
                                onCancel.invoke()
                            }
                        }
                    ) {
                        Text(text = "No")
                    }
                    HorizontalSpacer(20)
                    Button(
                        onClick = {
                            scope.launch {
                                onConfirm.invoke()
                                sheetState.hide()
                            }.invokeOnCompletion {
                                onCancel.invoke()
                            }
                        }
                    ) {
                        Text(text = "Yes")
                    }
                }
                VerticalSpacer(10)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomBottomSheetConfirmation(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = { onCancel.invoke() },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )
                VerticalSpacer(15)
                CustomTextContent(text = message)
                VerticalSpacer(15)
                Row() {
                    Spacer(Modifier.weight(1f))
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                sheetState.hide()
                            }.invokeOnCompletion {
                                onCancel.invoke()
                            }
                        }
                    ) {
                        Text(text = "No")
                    }
                    HorizontalSpacer(20)
                    Button(
                        onClick = {
                            scope.launch {
                                onConfirm.invoke()
                                sheetState.hide()
                            }.invokeOnCompletion {
                                onCancel.invoke()
                            }
                        }
                    ) {
                        Text(text = "Yes")
                    }
                }
                VerticalSpacer(10)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomBottomSheetInputComposable(
    coroutineScope: CoroutineScope,
    sheetState: SheetState,
    title: String,
    content: @Composable () -> Unit,
    btnYes: () -> Unit,
    onCancel: () -> Unit,
) {
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = { onCancel.invoke() },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )
                VerticalSpacer(15)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(tween(500))
                ) {
                    content.invoke()
                }
                VerticalSpacer(15)
                Row() {
                    Spacer(Modifier.weight(1f))
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                sheetState.hide()
                            }.invokeOnCompletion {
                                onCancel.invoke()
                            }
                        }
                    ) {
                        Text(text = "Close")
                    }
                    HorizontalSpacer(20)
                    Button(
                        onClick = { btnYes.invoke() }
                    ) {
                        Text(text = "Add")
                    }
                }
                VerticalSpacer(10)
            }
        }
    )
}