package com.remonilo.studentmanager

data class Student(
    val nim: String,
    val name: String,
    val major: String
)

val sampleStudents = listOf(
    Student("2301001", "Budi Santoso", "Informatika"),
    Student("2301002", "Siti Aminah", "Sistem Informasi"),
    Student("2301003", "Andi Wijaya", "Teknik Komputer")
)
