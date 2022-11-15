package ru.kheynov.data.repositories.rooms

import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.innerJoin
import org.ktorm.entity.add
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import ru.kheynov.data.entities.RoomMembers
import ru.kheynov.data.entities.Rooms
import ru.kheynov.data.entities.Users
import ru.kheynov.data.entities.mapToRoom
import ru.kheynov.domain.entities.Room
import ru.kheynov.domain.entities.RoomUpdate
import ru.kheynov.domain.entities.User
import ru.kheynov.domain.repositories.RoomsRepository

class PostgresRoomsRepository(
    private val database: Database,
) : RoomsRepository {

    override suspend fun createRoom(room: Room): Boolean {
        val newRoom = ru.kheynov.data.entities.Room {
            name = room.name
            password = room.password
            date = room.date
            maxPrice = room.maxPrice
            ownerId = room.ownerId
            gameStarted = false
        }
        val affectedRows = database.sequenceOf(Rooms).add(newRoom)
        return affectedRows == 1
    }

    override suspend fun deleteRoomByName(name: String): Boolean {
        val affectedRows = database.sequenceOf(Rooms).find { it.name eq name }?.delete()
        return affectedRows == 1
    }

    override suspend fun getRoomByName(name: String): Room? =
        database.sequenceOf(Rooms).find { it.name eq name }?.mapToRoom()

    override suspend fun getRoomUsers(name: String): List<User> {
        database.from(RoomMembers)
            .innerJoin(Rooms, on = RoomMembers.roomName eq Rooms.name)
            .innerJoin(Users, on = RoomMembers.userId eq Users.userId)
        TODO()
    }

    override suspend fun updateRoomByName(name: String, newRoomData: RoomUpdate): Boolean {
        val room = database.sequenceOf(Rooms).find { it.name eq name } ?: return false
        room.password = newRoomData.password ?: room.password
        room.date = newRoomData.date ?: room.date
        room.maxPrice = newRoomData.maxPrice ?: room.maxPrice
        val affectedRows = room.flushChanges()
        return affectedRows == 1
    }
}