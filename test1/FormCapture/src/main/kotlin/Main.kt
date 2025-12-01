package org.example

import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {

    //evesdrop on port 8000
    val server = HttpServer.create(InetSocketAddress(8000),0)

    //default route
    server.createContext("/") { exchange ->
        val response = "Do your worst"
        exchange.sendResponseHeaders(200 , response.length.toLong())
        exchange.responseBody.use { os -> os.write(response.toByteArray())}
    }

    //start
    println("Started server on port 8000")
    server.start()
}