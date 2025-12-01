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

        //read data from our index and store bytes
        val inputStream = object{}.javaClass.getResourceAsStream("/index.html")
        val filebytes = inputStream.readBytes()

        if (exchange.requestMethod == "GET") {
            exchange.responseHeaders.add("Content-Type" , "text/html")
            exchange.sendResponseHeaders(200 , filebytes.size.toLong())
            exchange.responseBody.use { os -> os.write(filebytes)}
        }
        else if (exchange.requestMethod == "POST"){
            //mongo
        }
        else{
            //error handle?
        }
    }

    //start
    println("Started server on port 8000")
    server.start()
}

