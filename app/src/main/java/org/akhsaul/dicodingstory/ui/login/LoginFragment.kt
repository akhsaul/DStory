package org.akhsaul.dicodingstory.ui.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import org.akhsaul.core.Settings
import org.akhsaul.core.data.Result
import org.akhsaul.dicodingstory.R
import org.akhsaul.dicodingstory.databinding.FragmentLoginBinding
import org.akhsaul.dicodingstory.getText
import org.akhsaul.dicodingstory.showErrorWithToast
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class LoginFragment : Fragment(), KoinComponent {
    private val settings: Settings by inject()
    private val loginViewModel: LoginViewModel by viewModel()
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
            lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    loginViewModel.loginResult.collect {
                        when (it) {
                            is Result.Success -> {
                                settings.setUser(it.data)
                                findNavController().navigate(
                                    R.id.action_loginFragment_to_homeFragment
                                )
                            }

                            is Result.Error -> {
                                this@LoginFragment.requireContext().showErrorWithToast(
                                    lifecycleScope, it.message,
                                    onShow = {
                                        loading.visibility = View.GONE
                                    }
                                )
                            }

                            is Result.Loading -> {
                                loading.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            }

            btnLogin.setOnClickListener {
                loginViewModel.login(edLoginEmail.getText()!!, edLoginPassword.getText()!!)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}