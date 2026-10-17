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
import com.example.news.databinding.FragmentRegisterBinding
import kotlinx.coroutines.launch

/**
 * Màn đăng ký.
 *
 * Cũng như màn đăng nhập, màn này KHÔNG tạo tài khoản ở đâu cả: chỉ kiểm tra các ô đã có chữ và
 * hai lần mật khẩu khớp nhau, rồi vào thẳng trang chính.
 *
 * Header theo đúng nếp các màn khác: [←] ĐĂNG KÝ — màn này mở từ màn đăng nhập nên phải quay lại
 * được. Màn đăng nhập thì không có mũi tên vì nó là màn vào ứng dụng.
 */
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RegisterViewModel by viewModels { RegisterViewModel.factory() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.headerToolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        binding.headerToolbar.setNavigationContentDescription(R.string.action_back)
        binding.headerToolbar.setNavigationOnClickListener {
            Navigation.findNavController(view).navigateUp()
        }

        binding.btnRegister.setOnClickListener { submit() }

        binding.etConfirmPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submit()
                true
            } else {
                false
            }
        }

        // Quay lại màn đăng nhập. Màn đăng nhập vẫn nằm ngay dưới trong back stack nên lùi một
        // bước là tới, không cần thêm hành động điều hướng riêng.
        binding.btnOpenLogin.setOnClickListener {
            Navigation.findNavController(view).navigateUp()
        }

        binding.etName.doAfterTextChanged { viewModel.clearErrors() }
        binding.etEmail.doAfterTextChanged { viewModel.clearErrors() }
        binding.etPassword.doAfterTextChanged { viewModel.clearErrors() }
        binding.etConfirmPassword.doAfterTextChanged { viewModel.clearErrors() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect(::showErrors) }
                launch { viewModel.signedUp.collect { openMain() } }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun submit() {
        viewModel.signUp(
            displayName = binding.etName.text?.toString().orEmpty(),
            email = binding.etEmail.text?.toString().orEmpty(),
            password = binding.etPassword.text?.toString().orEmpty(),
            confirmPassword = binding.etConfirmPassword.text?.toString().orEmpty(),
        )
    }

    private fun showErrors(state: RegisterUiState) {
        binding.tilName.error = state.nameError?.let(::getString)
        binding.tilEmail.error = state.emailError?.let(::getString)
        binding.tilPassword.error = state.passwordError?.let(::getString)
        binding.tilConfirmPassword.error = state.confirmPasswordError?.let(::getString)
    }

    /**
     * Vào trang chính, dọn luôn cả màn đăng nhập lẫn màn đăng ký khỏi back stack.
     */
    private fun openMain() {
        Navigation.findNavController(requireView())
            .navigate(R.id.action_registerFragment_to_mainFragment)
    }
}
