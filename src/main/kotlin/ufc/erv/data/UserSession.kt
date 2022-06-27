package ufc.erv.data

import io.ktor.server.auth.*


data class UserSession(
    var username: String,
): Principal
