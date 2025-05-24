package org.akhsaul.dicodingstory.ui.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import org.akhsaul.core.Settings
import org.akhsaul.core.data.Result
import org.akhsaul.dicodingstory.R
import org.akhsaul.dicodingstory.collectOn
import org.akhsaul.dicodingstory.databinding.FragmentLoginBinding
import org.akhsaul.dicodingstory.getText
import org.akhsaul.dicodingstory.showErrorWithToast
import org.akhsaul.dicodingstory.showMessageWithDialog
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class LoginFragment : Fragment(), KoinComponent {
    private val settings: Settings by inject()
    private val viewModel: LoginViewModel by viewModel()
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("LoginFragment", "onCreate: ${settings.hashCode()}")
        if (settings.isUserLoggedIn()) {
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
            viewModel.loginResult.collectOn(
                lifecycleScope,
                viewLifecycleOwner
            ) {
                when (it) {
                    is Result.Loading -> {
                        progressBar.isVisible = true
                        btnRegister.isEnabled = false
                        btnLogin.isEnabled = false
                    }

                    is Result.Success -> {
                        requireContext().showMessageWithDialog("Login", "Login successfully") {
                            settings.setUser(it.data)
                            btnRegister.isEnabled = true
                            btnLogin.isEnabled = true
                            findNavController().navigate(
                                R.id.action_loginFragment_to_homeFragment
                            )
                        }
                    }

                    is Result.Error -> {
                        requireContext().showErrorWithToast(
                            lifecycleScope, it.message,
                            onShow = {
                                progressBar.isVisible = false
                            },
                            onHidden = {
                                btnRegister.isEnabled = true
                                btnLogin.isEnabled = true
                            }
                        )
                    }
                }
            }

            btnLogin.setOnClickListener {
                val email = edLoginEmail.getText()
                val pass = edLoginPassword.getText()
                if (email == null || pass == null) {
                    requireContext().showErrorWithToast(
                        lifecycleScope, "Please fill all field!"
                    )
                    return@setOnClickListener
                }
                viewModel.login(email, pass)
                btnLogin.isEnabled = false
            }

            btnRegister.setOnClickListener {
                findNavController().navigate(
                    R.id.action_loginFragment_to_registerFragment
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}