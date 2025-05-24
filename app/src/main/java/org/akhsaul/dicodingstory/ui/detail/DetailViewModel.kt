package org.akhsaul.dicodingstory.ui.detail

import android.location.Address
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import org.akhsaul.core.domain.model.Story
import org.akhsaul.dicodingstory.util.MyGeocoder
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DetailViewModel : ViewModel(), KoinComponent, MyGeocoder.ResultListener {
    private var currentStory: Story? = null
    private val geocoder: MyGeocoder by inject()
    val location = MutableStateFlow("Unknown")

    fun setStory(story: Story) {
        currentStory = story
        geocoder.getAddressFrom(viewModelScope, story.lat, story.lon, 5, this)
    }

    fun getStory() = requireNotNull(currentStory)

    override fun onSuccess(address: List<Address>) {
        val validAddress = address.filter {
            it.locality != null
        }
        validAddress.firstOrNull()?.let {
            val parts = mutableListOf<String>()
            val locality: String? = it.locality
            val adminArea: String? = it.adminArea
            locality?.takeIf { it.isNotBlank() }?.let { parts.add(it.trim()) }
            adminArea?.takeIf { it.isNotBlank() }?.let { parts.add(it.trim()) }
            val newLocation = parts.joinToString(", ").takeIf { it.isNotBlank() } ?: "Unknown"
            location.tryEmit(newLocation)
        }
    }

    override fun onFailure(message: String) {
        Log.e(TAG, "Error when try to get city. message: $message")
    }

    companion object {
        private const val TAG = "DetailViewModel"
    }
}