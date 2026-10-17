package com.example.news.ui.auth

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.news.NewsApplication
import com.example.news.R
import com.example.news.data.repository.AuthRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Trạng thái màn đăng ký.
 *
 * @param nameError câu báo lỗi dưới ô họ tên, null khi ô hợp lệ.
 * @param emailError câu báo lỗi dưới ô email, null khi ô hợp lệ.
 * @param passwordError câu báo lỗi dưới ô mật khẩu, null khi ô hợp lệ.
 * @param confirmPasswordError câu báo lỗi dưới ô nhập lại mật khẩu, null khi ô hợp lệ.
 */
data class RegisterUiState(
    @param:StringRes val nameError: Int? = null,
    @param:StringRes val emailError: Int? = null,
    @param:StringRes val passwordError: Int? = null,
    @param:StringRes val confirmPasswordError: Int? = null,
)

/**
 * Màn đăng ký.
 *
 * Cũng như màn đăng nhập, đây chỉ là giao diện: kiểm tra các ô có chữ và hai lần mật khẩu có
 * khớp nhau hay không, rồi mở trang chính. Không tạo tài khoản ở đâu cả, không gọi mạng.
 *
 * Việc kiểm tra "nhập lại mật khẩu" là so sánh ngay trong máy giữa hai ô trên cùng một màn hình,
 * không phải xác thực với bên ngoài.
 */
class RegisterViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _signedUp = Channel<Unit>(Channel.BUFFERED)
    val signedUp: Flow<Unit> = _signedUp.receiveAsFlow()

    fun signUp(displayName: String, email: String, password: String, confirmPassword: String) {
        val nameBlank = displayName.isBlank()
        val emailBlank = email.isBlank()
        val passwordBlank = password.isBlank()
        val confirmBlank = confirmPassword.isBlank()
        val passwordMismatch = !passwordBlank && !confirmBlank && password != confirmPassword

        _uiState.value = RegisterUiState(
            nameError = if (nameBlank) R.string.auth_name_required else null,
            emailError = if (emailBlank) R.string.auth_email_required else null,
            passwordError = if (passwordBlank) R.string.auth_password_required else null,
            confirmPasswordError = when {
                confirmBlank -> R.string.auth_confirm_password_required
                passwordMismatch -> R.string.auth_password_mismatch
                else -> null
            },
        )
        if (nameBlank || emailBlank || passwordBlank || confirmBlank || passwordMismatch) return

        viewModelScope.launch {
            // Như màn đăng nhập: bản minh hoạ luôn cho qua khi các ô đã có chữ.
            if (authRepository.signUp(displayName.trim(), email.trim(), password)) {
                _signedUp.send(Unit)
            }
        }
    }

    fun clearErrors() {
        if (_uiState.value != RegisterUiState()) {
            _uiState.value = RegisterUiState()
        }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as NewsApplication
                RegisterViewModel(authRepository = application.container.authRepository)
            }
        }
    }
}
