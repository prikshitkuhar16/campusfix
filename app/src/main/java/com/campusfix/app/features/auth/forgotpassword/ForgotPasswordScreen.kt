package com.campusfix.app.features.auth.forgotpassword

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.campusfix.app.ui.theme.CampusFixTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    viewModel: ForgotPasswordViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val email by viewModel.email.collectAsState()

    ForgotPasswordContent(
        email = email,
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onSendResetEmailClick = viewModel::onSendResetEmailClick,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ForgotPasswordContent(
    email: String,
    uiState: ForgotPasswordUiState,
    onEmailChange: (String) -> Unit,
    onSendResetEmailClick: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        when (uiState) {
            is ForgotPasswordUiState.Error -> snackbarHostState.showSnackbar(uiState.message)
            is ForgotPasswordUiState.Success -> snackbarHostState.showSnackbar(uiState.message)
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Forgot Password") },
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
                    text = "Reset Password",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Enter your email and we'll send you a link to reset your password",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = uiState !is ForgotPasswordUiState.Loading
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onSendResetEmailClick,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState !is ForgotPasswordUiState.Loading
                ) {
                    if (uiState is ForgotPasswordUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        Text("Send Reset Email")
                    }
                }

                if (uiState is ForgotPasswordUiState.Success) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onNavigateBack,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Back to Login")
                    }
                }
            }

            if (uiState is ForgotPasswordUiState.Loading) {
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

@Preview(showBackground = true)
@Composable
private fun ForgotPasswordScreenPreview() {
    CampusFixTheme {
        ForgotPasswordContent(
            email = "",
            uiState = ForgotPasswordUiState.Initial,
            onEmailChange = {},
            onSendResetEmailClick = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ForgotPasswordSuccessPreview() {
    CampusFixTheme {
        ForgotPasswordContent(
            email = "user@campus.edu",
            uiState = ForgotPasswordUiState.Success("Password reset email sent. Check your inbox."),
            onEmailChange = {},
            onSendResetEmailClick = {},
            onNavigateBack = {}
        )
    }
}
