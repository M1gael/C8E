package org.example

import java.util.Scanner
import kotlin.random.Random
import java.sql.Connection

fun main() {

    //predefine 2 arrays
    val names = arrayOf(
        "Bruce", "Walter", "Oliver", "Elijah", "James", "William", "Benjamin", "Lucas", "Henry", "Theodore",
        "Emma", "Olivia", "Ava", "Charlotte", "Sophia", "Amelia", "Isabella", "Mia", "Evelyn", "Harper"
    )
    val surnames = arrayOf(
        "Wayne", "White", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez",
        "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson", "Thomas", "Taylor", "Moore", "Jackson", "Martin"
    )
    //map all 400 name surnames and chose there?

    val scanner = Scanner(System.`in`)

    //program loop
    var again = true
    while (again){
        println("\n\n\n========== START OF APPLICATION ==========\n")
        print("Provide the amount of records to generate : ")
        val recordCount = scanner.nextInt()
        println("\n\n\n========== ==========\n")


        val records = HashSet<String>()

        println("\n\n========== ADDING RECORDS ==========\n")
        while (records.size < recordCount){
            val name = names.random()
            var initials = name[0].toString()
                for( i in 0..name.length-2){
                    if ( name[i].toString().equals(" ") == true )
                initials = initials + name[i+1]
            }
            val surname = surnames.random()
            val birthYear = Random.nextInt(1950 , 2026)
            val birthMonth = Random.nextInt(1 , 13)
            //deterministic birthdays
            var birthDay = 0
                if ( ((birthYear%4==0) && (birthYear%100!=0)) || (birthYear%400==0) ){
                    when(birthMonth){
                        2 -> birthDay = Random.nextInt(1,30)
                        4,6,9,11 -> birthDay = Random.nextInt(1,31)
                        else -> birthDay = Random.nextInt(1,32)
                    }
                }else {
                    when(birthMonth){
                        2 -> birthDay = Random.nextInt(1,29)
                        4,6,9,11 -> birthDay = Random.nextInt(1,31)
                        else -> birthDay = Random.nextInt(1,32)
                    }
                }
            val birthDate = String.format("%02d/%02d/%d", birthDay, birthMonth, birthYear)
            val age = 2025 - birthYear
            //start building the entry list in mem
            records.add("$name,$surname,$initials,$age,$birthDate")
            //println("RECORD ${records.size} : $name $surname $initials $age $birthDate")
        }
        println("Generated ${records.size} Records. \nFinal Entry : ${records.last()}")
        println("\n========== ==========\n")

        println("\n\n========== COMPLETED ==========\n")
        print("Would you like to do another entry? (Y?) : ")
        if (scanner.next().compareTo("Y") != 0)
            again = false
    }

    scanner.close()

}