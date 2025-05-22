package org.akhsaul.dicodingstory.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import org.akhsaul.core.domain.model.Story
import org.akhsaul.dicodingstory.databinding.ItemStoryBinding
import org.akhsaul.dicodingstory.util.DiffCallBack
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class ListStoryAdapter(private val listener: (Story) -> Unit) :
    ListAdapter<Story, ListStoryAdapter.MyViewHolder>(DiffCallBack) {

    class MyViewHolder(
        private val binding: ItemStoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        private val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm:ss O")
        fun bind(item: Story, onClick: (Story) -> Unit) {
            with(binding) {
                root.setOnClickListener {
                    onClick(item)
                }
                ivPhoto.load(item.photoUrl)
                tvName.text = item.name
                tvDesc.text = item.description
                tvCreateAt.text = ZonedDateTime.parse(
                    item.createdAt,
                    DateTimeFormatter.ISO_DATE_TIME
                ).withZoneSameInstant(ZoneId.systemDefault())
                    .format(formatter)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(getItem(position), listener)
    }
}

