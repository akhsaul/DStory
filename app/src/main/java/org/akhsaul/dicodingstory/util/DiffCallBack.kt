package org.akhsaul.dicodingstory.util

import androidx.recyclerview.widget.DiffUtil
import org.akhsaul.core.domain.model.Story

object DiffCallBack : DiffUtil.ItemCallback<Story>() {
    override fun areItemsTheSame(oldItem: Story, newItem: Story): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Story, newItem: Story): Boolean {
        return oldItem == newItem
    }
}