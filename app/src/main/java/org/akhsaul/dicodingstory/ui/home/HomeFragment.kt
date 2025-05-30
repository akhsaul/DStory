package org.akhsaul.dicodingstory.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.view.MenuProvider
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialElevationScale
import org.akhsaul.core.data.Result
import org.akhsaul.core.domain.model.Story
import org.akhsaul.core.util.Settings
import org.akhsaul.dicodingstory.R
import org.akhsaul.dicodingstory.adapter.ListStoryAdapter
import org.akhsaul.dicodingstory.util.collectOn
import org.akhsaul.dicodingstory.databinding.FragmentHomeBinding
import org.akhsaul.dicodingstory.util.showConfirmationDialog
import org.akhsaul.dicodingstory.util.showErrorWithToast
import org.akhsaul.dicodingstory.util.showExitConfirmationDialog
import org.akhsaul.dicodingstory.ui.base.ProgressBarControls
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.ExperimentalTime

class HomeFragment : Fragment(), KoinComponent, MenuProvider {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var _adapter: ListStoryAdapter? = null
    private val adapter get() = _adapter!!
    private val settings: Settings by inject()
    private val viewModel: HomeViewModel by viewModel()
    private var progressBar: ProgressBarControls? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ProgressBarControls) {
            progressBar = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(layoutInflater)
        _adapter = ListStoryAdapter(::onItemStoryClicked)
        binding.rvStory.adapter = adapter
        
        requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.STARTED)
        return binding.root
    }

    private fun onItemStoryClicked(
        story: Story,
        sharedView: View,
        transitionName: String
    ) {
        exitTransition = MaterialElevationScale(false)
        reenterTransition = MaterialElevationScale(true)
        findNavController().navigate(
            HomeFragmentDirections.actionHomeFragmentToDetailFragment(
                story,
                transitionName
            ),
            FragmentNavigatorExtras(sharedView to transitionName)
        )
    }

    @OptIn(ExperimentalTime::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // return transition
        postponeEnterTransition()
        view.doOnPreDraw {
            startPostponedEnterTransition()
        }

        viewModel.currentListStory.collectOn(
            lifecycleScope,
            viewLifecycleOwner
        ) {
            adapter.submitList(it)
        }

        with(binding) {
            viewModel.stateFetchListStory.collectOn(
                lifecycleScope,
                viewLifecycleOwner
            ) {
                when (it) {
                    is Result.Error -> {
                        requireContext().showErrorWithToast(
                            lifecycleScope, it.message ?: getString(R.string.txt_no_network),
                            onShow = {
                                progressBar?.hideProgressBar()
                            }
                        )
                        if (adapter.currentList.isEmpty()) {
                            textMessage(getString(R.string.txt_no_network))
                        } else {
                            textMessage(null)
                        }
                    }

                    is Result.Loading -> progressBar?.showProgressBar()
                    is Result.Success -> {
                        progressBar?.hideProgressBar()
                        if (adapter.currentList.isEmpty() && it.data.isEmpty()) {
                            textMessage(getString(R.string.txt_no_data))
                        } else {
                            textMessage(null)
                        }
                    }
                }
            }

            swipeRefresh.setOnRefreshListener {
                viewModel.triggerRefresh()
                swipeRefresh.isRefreshing = false
            }

            btnAddStory.setOnClickListener {
                findNavController().navigate(
                    R.id.action_homeFragment_to_addStoryFragment
                )
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireContext().showExitConfirmationDialog {
                activity?.finish()
            }
        }
    }

    private fun textMessage(message: String?) {
        with(binding) {
            tvMessage.text = message
            tvMessage.isVisible = message != null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        _adapter = null
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.top_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.action_settings -> {
                findNavController().navigate(
                    R.id.action_homeFragment_to_settingsFragment
                )
                true
            }

            R.id.action_logout -> {
                onButtonLogoutClicked()
                true
            }
            else -> false
        }
    }

    private fun onButtonLogoutClicked() {
        requireContext().showConfirmationDialog(
            R.string.app_name,
            R.string.logout_confirm_msg
        ) {
            settings.setUser(null)
            findNavController().navigate(
                R.id.action_homeFragment_to_loginFragment
            )
        }
    }
}