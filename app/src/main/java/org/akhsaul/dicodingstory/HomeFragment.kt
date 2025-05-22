package org.akhsaul.dicodingstory

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.LocationServices
import org.akhsaul.core.domain.model.Story
import org.akhsaul.dicodingstory.adapter.ListStoryAdapter
import org.akhsaul.dicodingstory.databinding.FragmentHomeBinding
import org.koin.core.component.KoinComponent
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class HomeFragment : Fragment(), KoinComponent {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var _adapter: ListStoryAdapter? = null
    private val adapter get() = _adapter!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(layoutInflater)
        _adapter = ListStoryAdapter {
            Log.i("HomeFragment", "onCreateView: item $it")
        }
        binding.rvStory.adapter = adapter
        return binding.root
    }

    @OptIn(ExperimentalTime::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }
        LocationServices.getFusedLocationProviderClient(requireContext())
            .lastLocation.addOnSuccessListener { location ->
                adapter.submitList(
                    buildList<Story> {
                        repeat(10) {
                            add(
                                Story(
                                    "story-FvU4u0Vp2S3PMsFg",
                                    "Akhsaul",
                                    "Tak semua keberhasilan karier di dunia digital diawali dengan mengambil pendidikan formal di bidang yang sama, sebagaimana yang Meilia Tria Andari (24) alami. Perjalanan Meilia sebagai talenta informatika justru dimulai dengan berkuliah di jurusan Matematika.",
                                    "https://dicoding-assets.sgp1.cdn.digitaloceanspaces.com/blog/wp-content/uploads/2025/05/Blog-Banner-1.png",
                                    Clock.System.now().toString(),
                                    location.latitude,
                                    location.longitude
                                )
                            )
                        }
                    }
                )
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        _adapter = null
    }
}