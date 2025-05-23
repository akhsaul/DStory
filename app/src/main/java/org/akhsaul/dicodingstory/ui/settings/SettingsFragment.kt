package org.akhsaul.dicodingstory.ui.settings

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import org.akhsaul.core.Settings
import org.akhsaul.dicodingstory.R
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SettingsFragment : PreferenceFragmentCompat(), KoinComponent {
    private val settings: Settings by inject()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = settings
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        val switchMode = findPreference<SwitchPreferenceCompat>("theme_mode")
        switchMode?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                settings.setAppDarkMode(newValue as Boolean)
                true
            }
    }
}