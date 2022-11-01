package ru.kheynov.api.v1.routing

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ru.kheynov.api.v1.requests.UpdateRequest
import ru.kheynov.api.v1.responses.UserInfoResponse
import ru.kheynov.di.ServiceLocator
import ru.kheynov.domain.entities.User
import ru.kheynov.domain.repositories.UserRepository
import ru.kheynov.security.hashing.HashingService


fun Route.configureUserOperations(
    hashingService: HashingService = ServiceLocator.hashingService,
    userRepository: UserRepository = ServiceLocator.userRepository,
) {
    route("/user") {
        deleteUser(userRepository)
        editUser(userRepository, hashingService)
        getUser(userRepository)
    }
}

fun Route.deleteUser(
    userRepository: UserRepository,
) {
    authenticate {
        delete {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class)
            if (userRepository.getUserByID(userId.toString()) == null) {
                call.respond(HttpStatusCode.Forbidden)
                return@delete
            }
            if (userId != null) {
                val isSuccessful = userRepository.deleteUserByID(userId)
                if (isSuccessful) call.respond(HttpStatusCode.OK, "User deleted")
                else call.respond(HttpStatusCode.NotAcceptable, "Cannot delete user")
                return@delete
            }
            call.respond(HttpStatusCode.BadRequest, "Bad UserID")
        }
    }
}


fun Route.editUser(
    userRepository: UserRepository,
    hashingService: HashingService,
) {
    authenticate {
        patch {
            lateinit var request: UpdateRequest
            try {
                request = call.receive()
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Cannot read data")
                return@patch
            }
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class)

            if (userRepository.getUserByID(userId.toString()) == null) {
                call.respond(HttpStatusCode.Forbidden)
                return@patch
            }

            val isSuccessful: Boolean

            if (userId != null) {
                if (request.username != null && request.password != null) { // full user update
                    val areFieldsBlank = request.username!!.isBlank() || request.password!!.isBlank()
                    val isPwTooShort = request.password!!.length < 8
                    if (areFieldsBlank || isPwTooShort) {
                        call.respond(HttpStatusCode.BadRequest, "Password or Login are incorrect")
                        return@patch
                    }
                    val saltedHash = hashingService.generateSaltedHash(request.password!!)
                    val user = User(
                        username = request.username!!, password = saltedHash.hash, salt = saltedHash.salt
                    )
                    isSuccessful = userRepository.updateUserByID(userId, user)
                } else if (request.username == null && request.password != null) { // update password
                    val saltedHash = hashingService.generateSaltedHash(request.password!!)

                    val isPwTooShort = request.password!!.length < 8
                    if (isPwTooShort) {
                        call.respond(HttpStatusCode.BadRequest, "Password or Login are incorrect")
                        return@patch
                    }
                    isSuccessful = userRepository.updatePasswordByID(
                        userId,
                        password = saltedHash.hash,
                        salt = saltedHash.salt,
                    )
                } else if (request.username != null) { //update username
                    if (request.username!!.isBlank()) {
                        call.respond(HttpStatusCode.BadRequest, "Incorrect login")
                        return@patch
                    }
                    isSuccessful = userRepository.updateUsernameByID(userId, request.username!!)
                } else { // error: empty fields
                    call.respond(HttpStatusCode.BadRequest, "Empty Fields")
                    return@patch
                }
                if (isSuccessful) call.respond(HttpStatusCode.OK, "User updated")
                else call.respond(HttpStatusCode.NotAcceptable, "Cannot update user, name already taken")
            }
            call.respond(HttpStatusCode.BadRequest, "Bad UserID")
            return@patch
        }
    }
}

fun Route.getUser(
    userRepository: UserRepository,
) {
    get {
        val userId = call.request.queryParameters["id"]
        if (userId.isNullOrEmpty()) {
            call.respond(HttpStatusCode.BadRequest, "Wrong user ID")
            return@get
        }
        val user = userRepository.getUserByID(userId)
        if (user == null) {
            call.respond(HttpStatusCode.NotFound)
            return@get
        }
        call.respond(
            HttpStatusCode.OK,
            UserInfoResponse(
                username = user.username,
                id = user.id.toString(),
            )
        )
    }

}