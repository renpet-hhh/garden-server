package ufc.erv.plugins

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ufc.erv.db.DB
import ufc.erv.data.UserSession
import ufc.erv.db.model.User
import ufc.erv.db.model.getPlantImagePath
import ufc.erv.response.UserProfileResponse
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*


suspend fun getUserFromName(call: ApplicationCall, name: String): User? {
    val user = DB.getUserByName(name)
    if (user == null) {
        call.respond(HttpStatusCode.InternalServerError)
        return null
    }
    return user
}

suspend fun getUserFromSession(call: ApplicationCall): User? {
    val username = call.principal<UserSession>()?.username
    if (username == null) {
        call.respond(HttpStatusCode.Unauthorized)
        return null
    }
    return getUserFromName(call, username)
}

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Server is operational")
        }
        get("/u/{user}/plants") {
            val username = call.parameters["user"]
            if (username == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val plants = DB.getAllPlantsFromUser(username)
            call.respond(plants)
        }
        get("/u/{user}/plant/image/{id}") {
            val username = call.parameters["user"]
            val plantId = call.parameters["id"]?.runCatching { toInt() }?.getOrNull()
            if (username == null || plantId == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val imagePath = DB.getImagePath(plantId)
            if (imagePath == null) {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }
            val image = File(imagePath)
            if (image.exists()) {
                call.respondFile(image)
                return@get
            }
            call.respond(HttpStatusCode.NotFound)
        }
        authenticate("auth-login") {
            get("/login") {
                val username = call.principal<UserIdPrincipal>()?.name
                if (username == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@get
                }
                call.sessions.set(UserSession(username))
                call.respond(HttpStatusCode.OK)
            }
        }
        authenticate("auth-session") {
            get("/profile") {
                val user = getUserFromSession(call) ?: return@get
                call.respond(UserProfileResponse(user.name, user.email, user.city.state.name, user.city.name))
            }
            post("/register/plant") {
                val user = getUserFromSession(call) ?: return@post

                var popularName: String? = null
                var scientificName: String? = null
                var description: String? = null

                var imageExtension: String? = null

                val tempPlantId = UUID.randomUUID().toString() + "-temp"
                val tempImagePath = getPlantImagePath(user.name, tempPlantId)

                val multipart = call.receiveMultipart()
                val parts = multipart.readAllParts()
                parts.forEach { part ->
                    if (part is PartData.FileItem) {
                        imageExtension = part.contentType?.contentSubtype
                        val file = File("$tempImagePath.$imageExtension")
                        part.streamProvider().use { its ->
                            file.outputStream().buffered().use { it ->
                                its.copyTo(it)
                            }
                        }
                        part.dispose()
                    }
                    if (part is PartData.FormItem) {
                        when (part.name) {
                            "popularName" -> popularName = part.value
                            "scientificName" -> scientificName = part.value
                            "description" -> description = part.value
                        }
                    }
                }
                val vPopularName = popularName
                val vImageExtension = imageExtension
                if (vPopularName == null || vImageExtension == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@post
                }
                val plantId = DB.registerPlant(user.id, vPopularName, scientificName, description, vImageExtension)

                val realImagePath = getPlantImagePath(user.name, plantId.toString())
                withContext(Dispatchers.IO) {
                    val result = runCatching {
                        Files.move(Paths.get("$tempImagePath.$imageExtension"), Paths.get("$realImagePath.$imageExtension"))
                    }
                    if (result.isFailure) {
                        // a planta foi adicionada ao BD, mas a imagem foi corrompida
                        // TODO: remover planta do BD
                        call.respond(HttpStatusCode.InternalServerError)
                        return@withContext
                    }
                    println("Usuário ${user.name} cadastrou planta $popularName")
                    call.respond(HttpStatusCode.Created)
                }
            }
        }
    }
}

