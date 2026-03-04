package com.campusfix.app.features.dashboard.campusadmin.buildings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBuildingScreen(
    viewModel: CampusAdminBuildingsViewModel,
    onNavigateBack: () -> Unit
) {
    val name by viewModel.addBuildingName.collectAsState()
    val description by viewModel.addBuildingDescription.collectAsState()
    val number by viewModel.addBuildingNumber.collectAsState()
    val addState by viewModel.addBuildingState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Building") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = viewModel::onAddBuildingNameChange,
                label = { Text("Building Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = addState !is FormActionState.Loading
            )

            OutlinedTextField(
                value = description,
                onValueChange = viewModel::onAddBuildingDescriptionChange,
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                enabled = addState !is FormActionState.Loading
            )

            OutlinedTextField(
                value = number,
                onValueChange = viewModel::onAddBuildingNumberChange,
                label = { Text("Building Number") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = addState !is FormActionState.Loading
            )

            if (addState is FormActionState.Error) {
                Text(
                    text = (addState as FormActionState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.onSaveBuilding(onSuccess = onNavigateBack) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = addState !is FormActionState.Loading
            ) {
                if (addState is FormActionState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Save Building",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}



