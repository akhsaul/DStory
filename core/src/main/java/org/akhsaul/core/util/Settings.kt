package org.akhsaul.core.util

import android.content.res.Resources
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
import java.util.Locale

class Settings(private val datastore: DataStore<Preferences>) : PreferenceDataStore() {
    private var currentUser: User? = null

    fun init(
        resources: Resources,
        supportedLocales: Array<String>,
        keyThemeMode: String,
        keyLanguage: String
    ) {
        val localeFromDataStore = getFromDataStore(stringPreferencesKey(keyLanguage))?.let {
            Locale(it)
        }
        val localeFromSystem = getSupportedSystemLocale(resources, supportedLocales)
        val locale: Locale = (localeFromDataStore ?: localeFromSystem)
        val languageCode = locale.language
        if (localeFromDataStore == null) {
            putString(keyLanguage, languageCode)
        }
        applyAppLanguage(languageCode)

        val isDarkFromDatastore = getFromDataStore(booleanPreferencesKey(keyThemeMode))
        val isDarkFromSystem = isSystemInDarkMode(resources)
        val isDark = isDarkFromDatastore ?: isDarkFromSystem
        if (isDarkFromDatastore == null) {
            putBoolean(keyThemeMode, isDark)
        }
        setAppDarkMode(isDark)
    }

    fun isUserLoggedIn(): Boolean {
        val user = getUser()
        return user != null
    }

    /**
     * Set user to login or logout
     * @param user set null if you want logout, otherwise it count as login
     * */
    fun setUser(user: User?) {
        val userData: Set<String>? = user?.let {
            setOf(it.id, it.name, it.token)
        }

        currentUser = user
        putStringSet(USER_KEY, userData)
    }

    private fun getUser(): User? {
        if (currentUser != null) {
            return currentUser
        }

        val user = getStringSet(USER_KEY, null)
        return if (user == null || user.size != 3) {
            null
        } else {
            currentUser = User(
                id = user.elementAt(0),
                name = user.elementAt(1),
                token = user.elementAt(2)
            )
            currentUser
        }
    }

    fun getAuthToken(): String? {
        return getUser()?.token
    }

    private fun <T> getFromDataStore(key: Preferences.Key<T>): T? {
        return runBlocking {
            datastore.data.firstOrNull()?.get(key)
        }
    }

    private fun <T> editDataStore(key: Preferences.Key<T>, value: T?) {
        runBlocking {
            datastore.edit {
                if (value == null) {
                    it.remove(key)
                } else {
                    it[key] = value
                }
            }
        }
    }

    override fun getString(key: String, defValue: String?): String? {
        return getFromDataStore(stringPreferencesKey(key)) ?: defValue
    }

    override fun putString(key: String, value: String?) {
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
        editDataStore(stringSetPreferencesKey(key), values)
    }

    companion object {
        private const val USER_KEY = "user"
    }
}