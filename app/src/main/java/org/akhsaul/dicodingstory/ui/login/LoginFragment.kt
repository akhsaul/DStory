package org.akhsaul.dicodingstory.ui.login

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import org.akhsaul.core.data.Result
import org.akhsaul.core.util.AuthManager
import org.akhsaul.dicodingstory.R
import org.akhsaul.dicodingstory.databinding.FragmentLoginBinding
import org.akhsaul.dicodingstory.ui.base.ProgressBarControls
import org.akhsaul.dicodingstory.util.collectOn
import org.akhsaul.dicodingstory.util.getText
import org.akhsaul.dicodingstory.util.isError
import org.akhsaul.dicodingstory.util.showErrorWithToast
import org.akhsaul.dicodingstory.util.showExitConfirmationDialog
import org.akhsaul.dicodingstory.util.showMessageWithDialog
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class LoginFragment : Fragment(), KoinComponent {
    private val authManager: AuthManager by inject()
    private val viewModel: LoginViewModel by viewModel()
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private var progressBar: ProgressBarControls? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ProgressBarControls) {
            progressBar = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (authManager.isUserLoggedIn()) {
            findNavController().navigate(
                R.id.action_loginFragment_to_homeFragment
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            playAnimation()

            viewModel.loginResult.collectOn(viewLifecycleOwner) {
                when (it) {
                    is Result.Loading -> {
                        progressBar?.showProgressBar()
                        isAllButtonEnabled(false)
                    }

                    is Result.Success -> {
                        requireContext().showMessageWithDialog(
                            getString(R.string.txt_login),
                            getString(R.string.txt_login_success)
                        ) {
                            progressBar?.hideProgressBar()
                            authManager.setCurrentUser(it.data)
                            isAllButtonEnabled(true)
                            findNavController().navigate(
                                R.id.action_loginFragment_to_homeFragment
                            )
                        }
                    }

                    is Result.Error -> {
                        requireContext().showErrorWithToast(
                            lifecycleScope, it.message ?: getString(R.string.txt_error_no_network),
                            onShow = {
                                progressBar?.hideProgressBar()
                            },
                            onHidden = {
                                isAllButtonEnabled(true)
                            }
                        )
                    }
                }
            }

            btnLogin.setOnClickListener {
                if (edLoginEmail.isError() || edLoginPassword.isError()) {
                    requireContext().showErrorWithToast(
                        lifecycleScope, getString(R.string.txt_error_input)
                    )
                    return@setOnClickListener
                }

                val email = edLoginEmail.getText()
                val pass = edLoginPassword.getText()
                viewModel.login(email!!, pass!!)
                isAllButtonEnabled(false)
            }

            btnRegister.setOnClickListener {
                findNavController().navigate(
                    R.id.action_loginFragment_to_registerFragment
                )
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireContext().showExitConfirmationDialog {
                activity?.finish()
            }
        }
    }

    private fun FragmentLoginBinding.playAnimation() {
        ObjectAnimator.ofFloat(image, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val title = ObjectAnimator.ofFloat(title, View.ALPHA, 1f).setDuration(300)
        val email = ObjectAnimator.ofFloat(edLoginEmail, View.ALPHA, 1f).setDuration(300)
        val password = ObjectAnimator.ofFloat(edLoginPassword, View.ALPHA, 1f).setDuration(300)

        val btnLogin = ObjectAnimator.ofFloat(btnLogin, View.ALPHA, 1f).setDuration(300)
        val btnRegister = ObjectAnimator.ofFloat(btnRegister, View.ALPHA, 1f).setDuration(300)
        val btnAnimator = AnimatorSet().apply {
            playTogether(btnLogin, btnRegister)
        }

        AnimatorSet().apply {
            startDelay = 500
            playSequentially(title, email, password, btnAnimator)
        }.start()
    }

    private fun isAllButtonEnabled(value: Boolean) {
        with(binding) {
            btnRegister.isEnabled = value
            btnLogin.isEnabled = value
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}