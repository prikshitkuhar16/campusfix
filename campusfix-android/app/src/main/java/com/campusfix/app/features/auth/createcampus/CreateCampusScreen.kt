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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.campusfix.app.ui.theme.CampusFixTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCampusScreen(
    viewModel: CreateCampusViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToOtp: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val officialEmail by viewModel.officialEmail.collectAsState()
    val campusName by viewModel.campusName.collectAsState()
    val campusAddress by viewModel.campusAddress.collectAsState()
    val description by viewModel.description.collectAsState()
    val shouldNavigateToOtp by viewModel.shouldNavigateToOtp.collectAsState()

    LaunchedEffect(shouldNavigateToOtp) {
        if (shouldNavigateToOtp) {
            onNavigateToOtp()
            viewModel.clearNavigationFlag()
        }
    }

    CreateCampusContent(
        officialEmail = officialEmail,
        campusName = campusName,
        campusAddress = campusAddress,
        description = description,
        uiState = uiState,
        onOfficialEmailChange = viewModel::onOfficialEmailChange,
        onCampusNameChange = viewModel::onCampusNameChange,
        onCampusAddressChange = viewModel::onCampusAddressChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onSendOtpClick = viewModel::onSendOtpClick,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateCampusContent(
    officialEmail: String,
    campusName: String,
    campusAddress: String,
    description: String,
    uiState: CreateCampusUiState,
    onOfficialEmailChange: (String) -> Unit,
    onCampusNameChange: (String) -> Unit,
    onCampusAddressChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSendOtpClick: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        if (uiState is CreateCampusUiState.Error) {
            snackbarHostState.showSnackbar(uiState.message)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Campus") },
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
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Register Your Campus",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Fill in the details to create a new campus",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = officialEmail,
                    onValueChange = onOfficialEmailChange,
                    label = { Text("Official Domain Email") },
                    placeholder = { Text("admin@yourcampus.edu") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = uiState !is CreateCampusUiState.Loading
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = campusName,
                    onValueChange = onCampusNameChange,
                    label = { Text("Campus Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = uiState !is CreateCampusUiState.Loading
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = campusAddress,
                    onValueChange = onCampusAddressChange,
                    label = { Text("Campus Address") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = uiState !is CreateCampusUiState.Loading
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    enabled = uiState !is CreateCampusUiState.Loading
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onSendOtpClick,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState !is CreateCampusUiState.Loading
                ) {
                    if (uiState is CreateCampusUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        Text("Send OTP & Continue")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            if (uiState is CreateCampusUiState.Loading) {
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
private fun CreateCampusScreenPreview() {
    CampusFixTheme {
        CreateCampusContent(
            officialEmail = "",
            campusName = "",
            campusAddress = "",
            description = "",
            uiState = CreateCampusUiState.Initial,
            onOfficialEmailChange = {},
            onCampusNameChange = {},
            onCampusAddressChange = {},
            onDescriptionChange = {},
            onSendOtpClick = {},
            onNavigateBack = {}
        )
    }
}
