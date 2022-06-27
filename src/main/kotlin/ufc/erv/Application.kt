package ufc.erv

import io.ktor.server.application.*
import ufc.erv.db.DB
import ufc.erv.plugins.*


fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    DB.init()
    configureAuth()
    configureRouting()
    configureSerialization()
    configureMonitoring()
    configureHTTP()
}