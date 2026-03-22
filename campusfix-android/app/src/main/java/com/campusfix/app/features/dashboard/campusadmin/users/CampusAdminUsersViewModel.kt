package com.campusfix.app.features.dashboard.campusadmin.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusfix.app.core.util.Resource
import com.campusfix.app.data.remote.dto.BuildingDto
import com.campusfix.app.data.remote.dto.CampusUserDto
import com.campusfix.app.data.remote.dto.InviteDto
import com.campusfix.app.domain.repository.CampusAdminRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CampusAdminUsersViewModel(
    private val repository: CampusAdminRepository
) : ViewModel() {

    // ── Tab selection ──
    private val _selectedTab = MutableStateFlow(UsersTab.ACTIVE)
    val selectedTab: StateFlow<UsersTab> = _selectedTab.asStateFlow()

    // ── User list ──
    private val _usersState = MutableStateFlow<UsersUiState>(UsersUiState.Loading)
    val usersState: StateFlow<UsersUiState> = _usersState.asStateFlow()

    // ── Invites list ──
    private val _invitesState = MutableStateFlow<InvitesUiState>(InvitesUiState.Idle)
    val invitesState: StateFlow<InvitesUiState> = _invitesState.asStateFlow()

    private val _revokeState = MutableStateFlow<UserActionState>(UserActionState.Idle)
    val revokeState: StateFlow<UserActionState> = _revokeState.asStateFlow()

    // ── User detail ──
    private val _selectedUser = MutableStateFlow<CampusUserDto?>(null)
    val selectedUser: StateFlow<CampusUserDto?> = _selectedUser.asStateFlow()

    // ── Deactivate ──
    private val _deactivateState = MutableStateFlow<UserActionState>(UserActionState.Idle)
    val deactivateState: StateFlow<UserActionState> = _deactivateState.asStateFlow()

    // ── Activate ──
    private val _activateState = MutableStateFlow<UserActionState>(UserActionState.Idle)
    val activateState: StateFlow<UserActionState> = _activateState.asStateFlow()

    // ── Invite form ──
    private val _inviteEmail = MutableStateFlow("")
    val inviteEmail: StateFlow<String> = _inviteEmail.asStateFlow()

    private val _inviteSelectedBuildingId = MutableStateFlow<String?>(null)
    val inviteSelectedBuildingId: StateFlow<String?> = _inviteSelectedBuildingId.asStateFlow()

    private val _inviteState = MutableStateFlow<UserActionState>(UserActionState.Idle)
    val inviteState: StateFlow<UserActionState> = _inviteState.asStateFlow()

    // ── Buildings for dropdown ──
    private val _buildings = MutableStateFlow<List<BuildingDto>>(emptyList())
    val buildings: StateFlow<List<BuildingDto>> = _buildings.asStateFlow()

    // ── Snackbar ──
    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    init {
        loadUsers()
        loadBuildings()
    }

    // Removed onRoleFilterChange

    fun onTabSelected(tab: UsersTab) {
        _selectedTab.value = tab
        when (tab) {
            UsersTab.ACTIVE, UsersTab.INACTIVE -> loadUsers()
            UsersTab.INVITED -> loadInvites()
        }
    }

    fun loadUsers() {
        viewModelScope.launch {
            _usersState.value = UsersUiState.Loading
            // Always fetch BUILDING_ADMIN role
            when (val result = repository.getUsers("BUILDING_ADMIN")) {
                is Resource.Success -> {
                    val allUsers = result.data
                    val filteredUsers = when (_selectedTab.value) {
                        UsersTab.ACTIVE -> allUsers.filter { it.isActive }
                        UsersTab.INACTIVE -> allUsers.filter { !it.isActive }
                        else -> emptyList() // Should not happen for Active/Inactive tabs
                    }

                    if (filteredUsers.isEmpty()) {
                        _usersState.value = UsersUiState.Empty
                    } else {
                        _usersState.value = UsersUiState.Success(filteredUsers)
                    }
                }
                is Resource.Error -> {
                    _usersState.value = UsersUiState.Error(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun loadBuildings() {
        viewModelScope.launch {
            when (val result = repository.getBuildings()) {
                is Resource.Success -> _buildings.value = result.data
                is Resource.Error -> { /* silently fail */ }
                is Resource.Loading -> {}
            }
        }
    }

    fun setSelectedUser(user: CampusUserDto) {
        _selectedUser.value = user
    }

    fun deactivateUser(userId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _deactivateState.value = UserActionState.Loading
            when (val result = repository.deactivateBuildingAdmin(userId)) {
                is Resource.Success -> {
                    _deactivateState.value = UserActionState.Success(result.data)
                    _snackbarEvent.emit("User deactivated successfully")
                    loadUsers()
                    onSuccess()
                }
                is Resource.Error -> {
                    _deactivateState.value = UserActionState.Error(result.message)
                    _snackbarEvent.emit(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun activateUser(userId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _activateState.value = UserActionState.Loading
            when (val result = repository.activateBuildingAdmin(userId)) {
                is Resource.Success -> {
                    _activateState.value = UserActionState.Success(result.data)
                    _snackbarEvent.emit("User activated successfully")
                    loadUsers()
                    onSuccess()
                }
                is Resource.Error -> {
                    _activateState.value = UserActionState.Error(result.message)
                    _snackbarEvent.emit(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    // ── Invite form handlers ──

    fun loadInvites() {
        viewModelScope.launch {
            _invitesState.value = InvitesUiState.Loading
            when (val result = repository.getInvites()) {
                is Resource.Success -> {
                    if (result.data.isEmpty()) {
                        _invitesState.value = InvitesUiState.Empty
                    } else {
                        _invitesState.value = InvitesUiState.Success(result.data)
                    }
                }
                is Resource.Error -> {
                    _invitesState.value = InvitesUiState.Error(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun revokeInvite(inviteId: String) {
        viewModelScope.launch {
            _revokeState.value = UserActionState.Loading
            when (val result = repository.revokeInvite(inviteId)) {
                is Resource.Success -> {
                    _revokeState.value = UserActionState.Success(result.data)
                    _snackbarEvent.emit("Invite revoked successfully")
                    loadInvites()
                }
                is Resource.Error -> {
                    _revokeState.value = UserActionState.Error(result.message)
                    _snackbarEvent.emit(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun clearRevokeState() {
        _revokeState.value = UserActionState.Idle
    }

    // ── Invite form field handlers ──

    fun onInviteEmailChange(value: String) {
        _inviteEmail.value = value.replace("\n", "").replace("\r", "").trim()
    }

    fun onInviteBuildingSelected(buildingId: String) {
        _inviteSelectedBuildingId.value = buildingId
    }

    fun onSendInvite(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val email = _inviteEmail.value.trim()
            if (email.isBlank() || !email.contains("@")) {
                _inviteState.value = UserActionState.Error("Please enter a valid email")
                return@launch
            }
            if (_inviteSelectedBuildingId.value == null) {
                _inviteState.value = UserActionState.Error("Please select a building")
                return@launch
            }

            _inviteState.value = UserActionState.Loading

            // Only invite BUILDING_ADMIN
            val result = repository.inviteBuildingAdmin(
                email = email,
                buildingId = _inviteSelectedBuildingId.value!!
            )

            when (result) {
                is Resource.Success -> {
                    _inviteState.value = UserActionState.Success(result.data)
                    _snackbarEvent.emit("Invite sent successfully")
                    _inviteEmail.value = ""
                    _inviteSelectedBuildingId.value = null
                    onSuccess()
                }
                is Resource.Error -> {
                    _inviteState.value = UserActionState.Error(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun clearInviteState() {
        _inviteState.value = UserActionState.Idle
    }

    fun clearDeactivateState() {
        _deactivateState.value = UserActionState.Idle
    }

    fun clearActivateState() {
        _activateState.value = UserActionState.Idle
    }
}

// ── Tab ──

enum class UsersTab(val label: String) {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    INVITED("Invited")
}

// ── UI States ──

sealed class UsersUiState {
    data object Loading : UsersUiState()
    data object Empty : UsersUiState()
    data class Success(val users: List<CampusUserDto>) : UsersUiState()
    data class Error(val message: String) : UsersUiState()
}

sealed class InvitesUiState {
    data object Idle : InvitesUiState()
    data object Loading : InvitesUiState()
    data object Empty : InvitesUiState()
    data class Success(val invites: List<InviteDto>) : InvitesUiState()
    data class Error(val message: String) : InvitesUiState()
}

sealed class UserActionState {
    data object Idle : UserActionState()
    data object Loading : UserActionState()
    data class Success(val message: String) : UserActionState()
    data class Error(val message: String) : UserActionState()
}
