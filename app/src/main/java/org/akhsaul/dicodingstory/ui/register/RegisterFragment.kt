package org.akhsaul.dicodingstory.ui.register

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import org.akhsaul.core.data.Result
import org.akhsaul.dicodingstory.R
import org.akhsaul.dicodingstory.collectOn
import org.akhsaul.dicodingstory.databinding.FragmentRegisterBinding
import org.akhsaul.dicodingstory.getText
import org.akhsaul.dicodingstory.showErrorWithToast
import org.akhsaul.dicodingstory.showMessageWithDialog
import org.akhsaul.dicodingstory.ui.base.ProgressBarControls
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding: FragmentRegisterBinding get() = _binding!!
    private val viewModel: RegisterViewModel by viewModel()
    private var progressBar: ProgressBarControls? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ProgressBarControls) {
            progressBar = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(binding) {
            viewModel.registerResult.collectOn(
                lifecycleScope,
                viewLifecycleOwner
            ) {
                when (it) {
                    is Result.Loading -> {
                        progressBar?.showProgressBar()
                        isAllButtonEnabled(false)
                    }

                    is Result.Success -> {
                        requireContext().showMessageWithDialog("Register", it.data) {
                            progressBar?.hideProgressBar()
                            isAllButtonEnabled(true)
                            // return to loginFragment
                            findNavController().navigate(
                                R.id.action_registerFragment_to_loginFragment
                            )
                        }
                    }

                    is Result.Error -> {
                        requireContext().showErrorWithToast(
                            lifecycleScope, it.message,
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

            btnRegister.setOnClickListener {
                val name = edRegisterName.getText()
                val email = edRegisterEmail.getText()
                val password = edRegisterPassword.getText()

                if (name == null || email == null || password == null) {
                    requireContext().showErrorWithToast(
                        lifecycleScope, "Please fill all field!"
                    )
                    return@setOnClickListener
                }
                viewModel.register(name, email, password)
                isAllButtonEnabled(false)
            }

            btnLogin.setOnClickListener {
                findNavController().navigate(
                    R.id.action_registerFragment_to_loginFragment
                )
            }
        }
    }

    private fun isAllButtonEnabled(value: Boolean) {
        with(binding) {
            btnRegister.isEnabled = value
            btnLogin.isEnabled = value
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}