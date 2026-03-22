package com.campusfix.app.features.auth.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onNavigateToHome: () -> Unit = {},
    onNavigateToCampusAdminDashboard: () -> Unit = {},
    onNavigateToStaffDashboard: () -> Unit = {},
    onNavigateToBuildingAdminDashboard: () -> Unit = {},
    onNavigateToSignup: () -> Unit = {},
    onNavigateToForgotPassword: () -> Unit = {},
    onNavigateToCreateCampus: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val navigationEvent by viewModel.navigationEvent.collectAsState()

    LaunchedEffect(navigationEvent) {
        navigationEvent?.let { event ->
            when (event) {
                is LoginEvent.NavigateToHome -> {
                    onNavigateToHome()
                }
                is LoginEvent.NavigateToCampusAdminDashboard -> {
                    onNavigateToCampusAdminDashboard()
                }
                is LoginEvent.NavigateToStaffDashboard -> {
                    onNavigateToStaffDashboard()
                }
                is LoginEvent.NavigateToBuildingAdminDashboard -> {
                    onNavigateToBuildingAdminDashboard()
                }
                is LoginEvent.NavigateToSignup -> {
                    onNavigateToSignup()
                }
                is LoginEvent.NavigateToForgotPassword -> {
                    onNavigateToForgotPassword()
                }
                is LoginEvent.NavigateToCreateCampus -> {
                    onNavigateToCreateCampus()
                }
                is LoginEvent.ShowError -> {
                }
            }
            viewModel.clearNavigationEvent()
        }
    }

    LoginContent(
        email = email,
        password = password,
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLoginClick = viewModel::onLoginClick,
        onSignupClick = viewModel::onSignupClick,
        onForgotPasswordClick = viewModel::onForgotPasswordClick,
        onCreateCampusClick = viewModel::onCreateCampusClick
    )
}

@Composable
private fun LoginContent(
    email: String,
    password: String,
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onSignupClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onCreateCampusClick: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Error) {
            snackbarHostState.showSnackbar(uiState.message)
        }
    }

    Scaffold(
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
                    text = "CampusFix",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Login to continue",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(48.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = uiState !is LoginUiState.Loading
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = uiState !is LoginUiState.Loading,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Forgot Password?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(enabled = uiState !is LoginUiState.Loading) {
                            onForgotPasswordClick()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onLoginClick,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState !is LoginUiState.Loading
                ) {
                    if (uiState is LoginUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        Text("Continue")
                    }
                }


                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Don't have an account? ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Sign up as Student",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(enabled = uiState !is LoginUiState.Loading) {
                            onSignupClick()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Want to create a new campus? ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Create Campus",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(enabled = uiState !is LoginUiState.Loading) {
                            onCreateCampusClick()
                        }
                    )
                }
            }

            if (uiState is LoginUiState.Loading) {
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

@Preview(showBackground = true, name = "Login Screen - Initial")
@Composable
fun LoginScreenPreview() {
    CampusFixTheme {
        LoginContent(
            email = "",
            password = "",
            uiState = LoginUiState.Initial,
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onSignupClick = {},
            onForgotPasswordClick = {},
            onCreateCampusClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Login Screen - With Email")
@Composable
fun LoginScreenWithEmailPreview() {
    CampusFixTheme {
        LoginContent(
            email = "student@campus.edu",
            password = "",
            uiState = LoginUiState.Initial,
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onSignupClick = {},
            onForgotPasswordClick = {},
            onCreateCampusClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Login Screen - Loading")
@Composable
fun LoginScreenLoadingPreview() {
    CampusFixTheme {
        LoginContent(
            email = "student@campus.edu",
            password = "password123",
            uiState = LoginUiState.Loading,
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onSignupClick = {},
            onForgotPasswordClick = {},
            onCreateCampusClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Login Screen - Error")
@Composable
fun LoginScreenErrorPreview() {
    CampusFixTheme {
        LoginContent(
            email = "student@campus.edu",
            password = "password123",
            uiState = LoginUiState.Error("Invalid email or password"),
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onSignupClick = {},
            onForgotPasswordClick = {},
            onCreateCampusClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Login Screen - Dark Mode")
@Composable
fun LoginScreenDarkPreview() {
    CampusFixTheme(darkTheme = true) {
        LoginContent(
            email = "student@campus.edu",
            password = "password123",
            uiState = LoginUiState.Initial,
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onSignupClick = {},
            onForgotPasswordClick = {},
            onCreateCampusClick = {}
        )
    }
}

