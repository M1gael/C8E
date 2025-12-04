package org.example

import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.time.LocalDate

import com.mongodb.client.MongoClients
import org.bson.Document
import java.net.URLDecoder
import java.time.format.DateTimeFormatter


fun main() {

    //evesdrop on port 8000
    val server = HttpServer.create(InetSocketAddress(8000),0)

    //db connection
    val mongoClient = MongoClients.create("mongodb://localhost:27017")
    val database = mongoClient.getDatabase("User_Database")
    val collection = database.getCollection("User")

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

            //stream data to formData , segment and parse
            val formData = exchange.requestBody.reader().readText()
            println("Caputred Form Data :" + formData)
            val formParts = formData.split('&')

            val params = mutableMapOf<String , String>()
            for (part in formParts){
                val keyValue = part.split('=')
                params[keyValue[0]]=keyValue[1]
            }


            //assign , set sentinal values
            val name = params["name"]?.replace('+' , ' ')?.ifBlank { "INVALID" } ?: "INVALID"
            val surname = params["surname"]?.replace('+' , ' ')?.ifBlank { "INVALID" } ?: "INVALID"
            val idNum = params["id_number"]?.ifBlank { "INVALID" } ?: "INVALID"
            var birthday : LocalDate? = null
                val tempBirthday = URLDecoder.decode(((params["birthday"])?.ifBlank { "INVALID" } ?: "INVALID") , "UTF-8")//nasty,not worth the extra line
                val dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            println("Scrubbed Data : " + " " + name + " " + surname + " " + idNum + " " + tempBirthday)


            //validate
            var validData = true
            var invalidReason = "All Data Data Checks PASSED!"

            if ( (listOf(name , surname , idNum , tempBirthday).contains("INVALID")) ||
                (idNum.length != 13) ||
                (tempBirthday.count { it == '/' } != 2)) {
                    validData = false
                    invalidReason="Validation Failed Due to Sentinal INVALID"
            }

            if (validData){
                try {
                    birthday = LocalDate.parse(tempBirthday , dateFormat)
                }catch (e : Exception){
                    validData = false
                    invalidReason = "DATE : Correct Format , Invalid Values"
                }
            }

            println(invalidReason)

            //post to db
            if (validData){
                val doc = Document("name" , name)
                    .append("surname" , surname)
                    .append("idNum" , idNum)
                    .append("birthday" , birthday)

                collection.insertOne(doc)

                val postMsg = "User : " + name + " saved succesfully"
                val postMsgByteArr = postMsg.toByteArray() //better for efficiency on very large debug prints
                exchange.responseHeaders.add("Content-Type" , "text/html")
                exchange.sendResponseHeaders(200 , postMsgByteArr.size.toLong())
                exchange.responseBody.use{os -> os.write(postMsgByteArr)}
                println(postMsg)
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

