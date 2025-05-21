package org.akhsaul.core.data.source.remote.reponse

data class LoginResponse(
    val error: String,
    val message: String,
    val loginResult: UserResponse
)