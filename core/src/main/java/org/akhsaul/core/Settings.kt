package org.akhsaul.core

import android.content.res.Configuration
import android.content.res.Resources
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.preference.PreferenceDataStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.akhsaul.core.domain.model.User
import java.time.Duration
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class Settings(private val datastore: DataStore<Preferences>) : PreferenceDataStore() {

    fun initThemeMode(resources: Resources) {
        Log.i(TAG, "initThemeMode: Initialize mode")
        val isDarkFromDatastore = getFromDataStore(booleanPreferencesKey(THEME_MODE_KEY))
        val isDarkFromSystem = isSystemInDarkMode(resources)
        val isDark = isDarkFromDatastore ?: isDarkFromSystem
        setThemeMode(isDark)
    }

    private fun isSystemInDarkMode(resources: Resources): Boolean {
        return when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            Configuration.UI_MODE_NIGHT_NO -> false
            else -> false
        }
    }

    private fun setAppDarkMode(isDark: Boolean) {
        val compatDelegate = if (isDark) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(compatDelegate)
    }

    private fun setThemeMode(isDark: Boolean) {
        setAppDarkMode(isDark)
        putBoolean(THEME_MODE_KEY, isDark)
    }

    fun isUserLoggedIn(): Boolean {
        val lastLogin = getString(LAST_LOGIN_KEY, null) ?: return false

        val duration = Duration.between(
            ZonedDateTime.parse(lastLogin, DateTimeFormatter.ISO_INSTANT),
            ZonedDateTime.now(ZoneOffset.UTC)
        )

        return getUser() != null && duration.toMinutes() <= 60
    }

    fun setUser(user: User) {
        putStringSet(USER_KEY, setOf(user.id, user.name, user.token))
        putString(
            LAST_LOGIN_KEY,
            ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)
        )
    }

    fun getUser(): User? {
        val user = getStringSet(USER_KEY, null)
        return if (user == null || user.size != 3) {
            null
        } else {
            User(
                id = user.elementAt(0),
                name = user.elementAt(1),
                token = user.elementAt(2)
            )
        }
    }

    private fun <T> getFromDataStore(key: Preferences.Key<T>): T? {
        return runBlocking {
            datastore.data.firstOrNull()?.get(key)
        }
    }

    private fun <T> editDataStore(key: Preferences.Key<T>, value: T) {
        runBlocking {
            datastore.edit {
                it[key] = value
            }
        }
    }

    override fun getString(key: String, defValue: String?): String? {
        return getFromDataStore(stringPreferencesKey(key)) ?: defValue
    }

    override fun putString(key: String, value: String?) {
        if (value == null) return
        editDataStore(stringPreferencesKey(key), value)
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return getFromDataStore(booleanPreferencesKey(key)) ?: defValue
    }

    override fun putBoolean(key: String, value: Boolean) {
        editDataStore(booleanPreferencesKey(key), value)
    }

    override fun getStringSet(key: String, defValues: Set<String>?): Set<String>? {
        return getFromDataStore(stringSetPreferencesKey(key)) ?: defValues
    }

    override fun putStringSet(key: String, values: Set<String>?) {
        if (values == null) return
        editDataStore(stringSetPreferencesKey(key), values)
    }

    companion object {
        private const val LAST_LOGIN_KEY = "last_login"
        private const val USER_KEY = "user"
        private const val THEME_MODE_KEY = "theme_mode"
        private const val TAG = "Settings"
    }
}