package ufc.erv.plugins

import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import ufc.erv.data.Plant
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import java.io.File
import java.nio.file.Paths
import kotlin.io.path.toPath

val mockPlants: List<Plant> = listOf(
    Plant(id="0", popularName="Beijo pintado", scientificName="Impatiens hawkeri",
        description="Regas frequentes (2 a 3 vezes por semana). Luminosidade: meia-sombra. Plantar em jardineiras ou canteiros"),
    Plant(id="1", popularName="Buxinho",
        description="Regar 1 vez por semana. Luminosidade: meia-sombra ou sol pleno. Plantar em canteiros"),
    Plant(id="2", popularName="Celósia",
        description="Regar de 1 a 2 vezes por semana. Luminosidade: sol pleno. Plantar em canteiros"),
    Plant(id="3", popularName="Comigo-ninguém-pode", scientificName="Dieffenbachia seguine",
        description="Regar de 1 a 2 vezes por semana. Luminosidade: meia-sombra ou sol pleno. Plantar em canteiros ou vasos"),
    Plant(id="4", popularName="Dália",
        description="Regar 2 vezes por semana. Luminosidade: meia-sombra. Plantar em vasos, jardineiras ou canteiros"),
)


fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Server is operational")
        }
        get("/u/{user}/plants") {
            call.respond(mockPlants)
        }
        get("/u/{user}/plant/image/{id}") {
            val relativePath = "/u/${call.parameters["user"]}/plant/image/${call.parameters["id"]}.jpg"
            val resourceFile = Application::class.java.getResource(relativePath)?.toURI()?.toPath()?.toFile()
            resourceFile?.apply {
                call.respondFile(this)
            } ?: call.respond(HttpStatusCode.NotFound)
        }
        authenticate("user") {
            get("/login") {
                call.respond(HttpStatusCode.OK)
            }
            post("/register/plant") {
                val username = call.principal<UserIdPrincipal>()?.name
                if (username == null) {
                    call.respond(HttpStatusCode.InternalServerError, "Nome de usuário não encontrado, apesar de já estar autenticado")
                    return@post
                }
                val plantId = "test_id"
                val projectDirAbsolutePath = Paths.get("").toAbsolutePath().toString()
                val resourcesPath = Paths.get(projectDirAbsolutePath, "/src/main/resources").toString()
                val userPath = Paths.get(resourcesPath, "/u/${username}/plant/image").toString()
                val imagePath = Paths.get(userPath, "/$plantId").toString()

                val plant = Plant(plantId, "")
                val multipart = call.receiveMultipart()
                multipart.forEachPart { part ->
                    if (part is PartData.FileItem) {
                        val ext = part.contentType?.contentSubtype
                        val file = File("$imagePath.$ext")
                        part.streamProvider().use { its ->
                            file.outputStream().buffered().use { it ->
                                its.copyTo(it)
                            }
                        }
                        part.dispose()
                    }
                    if (part is PartData.FormItem) {
                        when (part.name) {
                            "popularName" -> plant.popularName = part.value
                            "scientificName" -> plant.scientificName = part.value
                            "localization" -> plant.localization = part.value
                            "description" -> plant.description = part.value
                        }
                    }
                }
                println("Usuário $username cadastrou planta $plant")
                call.respond(HttpStatusCode.Created)
            }
        }
    }
}

