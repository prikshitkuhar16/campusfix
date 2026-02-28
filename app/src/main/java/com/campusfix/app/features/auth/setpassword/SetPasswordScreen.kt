package com.campusfix.app.features.auth.setpassword

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.campusfix.app.ui.theme.CampusFixTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetPasswordScreen(
    viewModel: SetPasswordViewModel,
    mode: String,
    onNavigateBack: () -> Unit,
    onPasswordSetSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val newPassword by viewModel.newPassword.collectAsState()
    val confirmPassword by viewModel.confirmPassword.collectAsState()
    val shouldNavigate by viewModel.shouldNavigate.collectAsState()

    LaunchedEffect(shouldNavigate) {
        if (shouldNavigate) {
            onPasswordSetSuccess()
            viewModel.clearNavigationFlag()
        }
    }

    SetPasswordContent(
        mode = mode,
        newPassword = newPassword,
        confirmPassword = confirmPassword,
        uiState = uiState,
        onNewPasswordChange = viewModel::onNewPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onSubmitClick = viewModel::onSubmitClick,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SetPasswordContent(
    mode: String,
    newPassword: String,
    confirmPassword: String,
    uiState: SetPasswordUiState,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSubmitClick: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is SetPasswordUiState.Error) {
            snackbarHostState.showSnackbar(uiState.message)
        }
    }

    val title = if (mode == "RESET") "Reset Password" else "Set Password"
    val subtitle = if (mode == "RESET") "Enter your new password" else "Set up your account password"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = onNewPasswordChange,
                    label = { Text("New Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = uiState !is SetPasswordUiState.Loading,
                    visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                            Icon(
                                imageVector = if (newPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (newPasswordVisible) "Hide password" else "Show password"
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = onConfirmPasswordChange,
                    label = { Text("Confirm Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = uiState !is SetPasswordUiState.Loading,
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onSubmitClick,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState !is SetPasswordUiState.Loading
                ) {
                    if (uiState is SetPasswordUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        Text("Submit")
                    }
                }
            }

            if (uiState is SetPasswordUiState.Loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Set Password - Reset Mode Initial")
@Composable
fun SetPasswordResetScreenPreview() {
    CampusFixTheme {
        SetPasswordContent(
            mode = "RESET",
            newPassword = "",
            confirmPassword = "",
            uiState = SetPasswordUiState.Initial,
            onNewPasswordChange = {},
            onConfirmPasswordChange = {},
            onSubmitClick = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, name = "Set Password - Invite Mode Initial")
@Composable
fun SetPasswordInviteScreenPreview() {
    CampusFixTheme {
        SetPasswordContent(
            mode = "INVITE",
            newPassword = "",
            confirmPassword = "",
            uiState = SetPasswordUiState.Initial,
            onNewPasswordChange = {},
            onConfirmPasswordChange = {},
            onSubmitClick = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, name = "Set Password - Filled")
@Composable
fun SetPasswordScreenFilledPreview() {
    CampusFixTheme {
        SetPasswordContent(
            mode = "RESET",
            newPassword = "newpassword123",
            confirmPassword = "newpassword123",
            uiState = SetPasswordUiState.Initial,
            onNewPasswordChange = {},
            onConfirmPasswordChange = {},
            onSubmitClick = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, name = "Set Password - Loading")
@Composable
fun SetPasswordScreenLoadingPreview() {
    CampusFixTheme {
        SetPasswordContent(
            mode = "RESET",
            newPassword = "newpassword123",
            confirmPassword = "newpassword123",
            uiState = SetPasswordUiState.Loading,
            onNewPasswordChange = {},
            onConfirmPasswordChange = {},
            onSubmitClick = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, name = "Set Password - Error")
@Composable
fun SetPasswordScreenErrorPreview() {
    CampusFixTheme {
        SetPasswordContent(
            mode = "RESET",
            newPassword = "newpassword123",
            confirmPassword = "newpassword",
            uiState = SetPasswordUiState.Error("Passwords do not match"),
            onNewPasswordChange = {},
            onConfirmPasswordChange = {},
            onSubmitClick = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, name = "Set Password - Dark Mode")
@Composable
fun SetPasswordScreenDarkPreview() {
    CampusFixTheme(darkTheme = true) {
        SetPasswordContent(
            mode = "RESET",
            newPassword = "newpassword123",
            confirmPassword = "newpassword123",
            uiState = SetPasswordUiState.Initial,
            onNewPasswordChange = {},
            onConfirmPasswordChange = {},
            onSubmitClick = {},
            onNavigateBack = {}
        )
    }
}

