package com.canbazdev.hmskitsproject1.presentation.login

import android.app.Application
import android.text.Editable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.canbazdev.hmskitsproject1.domain.model.login.UserFirebase
import com.canbazdev.hmskitsproject1.domain.usecase.login.*
import com.canbazdev.hmskitsproject1.util.Resource
import com.canbazdev.hmskitsproject1.util.SilentSignInStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/*
*   Created by hamzacanbaz on 7/6/2022
*/
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val setEnabledSilentSignInUseCase: SetEnabledSilentSignInUseCase,
    private val checkUserLoginUseCase: CheckUserLoginUseCase,
    private val signInWithHuaweiIdUseCase: SignInWithHuaweiIdUseCase,
    private val signOutWithHuaweiUseCase: SignOutWithHuaweiUseCase,
    private val signInWithEmailUseCase: SignInWithEmailUseCase,
    private val insertUserToFirebaseUseCase: InsertUserToFirebaseUseCase,
    application: Application
) :
    AndroidViewModel(application) {
    private var _uiState = MutableStateFlow(0)
    val uiState: StateFlow<Int>
        get() = _uiState

    private var userName = MutableStateFlow("")

    private var _userEmail = MutableStateFlow("")
    val userEmail: StateFlow<String> = _userEmail

    private var _userPassword = MutableStateFlow("")
    private val userPassword: StateFlow<String> = _userPassword

    private var _isUserSignedIn = MutableStateFlow(false)
    val isUserSignedIn: StateFlow<Boolean> = _isUserSignedIn

    private val _userId = MutableStateFlow("")
    val userId: StateFlow<String> = _userId


    init {
        _uiState.value = -1
    }

    fun signInWithHuawei() {
        viewModelScope.launch {
            println("asd")
            signInWithHuaweiIdUseCase.invoke().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.silentSignIn()?.addOnSuccessListener {
                            _userEmail.value = it.email
                            _userId.value = it.unionId
                            userName.value = it.displayName
                            insertUserToFirebase()
                            _isUserSignedIn.value = true
                        }
                        setSilentSigninEnabled(true)
                        _uiState.value = 1

                    }
                    else -> {}
                }
            }
        }

    }

    fun checkUserLogin() {
        viewModelScope.launch {
            checkUserLoginUseCase.invoke(this).collect { status ->
                when (status) {
                    SilentSignInStatus.SUCCESS -> {
                        _uiState.value = 1
                    }
                    SilentSignInStatus.FAIL -> _uiState.value = -1
                    SilentSignInStatus.LOADING -> _uiState.value = 0
                    SilentSignInStatus.DISALLOWED -> _uiState.value = -2
                }
            }
        }
    }

    fun signInWithEmail() {
        viewModelScope.launch {
            signInWithEmailUseCase.invoke(
                userEmail.value,
                userPassword.value
            )
                .collect { result ->
                    when (result) {
                        is Resource.Loading -> println("loading")
                        is Resource.Success -> {
                            _userId.value = result.data.toString()
                            _isUserSignedIn.value = true
                        }
                        is Resource.Error -> println(result.errorMessage)
                    }

                }
        }

    }

    private fun setSilentSigninEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            setEnabledSilentSignInUseCase.invoke(isEnabled)
        }
    }

    private fun insertUserToFirebase() {
        var userFirebase = UserFirebase()
        userFirebase = userFirebase.copy(email = userEmail.value, id = userId.value)
        viewModelScope.launch {
            insertUserToFirebaseUseCase.invoke(userFirebase).collect {
                when (it) {
                    is Resource.Success -> println("added to fb")
                    is Resource.Loading -> println("loading fb")
                    is Resource.Error -> println("failed to fb")
                }
            }
        }
    }


    fun signOutHuawei() {
        viewModelScope.launch {
            signOutWithHuaweiUseCase.invoke().collect { result ->
                when (result) {
                    is Resource.Loading -> println("sign out loading")
                    is Resource.Success -> {
                        setSilentSigninEnabled(false)
                        println("sign out success")
                        _uiState.value = -1
                        _uiState.value = -2
                        _isUserSignedIn.value = false
                    }
                    is Resource.Error -> {
                        println("sign out error")
                        _uiState.value = 1
                        println(result.errorMessage.toString())
                    }
                }
            }
        }
    }

    fun updateEmail(s: Editable) {
        _userEmail.value = s.toString()
    }

    fun updatePassword(s: Editable) {
        _userPassword.value = s.toString()
    }


}