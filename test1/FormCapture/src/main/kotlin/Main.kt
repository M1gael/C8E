package org.example

import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.time.LocalDate

import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import org.bson.Document


fun main() {

    //evesdrop on port 8000
    val server = HttpServer.create(InetSocketAddress(8000),0)

    //default route
    server.createContext("/") { exchange ->

//GET
        if (exchange.requestMethod == "GET") {
            if (exchange.requestURI.path == "/" || exchange.requestURI.path == "/index.html") { //prevent double log of filebytes
                //read data from our index and store bytes
                val inputStream = object {}.javaClass.getResourceAsStream("/index.html")
                val fileBytes = inputStream.readBytes()

                exchange.responseHeaders.add("Content-Type", "text/html")
                exchange.sendResponseHeaders(200, fileBytes.size.toLong())
                exchange.responseBody.use { os -> os.write(fileBytes) }
                println("File Bytes :" + fileBytes)
            }
        }
//POST
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

            //assign and scrub
            val name = params["name"]?.replace('+' , ' ')?.ifBlank { "INVALID" } ?: "INVALID"
            val surname = params["surname"]?.replace('+' , ' ')?.ifBlank { "INVALID" } ?: "INVALID"
            val idNum = params["id_number"]?.ifBlank { "INVALID" } ?: "0"
            val birthday = LocalDate.parse(params["birthday"]?.ifBlank { "1900-01-01" } ?: "1900-01-01") //reformat date
                //val dateList = birthdayTemp.split('-') //to strictly adhere to '/' ??
                //val birthday = LocalDate.parse(dateList[2]+'/'+dateList[1]+'/'+dateList[0])//format as object //but dates are best in objects for db

            //build user , connect and post to db
            if ((idNum != "0") && (name != "INVALID") && (surname != "INVALID") && (birthday != LocalDate.parse("1900-01-01"))){

                val newUser = User(name , surname , idNum , birthday)

                val mongoClient = MongoClients.create("mongodb://localhost:27017")
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

