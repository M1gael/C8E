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
                var pageContent = inputStream.bufferedReader().use { it.readText() }

                //prevent tag display on initial visit
                pageContent = pageContent.replace("{{error_msg}}", "")
                pageContent = pageContent.replace("{{name}}", "")
                pageContent = pageContent.replace("{{surname}}", "")
                pageContent = pageContent.replace("{{id_number}}", "")
                pageContent = pageContent.replace("{{birthday}}", "")

                exchange.responseHeaders.add("Content-Type", "text/html")
                exchange.sendResponseHeaders(200, pageContent.toByteArray().size.toLong())
                exchange.responseBody.use { os -> os.write(pageContent.toByteArray()) }
                println("\n\n ========== PAGE CONTENT ========== \n\n" + pageContent + "\n\n ========== END ========== \n\n")
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
            val name = URLDecoder.decode((params["name"]?.ifBlank { "INVALID" } ?: "INVALID"), "UTF-8").trim()
            val surname = URLDecoder.decode((params["surname"]?.ifBlank { "INVALID" } ?: "INVALID"), "UTF-8").trim()
            val idNum = params["id_number"]?.ifBlank { "INVALID" } ?: "INVALID"
            var birthday : LocalDate? = null
                val tempBirthday = URLDecoder.decode(((params["birthday"])?.ifBlank { "INVALID" } ?: "INVALID") , "UTF-8")
                val dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            println("Scrubbed Data : " + name + " " + surname + " " + idNum + " " + tempBirthday)


            //validate
            var validData = true
            var invalidReason = "All Data Data Checks PASSED!"

            if ( (listOf(name , surname , idNum , tempBirthday).contains("INVALID")) ||
                (!name.matches(Regex("^[a-zA-Z\\s\\-']+$"))) ||
                (!surname.matches(Regex("^[a-zA-Z\\s\\-']+$"))) ||
                (idNum.length != 13) ||
                (!idNum.all { it.isDigit() }) ||
                (tempBirthday.count { it == '/' } != 2)) {
                    validData = false
                    invalidReason="Validation Failed Due to:  Invalid Values in ID/Name/Surname , Invalid ID length , or Invalid Date Format "
            }

            if (validData){
                try {
                    birthday = LocalDate.parse(tempBirthday , dateFormat)
                }catch (e : Exception){
                    validData = false
                    invalidReason = "DATE : Correct Format , Invalid Values"
                }
            }

            if ( birthday.toString().replace("-" , "").substring(2,8).compareTo(idNum.take(6)) != 0 ) {//find alternative to abomination
                validData = false
                invalidReason = "Specified Birthday Does not Match ID Values"
            }

            if ( (collection.find(Document("idNum" , idNum)).first()) != null) {
                validData = false
                invalidReason = "User ID Already Exists"
            }

            println(invalidReason)

            //verify unique and insert
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
//Failed Post
            }else{
                val inputStream = object {}.javaClass.getResourceAsStream("/index.html")
                var pageContent = inputStream.bufferedReader().use { it.readText() }
                //insert error with reason
                pageContent = pageContent.replace("{{error_msg}}", invalidReason)

                pageContent = pageContent.replace("{{name}}", name)
                pageContent = pageContent.replace("{{surname}}", surname)
                pageContent = pageContent.replace("{{id_number}}", idNum)
                pageContent = pageContent.replace("{{birthday}}", tempBirthday)

                exchange.sendResponseHeaders(200, pageContent.length.toLong())
                exchange.responseBody.use { os -> os.write(pageContent.toByteArray()) }
            }

        }
    }

    //start
    println("Started server on port 8000")
    server.start()
}

