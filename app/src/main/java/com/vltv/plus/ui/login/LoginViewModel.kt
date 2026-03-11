package com.vltv.plus.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vltv.plus.data.repository.XtreamRepository
import kotlinx.coroutines.launch

sealed class LoginState {
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}

class LoginViewModel : ViewModel() {
    
    private val repository = XtreamRepository()
    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState
    
    fun login(username: String, password: String) {
        // O viewModelScope gerencia automaticamente o cancelamento se a tela fechar
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            
            try {
                // Chamada suspend para o repositório de IPTV
                val success = repository.testLogin(username, password)
                
                if (success) {
                    _loginState.value = LoginState.Success
                } else {
                    _loginState.value = LoginState.Error("Login falhou. Verifique usuário/senha.")
                }
            } catch (e: Exception) {
                // Captura erros de rede ou DNS comuns em players de IPTV
                _loginState.value = LoginState.Error("Erro de conexão: ${e.localizedMessage}")
            }
        }
    }
}
