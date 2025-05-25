package org.akhsaul.dicodingstory.ui.detail

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil3.load
import org.akhsaul.core.domain.model.Story
import org.akhsaul.dicodingstory.collectOn
import org.akhsaul.dicodingstory.databinding.FragmentDetailBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinComponent
import java.time.format.DateTimeFormatter
import java.util.Locale

class DetailFragment : Fragment(), KoinComponent {
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DetailViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val story: Story = requireArguments().let {
            if (Build.VERSION.SDK_INT >= 33) {
                requireNotNull(it.getParcelable(KEY_DETAIL_DATA, Story::class.java))
            } else {
                @Suppress("deprecation")
                requireNotNull(it.getParcelable(KEY_DETAIL_DATA))
            }
        }
        viewModel.setStory(story)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        with(binding) {
            viewModel.location.collectOn(
                lifecycleScope,
                viewLifecycleOwner
            ) {
                tvLocation.text = it
            }
            val story = viewModel.getStory()
            ivDetailPhoto.load(story.photoUrl)
            tvDetailName.text = story.name
            tvDetailDescription.text = story.description
            tvDate.text = story.createdAt.format(dateFormatter)
        }
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        private val dateFormatter = DateTimeFormatter.ofPattern(
            "'Dibuat pada tanggal' dd MMMM yyyy 'jam' HH:mm:ss z",
            Locale("id")
        )
        const val KEY_DETAIL_DATA = "share_detail_data"
    }
}