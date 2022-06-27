package ufc.erv.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import io.ktor.util.*
import org.apache.commons.codec.binary.Hex
import ufc.erv.data.UserSession
import ufc.erv.db.DB
import java.security.MessageDigest

fun mD5(str: String): ByteArray = MessageDigest.getInstance("MD5").digest(str.toByteArray(Charsets.UTF_8))
const val realm_ = "Authorization required"

fun Application.configureAuth() {
    install(Authentication) {
        digest("auth-login") {
            realm = realm_
            digestProvider { username, _ ->
                DB.getUserByName(username)?.digestToken?.let { Hex.decodeHex(it) }
            }
        }
        session<UserSession>("auth-session") {
            validate { session ->
                if (session.username == "") return@validate null
                return@validate session
            }
            challenge {
                call.respond(HttpStatusCode.Unauthorized)
            }
        }
    }
    install(Sessions) {
        val secretSignKey = hex("6819b57b326945c1968f45236589")

        cookie<UserSession>("user-session", SessionStorageMemory()) {
            cookie.path = "/"
            cookie.maxAgeInSeconds = 86400 // 1 dia
            transform(SessionTransportTransformerMessageAuthentication(secretSignKey))
        }
    }
}