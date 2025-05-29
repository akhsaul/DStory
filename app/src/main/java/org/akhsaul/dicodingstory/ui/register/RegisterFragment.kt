package org.akhsaul.dicodingstory.ui.register

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
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
import org.akhsaul.dicodingstory.isError
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
        binding.playAnimation()
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
                        requireContext().showMessageWithDialog(
                            getString(R.string.txt_register),
                            it.data
                        ) {
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
                            lifecycleScope, it.message ?: getString(R.string.txt_no_network),
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
                if (edRegisterName.isError() || edRegisterEmail.isError() || edRegisterPassword.isError()) {
                    requireContext().showErrorWithToast(
                        lifecycleScope, getString(R.string.txt_error_input)
                    )
                    return@setOnClickListener
                }

                val name = edRegisterName.getText()
                val email = edRegisterEmail.getText()
                val password = edRegisterPassword.getText()
                viewModel.register(name!!, email!!, password!!)
                isAllButtonEnabled(false)
            }

            btnLogin.setOnClickListener {
                findNavController().navigate(
                    R.id.action_registerFragment_to_loginFragment
                )
            }
        }
    }

    private fun FragmentRegisterBinding.playAnimation() {
        ObjectAnimator.ofFloat(image, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val title = ObjectAnimator.ofFloat(title, View.ALPHA, 1f).setDuration(300)
        val name = ObjectAnimator.ofFloat(edRegisterName, View.ALPHA, 1f).setDuration(300)
        val email = ObjectAnimator.ofFloat(edRegisterEmail, View.ALPHA, 1f).setDuration(300)
        val password = ObjectAnimator.ofFloat(edRegisterPassword, View.ALPHA, 1f).setDuration(300)

        val btnLogin = ObjectAnimator.ofFloat(btnLogin, View.ALPHA, 1f).setDuration(300)
        val btnRegister = ObjectAnimator.ofFloat(btnRegister, View.ALPHA, 1f).setDuration(300)
        val btnAnimator = AnimatorSet().apply {
            playTogether(btnLogin, btnRegister)
        }

        AnimatorSet().apply {
            startDelay = 500
            playSequentially(title, name, email, password, btnAnimator)
        }.start()
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