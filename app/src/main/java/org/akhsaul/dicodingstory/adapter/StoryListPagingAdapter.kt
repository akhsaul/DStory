package org.akhsaul.dicodingstory.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import org.akhsaul.core.domain.model.Story
import org.akhsaul.dicodingstory.R
import org.akhsaul.dicodingstory.databinding.ItemStoryBinding
import org.akhsaul.dicodingstory.util.DiffCallBack
import java.time.format.DateTimeFormatter

class StoryListPagingAdapter(
    private val onItemClick: (story: Story, sharedView: View, transitionName: String) -> Unit
) : PagingDataAdapter<Story, StoryListPagingAdapter.MyViewHolder>(DiffCallBack) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(
        holder: MyViewHolder,
        position: Int
    ) {
        Log.i("PagingAdapter", "onBindViewHolder: ${getItem(position)}")
        val item = getItem(position)
        if (item != null) {
            holder.bind(item)

//            val view = holder.itemView
//            val anim = ObjectAnimator.ofFloat(view, "translationX", view.rootView.width.toFloat(), 0f)
//            anim.interpolator = LinearInterpolator()
//            anim.setDuration(300).start()
        }
    }

    class MyViewHolder(
        private val binding: ItemStoryBinding,
        private val onItemClick: (Story, View, String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        private val dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm:ss z")
        fun bind(item: Story) {
            with(binding) {
                val viewTransitionName = root.context.getString(
                    R.string.transition_view_detail,
                    item.id
                )
                root.transitionName = viewTransitionName
                root.setOnClickListener {
                    Log.d(StoryListPagingAdapter::class.simpleName, "onClick: ${item.id}")
                    onItemClick(item, root, viewTransitionName)
                }

                ivItemPhoto.load(item.photoUrl)
                tvItemName.text = item.name
                tvDesc.text = item.description
                tvCreateAt.text = item.createdAt.format(dateFormatter)
            }
        }
    }
}