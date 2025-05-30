package org.akhsaul.dicodingstory.adapter

import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import org.akhsaul.core.domain.model.Story
import org.akhsaul.dicodingstory.R
import org.akhsaul.dicodingstory.databinding.ItemStoryBinding
import org.akhsaul.dicodingstory.util.DiffCallBack
import java.time.format.DateTimeFormatter

class ListStoryAdapter(private val listener: (Story, View, String) -> Unit) :
    ListAdapter<Story, ListStoryAdapter.MyViewHolder>(DiffCallBack) {

    class MyViewHolder(
        private val binding: ItemStoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        private val dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm:ss z")

        fun bind(item: Story, onClick: (Story, View, String) -> Unit) {
            with(binding) {
                val viewTransitionName = root.context.getString(
                    R.string.transition_view_detail,
                    item.id
                )
                root.transitionName = viewTransitionName
                root.setOnClickListener {
                    onClick(item, root, viewTransitionName)
                }
                ivItemPhoto.load(item.photoUrl)
                tvItemName.text = item.name
                tvDesc.text = item.description
                tvCreateAt.text = item.createdAt.format(dateFormatter)
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

        val view = holder.itemView
        val anim = ObjectAnimator.ofFloat(view, "translationX", view.rootView.width.toFloat(), 0f)
        anim.interpolator = LinearInterpolator()
        anim.setDuration(300).start()
    }
}

