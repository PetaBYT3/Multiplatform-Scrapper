package org.scrapper.multiplatform.page

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import org.koin.compose.viewmodel.koinViewModel
import org.scrapper.multiplatform.action.LoginAction
import org.scrapper.multiplatform.effect.LoginEffect
import org.scrapper.multiplatform.route.Route
import org.scrapper.multiplatform.state.LoginState
import org.scrapper.multiplatform.template.CustomFilledButton
import org.scrapper.multiplatform.template.CustomTextContent
import org.scrapper.multiplatform.template.CustomTextField
import org.scrapper.multiplatform.template.CustomTextMedium
import org.scrapper.multiplatform.template.HorizontalSpacer
import org.scrapper.multiplatform.template.VerticalSpacer
import org.scrapper.multiplatform.template.Warning
import org.scrapper.multiplatform.viewmodel.LoginViewModel

@Composable
fun LoginPage(
    navController: NavController,
    viewModel: LoginViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onAction = viewModel::onAction

    Scaffold(
        navController = navController,
        state = state,
        onAction = onAction
    )

    LaunchedEffect(Unit) {
        viewModel.initData()
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is LoginEffect.Navigate -> {
                    navController.navigate(effect.route) {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            }
        }
    }
}

@Composable
private fun Scaffold(
    navController: NavController,
    state: LoginState,
    onAction: (LoginAction) -> Unit
) {
    Scaffold(
        modifier = Modifier
            .imePadding(),
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Content(
                    navController = navController,
                    state = state,
                    onAction = onAction
                )
            }
        }
    )
}

@Composable
private fun Content(
    navController: NavController,
    state: LoginState,
    onAction: (LoginAction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 15.dp)
    ) {
        var isPasswordVisible by rememberSaveable { mutableStateOf(true) }

        Spacer(Modifier.height(100.dp))
        CustomTextMedium(text = "Speed Runner")
        CustomTextContent(text = "SIIP, DPT, and LASIK auto checker")
        VerticalSpacer(10)
        CustomTextField(
            value = state.username,
            onValueChange = { onAction(LoginAction.Username(it)) },
            placeholder = "Username",
            leadingIcon = Icons.Filled.Person
        )
        VerticalSpacer(10)
        TextField(
            modifier = Modifier
                .fillMaxWidth(),
            value = state.password,
            onValueChange = { onAction(LoginAction.Password(it)) },
            placeholder = { Text(text = "Password") },
            leadingIcon = { Icon(imageVector = Icons.Filled.Password, contentDescription = null) },
            visualTransformation = if (isPasswordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = {
                IconButton (
                    onClick = { isPasswordVisible = !isPasswordVisible }
                ) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = null
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            )
        )
        VerticalSpacer(10)
        CustomFilledButton(
            title = "Login",
            onClick = {
                if (state.username.isBlank() || state.password.isBlank()) {
                    onAction(LoginAction.MessageDialog(
                        color = Warning,
                        icon = Icons.Filled.Warning,
                        message = "Username or password cannot be empty !"
                    ))
                } else {
                    onAction(LoginAction.Login)
                }
            },
        )
        VerticalSpacer(10)
        AnimatedVisibility(
            visible = state.dialogVisibility,
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(25))
                        .background(state.dialogColor)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = state.iconDialog,
                            contentDescription = null,
                            tint = Color.Black
                        )
                        HorizontalSpacer(10)
                        Text(
                            text = state.messageDialog,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black
                        )
                    }
                }
            }
        )
    }
}