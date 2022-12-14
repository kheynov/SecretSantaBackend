package ru.kheynov.domain.use_cases.game

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.kheynov.domain.repositories.GameRepository
import ru.kheynov.domain.repositories.RoomsRepository
import ru.kheynov.domain.repositories.UsersRepository

class StopGameUseCase : KoinComponent {
    private val usersRepository: UsersRepository by inject()
    private val roomsRepository: RoomsRepository by inject()
    private val gameRepository: GameRepository by inject()

    sealed interface Result {
        object Successful : Result
        object Failed : Result
        object RoomNotFound : Result
        object Forbidden : Result
        object UserNotFound : Result
        object GameAlreadyStopped : Result
    }

    suspend operator fun invoke(
        userId: String,
        roomId: String,
    ): Result {
        if (usersRepository.getUserByID(userId) == null) return Result.UserNotFound
        val room = roomsRepository.getRoomById(roomId) ?: return Result.RoomNotFound
        if (room.ownerId != userId) return Result.Forbidden
        if (!room.gameStarted) return Result.GameAlreadyStopped
        val res = gameRepository.setGameState(roomId, false) && gameRepository.deleteRecipients(roomId)
        return if (res) Result.Successful else Result.Failed
    }
}