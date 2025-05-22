package org.akhsaul.dicodingstory.ui.register

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
import org.akhsaul.core.data.Result
import org.akhsaul.dicodingstory.databinding.FragmentRegisterBinding
import org.akhsaul.dicodingstory.getText
import org.akhsaul.dicodingstory.showErrorWithToast
import org.akhsaul.dicodingstory.showMessageWithToast
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegisterFragment : Fragment() {
    private lateinit var binding: FragmentRegisterBinding
    private val viewModel: RegisterViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(binding) {
            lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.registerResult.collect {
                        Log.i(TAG, "onViewCreated: $it")
                        when (it) {
                            is Result.Loading -> {
                                progressBar.visibility = View.VISIBLE
                                btnRegister.isEnabled = false
                            }

                            is Result.Success -> {
                                this@RegisterFragment.requireContext().showMessageWithToast(
                                    lifecycleScope, it.data,
                                    onShow = {
                                        progressBar.visibility = View.GONE
                                    },
                                    onHidden = {
                                        btnRegister.isEnabled = true
                                        // return to loginFragment
                                        findNavController().popBackStack()
                                    }
                                )
                            }

                            is Result.Error -> {
                                this@RegisterFragment.requireContext().showErrorWithToast(
                                    lifecycleScope, it.message,
                                    onShow = {
                                        progressBar.visibility = View.GONE
                                    },
                                    onHidden = {
                                        btnRegister.isEnabled = true
                                    }
                                )
                            }
                        }
                    }
                }
            }

            btnRegister.setOnClickListener {
                val name = inputName.getText()
                val email = inputEmail.getText()
                val password = inputPassword.getText()

                Log.i(TAG, "onViewCreated: $name, $email, $password")
                viewModel.register(name!!, email!!, password!!)
                btnRegister.isEnabled = false
            }
        }
    }

    companion object {
        private const val TAG = "RegisterFragment"
    }
}