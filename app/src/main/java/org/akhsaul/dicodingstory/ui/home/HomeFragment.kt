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
import androidx.paging.LoadState
import androidx.recyclerview.widget.ConcatAdapter
import com.google.android.material.transition.MaterialElevationScale
import org.akhsaul.core.data.model.domain.Story
import org.akhsaul.core.util.AuthManager
import org.akhsaul.dicodingstory.R
import org.akhsaul.dicodingstory.adapter.StoryListPagingAdapter
import org.akhsaul.dicodingstory.adapter.StoryLoadingStateAdapter
import org.akhsaul.dicodingstory.databinding.FragmentHomeBinding
import org.akhsaul.dicodingstory.ui.base.ProgressBarControls
import org.akhsaul.dicodingstory.util.collectOn
import org.akhsaul.dicodingstory.util.showConfirmationDialog
import org.akhsaul.dicodingstory.util.showErrorWithToast
import org.akhsaul.dicodingstory.util.showExitConfirmationDialog
import org.akhsaul.dicodingstory.util.showMessageWithDialog
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.ExperimentalTime

class HomeFragment : Fragment(), KoinComponent, MenuProvider {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var _adapter: StoryListPagingAdapter? = null
    private val adapter get() = _adapter!!
    private val authManager: AuthManager by inject()
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
        _adapter = StoryListPagingAdapter(::onItemStoryClicked)

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

        with(binding) {
            var loadingAdapter = StoryLoadingStateAdapter(
                onError = { message ->
                    requireContext().showErrorWithToast(
                        lifecycleScope, message ?: getString(R.string.txt_error_no_network),
                    )
                },
                onRetry = { adapter.retry() }
            )

            adapter.addLoadStateListener { loadState ->
                swipeRefresh.isRefreshing = loadState.refresh is LoadState.Loading

                // Handle refresh errors (initial loading failures)
                if (loadState.refresh is LoadState.Error) {
                    if (adapter.itemCount == 0) {
                        val state = loadState.refresh as LoadState.Error
                        val errorMessage =
                            state.error.localizedMessage ?: getString(R.string.txt_error_no_network)
                        txtMessage.text = errorMessage
                        txtMessage.isVisible = true
                    } else {
                        txtMessage.isVisible = false
                    }
                }

                // handle refresh state when api doesn't have data
                if (loadState.refresh is LoadState.NotLoading) {
                    if (adapter.itemCount == 0) {
                        txtMessage.setText(R.string.txt_no_data)
                        txtMessage.isVisible = true
                    } else {
                        txtMessage.isVisible = false
                    }
                }

                if (loadState.refresh is LoadState.Error) {
                    // show error when refresh state is error
                    loadingAdapter.loadState = loadState.refresh
                } else {
                    loadingAdapter.loadState = loadState.append
                }
            }
            rvStory.adapter = ConcatAdapter(adapter, loadingAdapter)

            viewModel.storyPaging.collectOn(viewLifecycleOwner) {
                adapter.submitData(it)
            }

            swipeRefresh.setOnRefreshListener {
                adapter.refresh()
            }

            btnAddStory.setOnClickListener {
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToAddStoryFragment()
                )
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireContext().showExitConfirmationDialog {
                activity?.finish()
            }
        }

        authManager.authExpiredEvent.collectOn(viewLifecycleOwner) {
            requireContext().showMessageWithDialog(
                getString(R.string.app_name),
                getString(R.string.txt_session_expired)
            ) {
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToLoginFragment()
                )
            }
        }

        adapter.refresh()
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
                    HomeFragmentDirections.actionHomeFragmentToSettingsFragment()
                )
                true
            }

            R.id.action_logout -> {
                onButtonLogoutClicked()
                true
            }

            R.id.action_maps -> {
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToMapsActivity()
                )
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
            authManager.removeCurrentUser()
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToLoginFragment()
            )
        }
    }
}