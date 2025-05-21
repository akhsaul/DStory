package org.akhsaul.core.domain.repository

import kotlinx.coroutines.flow.Flow
import org.akhsaul.core.User
import org.akhsaul.core.data.Result

interface AuthRepository {
    fun register(name: String, email: String, password: String): Flow<Result<String>>
    fun login(email: String, password: String): Flow<Result<User>>
}