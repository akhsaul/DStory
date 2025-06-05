package org.akhsaul.dicodingstory.ui.maps

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.akhsaul.core.util.Result
import org.akhsaul.dicodingstory.R
import org.akhsaul.dicodingstory.databinding.ActivityMapsBinding
import org.akhsaul.dicodingstory.util.collectOn
import org.akhsaul.dicodingstory.util.showErrorWithToast
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinComponent

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, KoinComponent {
    private val viewModel: MapsViewModel by inject()
    private lateinit var mMap: GoogleMap
    private var _binding: ActivityMapsBinding? = null
    private val binding: ActivityMapsBinding get() = requireNotNull(_binding)

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = binding.map.getFragment<SupportMapFragment>()
        mapFragment.getMapAsync(this)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isIndoorLevelPickerEnabled = true
            isCompassEnabled = true
            isMapToolbarEnabled = true
        }

        viewModel.storyList.collectOn(owner = this) { list ->
            list.forEach {
                mMap.addMarker(
                    MarkerOptions()
                        .position(LatLng(it.lat, it.lon))
                        .title(it.name)
                        .snippet(it.description)
                )
            }
        }

        viewModel.stateFetchListStory.collectOn(owner = this) {
            when (it) {
                is Result.Error -> {
                    binding.progressBar.isVisible = false
                    showErrorWithToast(
                        lifecycleScope,
                        it.message ?: getString(R.string.txt_error_unknown)
                    )
                }

                Result.Loading -> {
                    binding.progressBar.isVisible = true
                }

                is Result.Success -> {
                    binding.progressBar.isVisible = false
                }
            }
        }

        mMap.setOnMapLongClickListener {
            mMap.addMarker(
                MarkerOptions()
                    .position(it)
                    .title("new marker")
                    .snippet("Lat ${it.latitude}, lon ${it.longitude}")
            )
        }
    }
}