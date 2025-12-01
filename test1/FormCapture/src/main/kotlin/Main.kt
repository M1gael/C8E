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

        if (exchange.requestMethod == "GET") {
            //read data from our index and store bytes
            val inputStream = object{}.javaClass.getResourceAsStream("/index.html")
            val fileBytes = inputStream.readBytes()

            exchange.responseHeaders.add("Content-Type" , "text/html")
            exchange.sendResponseHeaders(200 , fileBytes.size.toLong())
            exchange.responseBody.use { os -> os.write(fileBytes)}
            println("File Bytes :" + fileBytes)
        }
        else if (exchange.requestMethod == "POST"){
            //stream data to formData , segment into formparts
            val formData = exchange.requestBody.reader().readText()
            println("Caputred Form Data :" + formData)
            val formParts = formData.split('&')

            //manual form parser
            val params = mutableMapOf<String , String>()
            for (part in formParts){
                val keyValue = part.split('=')
                params[keyValue[0]]=keyValue[1]
            }

        }
        else{
            //error handle?
        }
    }

    //start
    println("Started server on port 8000")
    server.start()
}

