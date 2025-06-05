package org.akhsaul.core.util

import androidx.preference.PreferenceDataStore
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.akhsaul.core.domain.model.User

class AuthManager(private val prefs: PreferenceDataStore) {
    private var currentUser: User? = null
    private val _authExpiredEvent = MutableSharedFlow<Unit>(replay = 1)
    val authExpiredEvent = _authExpiredEvent.asSharedFlow()

    init {
        val user = prefs.getStringSet(KEY, null)
        if (user != null && user.size == 3) {
            currentUser = User(
                id = user.elementAt(0),
                name = user.elementAt(1),
                token = user.elementAt(2)
            )
        }
    }

    fun getAuthToken(): String? = currentUser?.token
    fun isUserLoggedIn(): Boolean = currentUser != null

    fun setCurrentUser(user: User) {
        currentUser = user
        prefs.putStringSet(KEY, setOf(user.id, user.name, user.token))
    }

    fun removeCurrentUser() {
        currentUser = null
        prefs.putStringSet(KEY, null)
    }

    fun triggerAuthExpired() {
        _authExpiredEvent.tryEmit(Unit)
        removeCurrentUser()
    }

    private companion object {
        const val KEY = "current_user"
    }
}