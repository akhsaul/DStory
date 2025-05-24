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
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import org.akhsaul.core.data.Result
import org.akhsaul.core.domain.model.Story
import org.akhsaul.dicodingstory.R
import org.akhsaul.dicodingstory.adapter.ListStoryAdapter
import org.akhsaul.dicodingstory.collectOn
import org.akhsaul.dicodingstory.databinding.FragmentHomeBinding
import org.akhsaul.dicodingstory.showErrorWithToast
import org.akhsaul.dicodingstory.showExitConfirmationDialog
import org.akhsaul.dicodingstory.ui.base.ProgressBarControls
import org.akhsaul.dicodingstory.ui.detail.DetailFragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.ExperimentalTime

class HomeFragment : Fragment(), KoinComponent, MenuProvider {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var _adapter: ListStoryAdapter? = null
    private val adapter get() = _adapter!!
    private val settings: org.akhsaul.core.Settings by inject()
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
        _adapter = ListStoryAdapter(this::onItemStoryClicked)
        binding.rvStory.adapter = adapter
        requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.STARTED)
        return binding.root
    }

    private fun onItemStoryClicked(story: Story) {
        findNavController().navigate(
            R.id.action_homeFragment_to_detailFragment,
            Bundle().apply {
                putParcelable(DetailFragment.KEY_DETAIL_DATA, story)
            }
        )
    }

    @OptIn(ExperimentalTime::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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
                            lifecycleScope, it.message,
                            onShow = {
                                progressBar?.hideProgressBar()
                            }
                        )
                        if (adapter.currentList.isEmpty()) {
                            textMessage("No internet available")
                        }
                    }

                    is Result.Loading -> progressBar?.showProgressBar()
                    is Result.Success -> {
                        progressBar?.hideProgressBar()
                        if (adapter.currentList.isEmpty()) {
                            textMessage("No data available")
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

            R.id.action_logout -> onButtonLogoutClicked()
            else -> false
        }
    }

    private fun onButtonLogoutClicked(): Boolean {
        settings.setUser(null)
        findNavController().navigate(
            R.id.action_homeFragment_to_loginFragment
        )
        return true
    }
}