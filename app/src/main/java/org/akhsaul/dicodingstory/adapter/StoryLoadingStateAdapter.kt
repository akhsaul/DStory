package org.akhsaul.dicodingstory.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import org.akhsaul.dicodingstory.databinding.ItemLoadingStateBinding

class StoryLoadingStateAdapter(
    private val onError: (message: String?) -> Unit,
    private val onRetry: () -> Unit,
) : LoadStateAdapter<StoryLoadingStateAdapter.StateViewHolder>() {
    override fun onBindViewHolder(
        holder: StateViewHolder,
        loadState: LoadState
    ) {
        Log.i("StateAdapter", "onBindViewHolder: $loadState")
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): StateViewHolder {
        val binding =
            ItemLoadingStateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StateViewHolder(binding, onError, onRetry)
    }

    class StateViewHolder(
        private val binding: ItemLoadingStateBinding,
        private val onError: (String?) -> Unit,
        private val onRetry: () -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(loadState: LoadState) {
            with(binding) {
                progressBar.isVisible = loadState is LoadState.Loading
                if (loadState is LoadState.Error) {
                    onError(loadState.error.localizedMessage)
                }
                btnRetry.apply {
                    isVisible = loadState is LoadState.Error
                    setOnClickListener { onRetry() }
                }
            }
        }
    }
}