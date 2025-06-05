package org.akhsaul.dicodingstory.ui.detail

import android.content.Context
import android.location.Address
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import org.akhsaul.core.data.model.domain.Story
import org.akhsaul.dicodingstory.R
import org.akhsaul.dicodingstory.util.MyGeocoder
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject

class DetailViewModel() : ViewModel(), KoinComponent {
    private var currentStory: Story? = null
    private val geocoder: MyGeocoder by inject()
    private val defaultLocation = get<Context>().getString(R.string.txt_location_unknown)
    val location = MutableStateFlow(defaultLocation)
    private var geocoderErrorListener: (String) -> Unit = {}

    fun setStory(story: Story) {
        currentStory = story
        geocoder.getAddressFrom(
            viewModelScope,
            story.lat,
            story.lon,
            5,
            object : MyGeocoder.ResultListener {
                override fun onSuccess(address: List<Address>) {
                    geocoderSuccessListener(address)
                }

                override fun onFailure(message: String) {
                    geocoderErrorListener(message)
                }
            }
        )
    }

    fun setGeocoderErrorListener(onError: (message: String) -> Unit) {
        geocoderErrorListener = onError
    }

    fun getStory() = requireNotNull(currentStory)

    private fun geocoderSuccessListener(address: List<Address>) {
        val validAddress = address.filter {
            it.locality != null
        }
        validAddress.firstOrNull()?.let {
            val parts = mutableListOf<String>()
            val locality: String? = it.locality
            val adminArea: String? = it.adminArea
            locality?.takeIf { it.isNotBlank() }?.let { parts.add(it.trim()) }
            adminArea?.takeIf { it.isNotBlank() }?.let { parts.add(it.trim()) }
            val newLocation = parts.joinToString(", ").takeIf { it.isNotBlank() } ?: defaultLocation
            location.tryEmit(newLocation)
        }
    }
}