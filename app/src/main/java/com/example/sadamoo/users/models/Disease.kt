package com.example.sadamoo.users.models

data class Disease(
    val id: String,
    val name: String,
    val scientificName: String,
    val description: String,
    val symptoms: List<String>,
    val causes: List<String>,
    val treatment: List<String>,
    val prevention: List<String>,
    val severity: String, // "Ringan", "Sedang", "Berat"
    val imageRes: Int,
    val isContagious: Boolean,
    val affectedAnimals: List<String>
)
