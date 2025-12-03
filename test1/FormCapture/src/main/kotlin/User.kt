package org.example

import java.time.LocalDate

data class User(
    val name : String,
    val surname : String,
    val idNum : String,
    val birthday : LocalDate
)