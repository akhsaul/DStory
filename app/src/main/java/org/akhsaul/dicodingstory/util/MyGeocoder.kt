package org.akhsaul.dicodingstory.util

import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MyGeocoder : KoinComponent {
    private val geocoder: Geocoder by inject()

    interface ResultListener {
        fun onSuccess(address: List<Address>)
        fun onFailure(message: String)
    }

    fun getAddressFrom(
        scope: CoroutineScope,
        latitude: Double,
        longitude: Double,
        maxResults: Int,
        listener: ResultListener
    ) {
        if (Build.VERSION.SDK_INT >= 33) {
            getFromLocation(latitude, longitude, maxResults, listener)
        } else {
            scope.launch {
                getFromLocationLegacy(latitude, longitude, maxResults, listener)
            }
        }
    }

    /**
     * API 33+
     * */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun getFromLocation(
        latitude: Double,
        longitude: Double,
        maxResults: Int,
        listener: ResultListener
    ) {
        geocoder.getFromLocation(
            latitude,
            longitude,
            maxResults,
            object : Geocoder.GeocodeListener {
                override fun onGeocode(addresses: List<Address?>) {
                    listener.onSuccess(addresses.filterNotNull())
                }

                override fun onError(errorMessage: String?) {
                    listener.onFailure(errorMessage ?: "Unknown Error")
                }
            }
        )
    }

    /**
     * Below API 33
     * */
    @Suppress("deprecation")
    private suspend fun getFromLocationLegacy(
        latitude: Double,
        longitude: Double,
        maxResults: Int,
        listener: ResultListener
    ) {
        withContext(Dispatchers.IO) {
            runCatching {
                val result = geocoder.getFromLocation(latitude, longitude, maxResults)
                withContext(Dispatchers.Main) {
                    listener.onSuccess(result?.filterNotNull() ?: emptyList())
                }
            }.getOrElse {
                withContext(Dispatchers.Main) {
                    listener.onFailure(it.message ?: "Unknown Error")
                }
                null
            }
        }
    }
}