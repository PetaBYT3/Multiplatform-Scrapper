package org.scrapper.multiplatform.page

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.Start
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.scrapper.multiplatform.action.HomeAction
import org.scrapper.multiplatform.platformId
import org.scrapper.multiplatform.route.Route
import org.scrapper.multiplatform.state.HomeState
import org.scrapper.multiplatform.template.CustomBottomSheetConfirmation
import org.scrapper.multiplatform.template.CustomBottomSheetMessageComposable
import org.scrapper.multiplatform.template.CustomIconButton
import org.scrapper.multiplatform.template.CustomTextContent
import org.scrapper.multiplatform.template.CustomTextHint
import org.scrapper.multiplatform.template.CustomTextTitle
import org.scrapper.multiplatform.template.HorizontalSpacer
import org.scrapper.multiplatform.template.VerticalSpacer
import org.scrapper.multiplatform.viewmodel.HomeViewModel

@Composable
fun HomePage(
    navController: NavController,
    viewModel: HomeViewModel = koinViewModel()
) {
    val scope = rememberCoroutineScope()

    val state by viewModel.state.collectAsStateWithLifecycle()
    val onAction = viewModel::onAction

    LaunchedEffect(Unit) {
        viewModel.getUserId()
    }

    Scaffold(
        navController = navController,
        state = state,
        onAction = onAction
    )

    if (state.profileBottomSheet) {
        CustomBottomSheetMessageComposable(
            title = "Profile",
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
                                CustomTextContent(text = state.userData?.userName ?: "")
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
                                CustomTextContent(text = state.userData?.userPassword ?: "")
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
                                CustomTextContent(text = state.userData?.userRole ?: "")
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
                                CustomTextContent(text = state.userData?.androidId?.ifBlank { "No Hardware ID !" } ?: "No Hardware ID !")
                            }
                        }
                    }
                }
            },
            onDismiss = { onAction(HomeAction.ProfileBottomSheet) },
        )
    }

    if (state.logoutBottomSheet) {
        CustomBottomSheetConfirmation(
            title = "Logout",
            message = "Are you sure you want to logout?",
            onConfirm = {
                scope.launch {
                    onAction(HomeAction.Logout)
                }.invokeOnCompletion {
                    navController.navigate(Route.LoginPage)
                }
            },
            onCancel = { onAction(HomeAction.LogoutBottomSheet) }
        )
    }
}

@Composable
private fun Scaffold(
    navController: NavController,
    state: HomeState,
    onAction: (HomeAction) -> Unit
) {
    Scaffold(
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
                    navController = navController
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    navController: NavController,
    state: HomeState = HomeState(),
    onAction: (HomeAction) -> Unit
) {
    TopAppBar(
        navigationIcon = {},
        title = { Text(text = "Home") },
        actions = {
            AnimatedVisibility(
                enter = fadeIn(),
                exit = fadeOut(),
                visible = state.userData?.userRole?.contains("Admin", ignoreCase = true) == true,
                content = {
                    CustomIconButton(
                        imageVector = Icons.Filled.AdminPanelSettings,
                        onClick = { navController.navigate(Route.AdminPage) }
                    )
                }
            )
            CustomIconButton(
                imageVector = Icons.Filled.Person,
                onClick = { onAction(HomeAction.ProfileBottomSheet) }
            )
            CustomIconButton(
                imageVector = Icons.Filled.Logout,
                onClick = { onAction(HomeAction.LogoutBottomSheet) }
            )
        }
    )
}

@Composable
private fun Content(
    navController: NavController
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 15.dp)
            .verticalScroll(
                state = scrollState,
                enabled = true
            )
    ) {
        data class MenuItem(
            val icon: ImageVector,
            val title: String,
            val content: String,
            val onClick: () -> Unit,
        )
        val menuList = listOf(
            MenuItem(
                icon = Icons.Rounded.Start,
                title = "SIIP BPJS",
                content = "Automated collecting NIK, name, birth date and email from SIIP BPJS web. By using KPJ number",
                onClick = { navController.navigate(Route.SiipBpjsPage) }
            ),
            MenuItem(
                icon = Icons.Rounded.Start,
                title = "DPT",
                content = "Automated collecting address from DPT web. By using NIK",
                onClick = { navController.navigate(Route.DptPage) }
            ),
            MenuItem(
                icon = Icons.Rounded.Start,
                title = "Lasik",
                content = "Automated check JMO access in LASIK page by using NIK, KPJ and name",
                onClick = { navController.navigate(Route.LasikPage) }
            ),
        )
        menuList.forEach { menuItem ->
            Card(
                onClick = { menuItem.onClick.invoke() }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp)
                    ) {
                        Column {
                            CustomTextTitle(text = menuItem.title)
                            VerticalSpacer(5)
                            CustomTextHint(text = menuItem.content)
                        }
                    }
                }
            }
            VerticalSpacer(10)
        }
    }
}