package org.scrapper.multiplatform.page

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import org.koin.compose.viewmodel.koinViewModel
import org.scrapper.multiplatform.action.AdminAction
import org.scrapper.multiplatform.state.AdminState
import org.scrapper.multiplatform.template.CustomBottomSheetConfirmationComposable
import org.scrapper.multiplatform.template.CustomBottomSheetInputComposable
import org.scrapper.multiplatform.template.CustomDropDownMenu
import org.scrapper.multiplatform.template.CustomIconButton
import org.scrapper.multiplatform.template.CustomRadioButton
import org.scrapper.multiplatform.template.CustomTextContent
import org.scrapper.multiplatform.template.CustomTextField
import org.scrapper.multiplatform.template.DropDownItem
import org.scrapper.multiplatform.template.HorizontalSpacer
import org.scrapper.multiplatform.template.VerticalSpacer
import org.scrapper.multiplatform.template.Warning
import org.scrapper.multiplatform.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPage(
    navController: NavController,
    viewModel: AdminViewModel = koinViewModel()
) {
    val scope = rememberCoroutineScope()

    val state by viewModel.state.collectAsStateWithLifecycle()
    val onAction = viewModel::onAction

    LaunchedEffect(Unit) {
        viewModel.initData()
    }

    Scaffold(
        navController = navController,
        state = state,
        onAction = onAction
    )

    LaunchedEffect(state.userData) {
        if (state.userData?.userRole == null) return@LaunchedEffect

        if (state.userData?.userRole != "Admin") {
            navController.popBackStack()
        }
    }

    if (state.addBottomSheet) {
        val addSheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )
        CustomBottomSheetInputComposable(
            coroutineScope = scope,
            sheetState = addSheetState,
            title = "Add User",
            content = {
                CustomTextField(
                    value = state.userName,
                    onValueChange = { onAction(AdminAction.UserName(it)) },
                    placeholder = "Username",
                    leadingIcon = Icons.Filled.Person
                )
                VerticalSpacer(10)
                CustomTextField(
                    value = state.userPassword,
                    onValueChange = { onAction(AdminAction.UserPassword(it)) },
                    placeholder = "Password",
                    leadingIcon = Icons.Filled.Password
                )
                VerticalSpacer(10)
                val userRole = listOf(
                    "Admin",
                    "User"
                )
                CustomRadioButton(
                    itemList = userRole,
                    selectedValue = state.userRole,
                    onItemSelected = { onAction(AdminAction.UserRole(it)) }
                )
                AnimatedVisibility(
                    enter = fadeIn(),
                    exit = fadeOut(),
                    visible = state.dialogVisibility,
                    content = {
                        Column() {
                            VerticalSpacer(10)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(state.dialogColor),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    modifier = Modifier
                                        .padding(10.dp),
                                    imageVector = state.iconDialog,
                                    contentDescription = null,
                                    tint = Color.Black
                                )
                                Text(
                                    text = state.messageDialog,
                                    color = Color.Black,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                )
            },
            btnYes = {
                if (state.userName.isBlank() || state.userPassword.isBlank() || state.userRole.isBlank()) {
                    onAction(AdminAction.MessageDialog(
                        color = Warning,
                        icon = Icons.Filled.Warning,
                        message = "Please fill all fields & select the role !"
                    ))
                } else {
                    onAction(AdminAction.AddUser)
                }
            },
            onCancel = { onAction(AdminAction.AddBottomSheet) },
        )
    }

    if (state.deleteUserBottomSheet) {
        CustomBottomSheetConfirmationComposable(
            title = "Delete User",
            content = {
                Card() {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column() {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = null
                                )
                                HorizontalSpacer(15)
                                CustomTextContent(text = state.userToDelete?.userName ?: "")
                            }
                            VerticalSpacer(5)
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Password,
                                    contentDescription = null
                                )
                                HorizontalSpacer(15)
                                CustomTextContent(text = state.userToDelete?.userPassword ?: "")
                            }
                            VerticalSpacer(5)
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AdminPanelSettings,
                                    contentDescription = null
                                )
                                HorizontalSpacer(15)
                                CustomTextContent(text = state.userToDelete?.userRole ?: "")
                            }
                            VerticalSpacer(5)
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Memory,
                                    contentDescription = null
                                )
                                HorizontalSpacer(15)
                                CustomTextContent(text = state.userToDelete?.androidId?.ifBlank { "No Hardware ID !" } ?: "No Hardware ID !")
                            }
                        }
                    }
                }
            },
            onConfirm = { onAction(AdminAction.DeleteUser) },
            onCancel = { onAction(AdminAction.DeleteBottomSheet(null)) }
        )
    }

    if (state.deleteAndroidIdBottomSheet) {
        CustomBottomSheetConfirmationComposable(
            title = "Reset Hardware ID",
            content = {
                Card() {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column() {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = null
                                )
                                HorizontalSpacer(15)
                                CustomTextContent(text = state.androidIdToDelete?.userName ?: "")
                            }
                            VerticalSpacer(5)
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Password,
                                    contentDescription = null
                                )
                                HorizontalSpacer(15)
                                CustomTextContent(text = state.androidIdToDelete?.userPassword ?: "")
                            }
                            VerticalSpacer(5)
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AdminPanelSettings,
                                    contentDescription = null
                                )
                                HorizontalSpacer(15)
                                CustomTextContent(text = state.androidIdToDelete?.userRole ?: "")
                            }
                            VerticalSpacer(5)
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Memory,
                                    contentDescription = null
                                )
                                HorizontalSpacer(15)
                                CustomTextContent(text = state.androidIdToDelete?.androidId?.ifBlank { "No Hardware ID !" } ?: "No Hardware ID !")
                            }
                        }
                    }
                }
            },
            onConfirm = { onAction(AdminAction.DeleteAndroidId) },
            onCancel = { onAction(AdminAction.DeleteAndroidIdBottomSheet(null)) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Scaffold(
    navController: NavController,
    state: AdminState,
    onAction: (AdminAction) -> Unit
) {
    Scaffold(
        modifier = Modifier
            .imePadding(),
        topBar = {
            TopBar(
                navController = navController,
                state = state,
                onAction = onAction
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Content(
                    navController = navController,
                    state = state,
                    onAction = onAction
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                state = state,
                onAction = onAction
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    navController: NavController,
    state: AdminState,
    onAction: (AdminAction) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    TopAppBar(
        navigationIcon = {
            CustomIconButton(
                imageVector = Icons.Rounded.ArrowBack,
                onClick = { navController.popBackStack() }
            )
        },
        title = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterStart),
                    text = "Admin Panel"
                )
                AnimatedVisibility(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    enter = fadeIn(),
                    exit = fadeOut(),
                    visible = state.isSearchActive,
                    content = {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            TextField(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester),
                                value = state.searchText,
                                onValueChange = { onAction(AdminAction.SearchText(it)) },
                                placeholder = {
                                    Text(
                                        text = "Search Username...",
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                },
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                )
                            )
                        }
                        LaunchedEffect(Unit) {
                            focusRequester.requestFocus()
                        }
                    }
                )
            }
        },
        actions = {
            CustomIconButton(
                imageVector = if (state.isSearchActive) Icons.Filled.Close else Icons.Filled.Search,
                onClick = {
                    onAction(AdminAction.IsSearchActive)
                    onAction(AdminAction.SearchText(""))
                }
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Content(
    navController: NavController,
    state: AdminState,
    onAction: (AdminAction) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 15.dp)
    ) {
        LazyColumn() {
            items(
                items = state.filteredUserList,
                key = { userData -> userData.userId }
            ) { userData ->
                Card() {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(15.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = null
                                )
                                HorizontalSpacer(15)
                                CustomTextContent(text = userData.userName)
                            }
                            VerticalSpacer(5)
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Password,
                                    contentDescription = null
                                )
                                HorizontalSpacer(15)
                                CustomTextContent(text = userData.userPassword)
                            }
                            VerticalSpacer(5)
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AdminPanelSettings,
                                    contentDescription = null
                                )
                                HorizontalSpacer(15)
                                CustomTextContent(text = userData.userRole)
                            }
                            VerticalSpacer(5)
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Memory,
                                    contentDescription = null
                                )
                                HorizontalSpacer(15)
                                CustomTextContent(text = userData.androidId.ifBlank { "No Hardware ID !" })
                            }
                        }
                        val dropDownList = buildList {
                            add(
                                DropDownItem(
                                    imageVector = Icons.Filled.Delete,
                                    title = "Delete User",
                                    onClick = { onAction(AdminAction.DeleteBottomSheet(userData)) }
                                )
                            )
                            if (userData.androidId.isNotBlank()) {
                                add(
                                    DropDownItem(
                                        imageVector = Icons.Filled.RemoveCircle,
                                        title = "Reset Hardware ID",
                                        onClick = { onAction(AdminAction.DeleteAndroidIdBottomSheet(userData)) }
                                    )
                                )
                            }
                        }
                        CustomDropDownMenu(
                            icon = Icons.Filled.MoreVert,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 5.dp),
                            dropDownList = dropDownList
                        )
                    }
                }
                VerticalSpacer(10)
            }
        }

        if (state.initialUserList.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                )
                HorizontalSpacer(10)
                CustomTextContent(text = "No User Found !")
            }
        }
    }
}

@Composable
private fun FloatingActionButton(
    state: AdminState,
    onAction: (AdminAction) -> Unit
) {
    ExtendedFloatingActionButton(
        onClick = { onAction(AdminAction.AddBottomSheet) },
        icon = {
            Icon(
                imageVector = Icons.Filled.PersonAdd,
                contentDescription = null
            )
        },
        text = { Text(text = "Add User") }
    )
}