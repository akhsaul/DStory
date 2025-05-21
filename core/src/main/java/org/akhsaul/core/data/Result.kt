package org.akhsaul.core.data

sealed class Result<out R> private constructor() {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error<out T>(val message: String? = null) : Result<T>()
    data object Loading : Result<Nothing>()
}
