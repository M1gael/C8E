package org.example

import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import kotlin.random.Random
import java.io.File
import java.net.URLDecoder
import java.sql.Driver
import java.sql.DriverManager
import java.sql.Statement
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun main() {

    // host server
    val server = HttpServer.create(InetSocketAddress(8001),0)
    var recordCount = 0
    var records = HashSet<String>()

    server.createContext("/") { exchange ->

// GET
        if (exchange.requestMethod == "GET") {
            // root
            if (exchange.requestURI.path == "/" || exchange.requestURI.path == "/index.html") {

                // read data from our index and store bytes
                val inputStream = object {}.javaClass.getResourceAsStream("/index.html")
                var pageContent = inputStream.bufferedReader().use { it.readText() }

                exchange.responseHeaders.add("Content-Type", "text/html")
                exchange.sendResponseHeaders(200, pageContent.toByteArray().size.toLong())
                exchange.responseBody.use { os -> os.write(pageContent.toByteArray()) }
            }

            // generate csv
            else if (exchange.requestURI.path == "/generate"){
                val recordCount =  exchange.requestURI.query.split("=")[1].toInt()
                records = generateRecords(recordCount)
                generateCsv(records)

                val response = """
                    <script>
                        alert("Success! Generated $recordCount records.");
                        window.location.href = "/"; // Redirects back to the home page
                    </script>
                """
                exchange.sendResponseHeaders(200, response.toByteArray().size.toLong())
                exchange.responseBody.use { os -> os.write(response.toByteArray()) }
            }
        }
// POST
        else if (exchange.requestMethod == "POST") {
            if (exchange.requestURI.path == "/import-csv") {
                try {
                    // Pass the request body stream directly to our function
                    val insertedCount = databaseStore(exchange.requestBody)
                    val response = "Success! Imported $insertedCount records into the database."

                    exchange.sendResponseHeaders(200, response.toByteArray().size.toLong())
                    exchange.responseBody.use { os -> os.write(response.toByteArray()) }

                } catch (e: Exception) {
                    val error = "Error: ${e.message}"

                    exchange.sendResponseHeaders(500, error.toByteArray().size.toLong())
                    exchange.responseBody.use { os -> os.write(error.toByteArray()) }
                    e.printStackTrace()
                }
            }
        }
    }

    server.start()

}


// ========== Helper Functions ==========


fun generateRecords(recordCount : Int) : HashSet<String> {
    val names = arrayOf(
        "Bruce Willis", "Walter Hardwell", "Oliver", "Elijah", "James", "William Lucas", "Benjamin", "Lucas", "Henry William", "Theodore",
        "Emma", "Olivia", "Ava", "Charlotte", "Sophia Rasmus", "Amelia", "Isabella", "Mia", "Evelyn", "Harper"
    )
    val surnames = arrayOf(
        "Wayne", "White", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez",
        "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson", "Thomas", "Taylor", "Moore", "Jackson", "Martin"
    )
    //maybe map all 400 name surnames and chose there?

    val records = HashSet<String>() //hash because a hash by definition is unique

    println("\n\n========== ADDING RECORDS\n")
    while (records.size < recordCount){
        val name = names.random()
        var initials = name[0].toString()
        for( i in 0..name.length-2){
            if ( name[i].toString().equals(" ") == true )
                initials = initials + name[i+1]
        }

        //assign random values & calculate dates
        val surname = surnames.random()
        val birthYear = Random.nextInt(1950 , 2026)
        val birthMonth = Random.nextInt(1 , 13)
        var birthDay = 0 // deterministic birthdates
        if ( ((birthYear%4==0) && (birthYear%100!=0)) || (birthYear%400==0) ){
            when(birthMonth){
                2 -> birthDay = Random.nextInt(1,30)
                4,6,9,11 -> birthDay = Random.nextInt(1,31)
                else -> birthDay = Random.nextInt(1,32)
            }
        } else {
            when(birthMonth){
                2 -> birthDay = Random.nextInt(1,29)
                4,6,9,11 -> birthDay = Random.nextInt(1,31)
                else -> birthDay = Random.nextInt(1,32)
            }
        }
        val birthDate = String.format("%02d/%02d/%d", birthDay, birthMonth, birthYear)
        val age = 2025 - birthYear

        // start building the entry list in mem
        records.add("\"$name\", \"$surname\", \"$initials\", \"$age\", \"$birthDate\"")
        // println("RECORD ${records.size} : $name $surname $initials $age $birthDate")
    }
    println("Generated ${records.size} Records. \nFinal Entry : ${records.last()}")
    println("\n========== ==========\n")

    return records
}


