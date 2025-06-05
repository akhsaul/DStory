package org.akhsaul.core.data.model.reponse

data class LoginResponse(
    val error: String,
    val message: String,
    val loginResult: UserResponse
)