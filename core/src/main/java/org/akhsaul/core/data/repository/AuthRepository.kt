package org.akhsaul.core.data.repository

import kotlinx.coroutines.flow.Flow
import org.akhsaul.core.data.model.domain.User
import org.akhsaul.core.util.Result

interface AuthRepository {
    fun register(name: String, email: String, password: String): Flow<Result<String>>
    fun login(email: String, password: String): Flow<Result<User>>
}