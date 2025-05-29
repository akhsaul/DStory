package org.akhsaul.dicodingstory.ui.settings

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import org.akhsaul.core.Settings
import org.akhsaul.core.applyAppLanguage
import org.akhsaul.core.setAppDarkMode
import org.akhsaul.dicodingstory.R
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SettingsFragment : PreferenceFragmentCompat(), KoinComponent {
    private val settings: Settings by inject()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        requireContext().resources.getStringArray(R.array.language_entries)
        preferenceManager.preferenceDataStore = settings
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        val switchMode = findPreference<SwitchPreferenceCompat>(getString(R.string.key_theme_mode))
        switchMode?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                setAppDarkMode(newValue as Boolean)
                true
            }

        val languagePreference = findPreference<ListPreference>(getString(R.string.key_language))
        languagePreference?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                applyAppLanguage(newValue as String)
                true
            }
    }
}