package com.ute.lab03_bai3
import java.io.Serializable

data class Student(
    val id: String,
    var name: String,
    var className: String,
    val email: String,
    var gpa: Double
) : Serializable