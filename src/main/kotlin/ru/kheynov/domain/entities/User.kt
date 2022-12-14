package ru.kheynov.domain.entities

import io.ktor.server.auth.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface UserDTO {
    @Serializable
    data class User(
        @SerialName("user_id") val userId: String,
        val username: String,
        val passwordHash: String?,
        @SerialName("auth_provider") val authProvider: String,
        val email: String,
    ) : UserDTO

    @Serializable
    data class UserEmailRegister(
        val username: String,
        val password: String,
        val email: String,
    ) : UserDTO

    @Serializable
    data class UserInfo(
        @SerialName("user_id") val userId: String,
        val username: String,
        val email: String,
    ) : UserDTO
}