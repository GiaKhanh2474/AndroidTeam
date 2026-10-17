package com.example.news.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.Navigation
import com.example.news.R
import com.example.news.databinding.FragmentLoginBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

/**
 * Màn đăng nhập.
 *
 * Màn này KHÔNG xác thực gì: hai ô nhập chỉ bị kiểm tra rỗng, sau đó vào thẳng trang chính. Dòng
 * ghi chú ở cuối màn nói rõ điều đó để người xem không hiểu nhầm là ứng dụng có tài khoản thật.
 *
 * Vì là màn vào ứng dụng (start destination) nên không có mũi tên quay lại, và cũng không có
 * cử chỉ vuốt về Main: chưa có trang chính nào ở dưới để mà vuốt về.
 */
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels { LoginViewModel.factory() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogin.setOnClickListener { submit() }

        // Bấm nút "xong" trên bàn phím cũng vào, không bắt người dùng phải với tay xuống nút.
        binding.etPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submit()
                true
            } else {
                false
            }
        }

        // Chưa có nguồn tài khoản nên chưa gửi được thư đặt lại mật khẩu; nói thẳng thay vì im lặng.
        binding.btnForgotPassword.setOnClickListener {
            Snackbar.make(binding.root, R.string.feature_unavailable, Snackbar.LENGTH_SHORT).show()
        }

        binding.btnOpenRegister.setOnClickListener {
            Navigation.findNavController(view)
                .navigate(R.id.action_loginFragment_to_registerFragment)
        }

        // Sửa lại ô nào thì bỏ câu báo lỗi cũ của chính ô đó.
        binding.etEmail.doAfterTextChanged { viewModel.clearErrors() }
        binding.etPassword.doAfterTextChanged { viewModel.clearErrors() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect(::showErrors) }
                launch { viewModel.signedIn.collect { openMain() } }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun submit() {
        viewModel.signIn(
            email = binding.etEmail.text?.toString().orEmpty(),
            password = binding.etPassword.text?.toString().orEmpty(),
        )
    }

    private fun showErrors(state: LoginUiState) {
        binding.tilEmail.error = state.emailError?.let(::getString)
        binding.tilPassword.error = state.passwordError?.let(::getString)
    }

    /**
     * Vào trang chính.
     *
     * Hành động trong nav_graph đã đặt popUpTo màn đăng nhập (kèm chính nó), nên sau bước này nút
     * Back ở trang chính thoát ứng dụng chứ không quay lại màn đăng nhập.
     */
    private fun openMain() {
        Navigation.findNavController(requireView())
            .navigate(R.id.action_loginFragment_to_mainFragment)
    }
}
