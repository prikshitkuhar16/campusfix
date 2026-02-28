package com.campusfix.app.features.auth.createcampus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.campusfix.app.ui.theme.CampusFixTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampusAdminDetailsScreen(
    viewModel: CampusAdminDetailsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDashboard: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val adminName by viewModel.adminName.collectAsState()
    val password by viewModel.password.collectAsState()
    val confirmPassword by viewModel.confirmPassword.collectAsState()
    val shouldNavigateToDashboard by viewModel.shouldNavigateToDashboard.collectAsState()

    LaunchedEffect(shouldNavigateToDashboard) {
        if (shouldNavigateToDashboard) {
            onNavigateToDashboard()
            viewModel.clearNavigationFlag()
        }
    }

    CampusSetPasswordContent(
        adminName = adminName,
        password = password,
        confirmPassword = confirmPassword,
        uiState = uiState,
        onAdminNameChange = viewModel::onAdminNameChange,
        onPasswordChange = viewModel::onPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onSubmitClick = viewModel::onSubmitClick,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CampusSetPasswordContent(
    adminName: String,
    password: String,
    confirmPassword: String,
    uiState: CampusAdminDetailsUiState,
    onAdminNameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSubmitClick: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        when (uiState) {
            is CampusAdminDetailsUiState.Error -> snackbarHostState.showSnackbar(uiState.message)
            is CampusAdminDetailsUiState.Success -> snackbarHostState.showSnackbar(uiState.message)
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Set Password") },
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
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Almost Done!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Enter your name and set a password for your campus admin account",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = adminName,
                    onValueChange = onAdminNameChange,
                    label = { Text("Admin Name") },
                    placeholder = { Text("Your full name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = uiState !is CampusAdminDetailsUiState.Loading
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    enabled = uiState !is CampusAdminDetailsUiState.Loading
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = onConfirmPasswordChange,
                    label = { Text("Confirm Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    enabled = uiState !is CampusAdminDetailsUiState.Loading
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onSubmitClick,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState !is CampusAdminDetailsUiState.Loading
                ) {
                    if (uiState is CampusAdminDetailsUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        Text("Create Campus")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            if (uiState is CampusAdminDetailsUiState.Loading) {
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
private fun CampusSetPasswordPreview() {
    CampusFixTheme {
        CampusSetPasswordContent(
            adminName = "",
            password = "",
            confirmPassword = "",
            uiState = CampusAdminDetailsUiState.Initial,
            onAdminNameChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onSubmitClick = {},
            onNavigateBack = {}
        )
    }
}

