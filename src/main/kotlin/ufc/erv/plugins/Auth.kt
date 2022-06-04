package ufc.erv.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import java.security.MessageDigest

fun MD5(str: String): ByteArray = MessageDigest.getInstance("MD5").digest(str.toByteArray(Charsets.UTF_8))

const val realm_ = "Authorization required"
val userTable: Map<String, ByteArray> = mapOf(
    "mock-user" to MD5("mock-user:$realm_:123456"),
)

fun Application.configureAuth() {
    install(Authentication) {
        digest("user") {
            realm = realm_
            digestProvider { userName, _ ->
                userTable[userName]
            }
        }
    }
}