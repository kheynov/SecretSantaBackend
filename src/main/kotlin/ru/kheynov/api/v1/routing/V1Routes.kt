package ru.kheynov.api.v1.routing

import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import ru.kheynov.domain.use_cases.UseCases

fun Route.v1Routes() {
    route("/v1") {
        val useCases by inject<UseCases>()
        configureUserRoutes(useCases)
//        configureRoomsRoutes(useCases)
//        configureGameRoutes(useCases)
    }
}