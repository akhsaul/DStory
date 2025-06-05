package org.akhsaul.core.util

sealed class Result<out R> {
    data class Success<out T : Any>(val data: T) : Result<T>()
    data class Error<out T : Any>(val message: String? = null) : Result<T>()
    data object Loading : Result<Nothing>()
}