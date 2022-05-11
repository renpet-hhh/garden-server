package ufc.erv

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import ufc.erv.plugins.*


fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureRouting()
        configureSerialization()
        configureMonitoring()
        configureHTTP()
    }.start(wait = true)
}