fun generateCsv(records : HashSet<String>){

    // path
    val file = File("../outputs/output.csv")
    file.parentFile.mkdirs()// in case it DNE
        // val writer = PrintWriter(file) // printWriter slower

    // increment through records and add
        // writer.append("Id,Name,Surname,Initials,Age,DateOfBirth\n") // writer.
    file.bufferedWriter().use{out ->
        out.write("Id,Name,Surname,Initials,Age,DateOfBirth")
        out.newLine()// safer apparently?
        var id = 1 // counter double as id
        for (record in records){
            out.write("\"$id\", $record")
            out.newLine()
            id++
        }
    }
}


fun databaseStore(fileInputStream :java.io.InputStream) : Int{

    // connect
    val connection = DriverManager.getConnection("jdbc:sqlite:../database/data.db")

    val statement = connection.createStatement()

    // drop existing table
    statement.executeUpdate("DROP TABLE IF EXISTS csv_import") //ensure repeatable runs

    // create fresh table
    val createTableSql = """
        CREATE TABLE  IF NOT EXISTS csv_import (
            Id TEXT,
            Name TEXT,
            Surname TEXT,
            Initials TEXT,
            Age INTEGER,
            DateOfBirth TEXT
        );
    """
    statement.executeUpdate(createTableSql)

    //now we disable autocommit
    connection.autoCommit = false // free up performance , do here to allow DROP/CREATE Table

    // prepare a statement
    val insertSql = "INSERT INTO csv_import (Id, Name, Surname, Initials, Age, DateOfBirth) VALUES (?, ?, ?, ?, ?, ?)"
    val pstmt = connection.prepareStatement(insertSql)

    // stream
    fileInputStream.bufferedReader().use { reader ->
        var line : String?
        var startProcessing = false
        var count = 0

        while (reader.readLine().also { line = it} != null){
            val currentLine = line!!.trim()

            // find csv start
            if (!startProcessing){ //data already assessed is forever lost
                if (currentLine.contains("Id,Name,Surname")) {
                    startProcessing = true
                    println("\n\n========== EVENT Drop Current Iteration\n")
                    println("Found CSV First Line\n")
                    println("========== ==========\n")
                }
                continue
            }

            // stop at multipart bound
            if (currentLine.startsWith("-----")) { //inputstream metadata gaurentee
                println("\n\n========== EVENT Drop Current Iteration\n")
                println("Found CSV Last Line\n")
                println("========== ==========\n")
                continue
            }

            // parse csv
            val parts = currentLine.split(",").map { it.trim().removeSurrounding("\"") }
            if (parts.size >= 6){
                pstmt.setString(1, parts[0]) // id
                pstmt.setString(2, parts[1]) // name
                pstmt.setString(3, parts[2]) // surname
                pstmt.setString(4, parts[3]) // initials
                pstmt.setInt(5, parts[4].toIntOrNull() ?: 0)// age
                    //convert to SQLite format?
                pstmt.setString(6, parts[5]) // dob

                // batch in 1000s
                pstmt.addBatch()
                count++

                if ( (count % 1000) == 0)
                    pstmt.executeBatch()
            }
        }

        // last batch might not meet 1000
        pstmt.executeBatch()
        connection.commit()
        connection.close()

        return count
    }
}