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
 * Trạng thái màn đăng nhập.
 *
 * Chỉ có lỗi hiển thị dưới từng ô nhập: màn này không tải dữ liệu gì từ đâu nên không cần trạng
 * thái "đang tải".
 *
 * @param emailError câu báo lỗi dưới ô email, null khi ô hợp lệ.
 * @param passwordError câu báo lỗi dưới ô mật khẩu, null khi ô hợp lệ.
 */
data class LoginUiState(
    @param:StringRes val emailError: Int? = null,
    @param:StringRes val passwordError: Int? = null,
)

/**
 * Màn đăng nhập.
 *
 * Việc duy nhất màn này làm là kiểm tra hai ô có chữ hay không rồi báo cho giao diện mở trang
 * chính. Không so mật khẩu, không gọi mạng — xem [AuthRepository].
 */
class LoginViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /**
     * Báo một lần cho giao diện biết đã được vào ứng dụng.
     *
     * Dùng Channel thay vì thêm một trường vào trạng thái: đây là sự việc xảy ra một lần, nếu để
     * trong trạng thái thì khi xoay màn hình giao diện sẽ mở trang chính thêm lần nữa.
     */
    private val _signedIn = Channel<Unit>(Channel.BUFFERED)
    val signedIn: Flow<Unit> = _signedIn.receiveAsFlow()

    fun signIn(email: String, password: String) {
        val emailBlank = email.isBlank()
        val passwordBlank = password.isBlank()

        // Báo lỗi ngay trên máy, không chờ nguồn dữ liệu: hai ô trống thì không cần hỏi ai cả.
        _uiState.value = LoginUiState(
            emailError = if (emailBlank) R.string.auth_email_required else null,
            passwordError = if (passwordBlank) R.string.auth_password_required else null,
        )
        if (emailBlank || passwordBlank) return

        viewModelScope.launch {
            // Bản minh hoạ luôn trả về true khi hai ô đã có chữ, nên nhánh dưới không chạy. Khi
            // nối API thật, đây chính là chỗ báo "sai email hoặc mật khẩu".
            if (authRepository.signIn(email.trim(), password)) {
                _signedIn.send(Unit)
            }
        }
    }

    /** Người dùng bắt đầu sửa lại thì bỏ câu báo lỗi cũ, không để dòng đỏ nằm lại dưới ô đã sửa. */
    fun clearErrors() {
        if (_uiState.value != LoginUiState()) {
            _uiState.value = LoginUiState()
        }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as NewsApplication
                LoginViewModel(authRepository = application.container.authRepository)
            }
        }
    }
}
