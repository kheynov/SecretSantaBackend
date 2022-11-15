package ru.kheynov.domain.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.kheynov.utils.LocalDateSerializer
import java.time.LocalDate

@Serializable
data class Room(
    val name: String,
    val password: String?,
    @Serializable(with = LocalDateSerializer::class) val date: LocalDate?,
    @SerialName("owner_id")val ownerId: String,
    @SerialName("max_price")val maxPrice: Int? = null,
    @SerialName("game_started") val gameStarted: Boolean = false,
)

@Serializable
data class RoomUpdate(
    val password: String? = null,
    @Serializable(with = LocalDateSerializer::class) val date: LocalDate? = null,
    @SerialName("max_price")val maxPrice: Int? = null,
)