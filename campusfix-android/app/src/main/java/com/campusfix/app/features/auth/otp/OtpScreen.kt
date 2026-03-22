package com.campusfix.app.features.auth.otp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.campusfix.app.ui.theme.CampusFixTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpScreen(
    viewModel: OtpViewModel,
    email: String,
    mode: OtpMode,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToCampusSetPassword: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val otp by viewModel.otp.collectAsState()
    val navigationEvent by viewModel.navigationEvent.collectAsState()

    LaunchedEffect(navigationEvent) {
        navigationEvent?.let { event ->
            when (event) {
                is OtpEvent.NavigateToHome -> {
                    onNavigateToHome()
                }
                is OtpEvent.NavigateToCampusSetPassword -> {
                    onNavigateToCampusSetPassword()
                }
                is OtpEvent.ShowError -> {
                }
            }
            viewModel.clearNavigationEvent()
        }
    }

    OtpContent(
        email = email,
        mode = mode,
        otp = otp,
        uiState = uiState,
        onOtpChange = viewModel::onOtpChange,
        onVerifyClick = viewModel::onVerifyClick,
        onResendOtp = viewModel::onResendOtp,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OtpContent(
    email: String,
    mode: OtpMode,
    otp: String,
    uiState: OtpUiState,
    onOtpChange: (String) -> Unit,
    onVerifyClick: () -> Unit,
    onResendOtp: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        if (uiState is OtpUiState.Error) {
            snackbarHostState.showSnackbar(uiState.message)
        } else if (uiState is OtpUiState.Success) {
            snackbarHostState.showSnackbar(uiState.message)
        }
    }

    val title = when (mode) {
        OtpMode.SIGNUP -> "Verify Email"
        OtpMode.CREATE_CAMPUS -> "Verify Campus Email"
    }

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
                    text = "Enter the 6-digit code sent to",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = otp,
                    onValueChange = onOtpChange,
                    label = { Text("OTP Code") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = uiState !is OtpUiState.Loading,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onVerifyClick,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState !is OtpUiState.Loading
                ) {
                    if (uiState is OtpUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        Text("Verify OTP")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Didn't receive code? ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(
                        onClick = onResendOtp,
                        enabled = uiState !is OtpUiState.Loading
                    ) {
                        Text("Resend")
                    }
                }
            }

            if (uiState is OtpUiState.Loading) {
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

@Preview(showBackground = true, name = "OTP Screen - Initial")
@Composable
fun OtpScreenPreview() {
    CampusFixTheme {
        OtpContent(
            email = "student@campus.edu",
            mode = OtpMode.SIGNUP,
            otp = "",
            uiState = OtpUiState.Initial,
            onOtpChange = {},
            onVerifyClick = {},
            onResendOtp = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, name = "OTP Screen - Filled")
@Composable
fun OtpScreenFilledPreview() {
    CampusFixTheme {
        OtpContent(
            email = "student@campus.edu",
            mode = OtpMode.SIGNUP,
            otp = "123456",
            uiState = OtpUiState.Initial,
            onOtpChange = {},
            onVerifyClick = {},
            onResendOtp = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, name = "OTP Screen - Loading")
@Composable
fun OtpScreenLoadingPreview() {
    CampusFixTheme {
        OtpContent(
            email = "student@campus.edu",
            mode = OtpMode.SIGNUP,
            otp = "123456",
            uiState = OtpUiState.Loading,
            onOtpChange = {},
            onVerifyClick = {},
            onResendOtp = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, name = "OTP Screen - Error")
@Composable
fun OtpScreenErrorPreview() {
    CampusFixTheme {
        OtpContent(
            email = "student@campus.edu",
            mode = OtpMode.SIGNUP,
            otp = "12345",
            uiState = OtpUiState.Error("Invalid OTP"),
            onOtpChange = {},
            onVerifyClick = {},
            onResendOtp = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, name = "OTP Screen - Dark Mode")
@Composable
fun OtpScreenDarkPreview() {
    CampusFixTheme(darkTheme = true) {
        OtpContent(
            email = "student@campus.edu",
            mode = OtpMode.SIGNUP,
            otp = "123456",
            uiState = OtpUiState.Initial,
            onOtpChange = {},
            onVerifyClick = {},
            onResendOtp = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, name = "OTP Screen - Create Campus")
@Composable
fun OtpScreenCreateCampusPreview() {
    CampusFixTheme {
        OtpContent(
            email = "admin@iitk.ac.in",
            mode = OtpMode.CREATE_CAMPUS,
            otp = "123456",
            uiState = OtpUiState.Initial,
            onOtpChange = {},
            onVerifyClick = {},
            onResendOtp = {},
            onNavigateBack = {}
        )
    }
}


