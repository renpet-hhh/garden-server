package ufc.erv

import io.ktor.http.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlin.test.*
import io.ktor.server.testing.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import ufc.erv.data.Plant
import ufc.erv.plugins.configureHTTP
import ufc.erv.plugins.configureRouting
import ufc.erv.plugins.configureSerialization

class ApplicationTest {
    @Test
    fun testRoot() = testApplication {
        application {
            configureSerialization()
            configureRouting()
            configureHTTP()
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Server is operational", bodyAsText())
        }
        client.get("/u/mock-user/plants").apply {
            assertEquals(HttpStatusCode.OK, status)
            val plants = Json.decodeFromString<List<Plant>>(bodyAsText())
            assertEquals(5, plants.size)
            assertEquals("Beijo pintado", plants[0].popularName)
        }
        client.get("/u/mock-user/plant/image/0").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
        client.get("/u/mock-user/plant/image/1").apply {
            assertEquals(HttpStatusCode.NotFound, status)
        }
        client.get("/u/non-existent-user/plant/image/0").apply {
            assertEquals(HttpStatusCode.NotFound, status)
        }
    }
}