package org.akhsaul.dicodingstory.ui.detail

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import coil3.load
import com.google.android.material.color.MaterialColors
import com.google.android.material.transition.MaterialContainerTransform
import org.akhsaul.core.data.model.domain.Story
import org.akhsaul.dicodingstory.R
import org.akhsaul.dicodingstory.databinding.FragmentDetailBinding
import org.akhsaul.dicodingstory.util.collectOn
import org.akhsaul.dicodingstory.util.showErrorWithToast
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinComponent
import java.time.format.DateTimeFormatter

class DetailFragment : Fragment(), KoinComponent {
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DetailViewModel by viewModel()
    private val args: DetailFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val story: Story = args.shareData
        viewModel.setStory(story)
        sharedElementEnterTransition = MaterialContainerTransform(requireContext(), true).apply {
            drawingViewId = R.id.fragmentContainerView
            val colorSurface: Int = MaterialColors.getColor(
                requireContext(),
                com.google.android.material.R.attr.colorSurface,
                Color.TRANSPARENT
            )
            setAllContainerColors(colorSurface)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        viewModel.setGeocoderErrorListener {
            requireContext().showErrorWithToast(
                lifecycleScope, it
            )
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.transitionName = args.transitionName

        with(binding) {
            viewModel.location.collectOn(viewLifecycleOwner) {
                tvLocation.text = it
            }
            val story = viewModel.getStory()
            ivDetailPhoto.load(story.photoUrl)
            tvDetailName.text = story.name
            tvDetailDescription.text = story.description
            tvDate.text = story.createdAt.format(
                DateTimeFormatter.ofPattern(
                    requireContext().getString(R.string.txt_detail_desc)
                )
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}