package com.example.testrickmorty.feature.locations.models

import android.graphics.pdf.PdfDocument


data class LocationResponse(
    val results: List<Location>,
    val info: PdfDocument.PageInfo
)
data class Location(
    val id: Int,
    val name: String,
    val type: String,
    val dimension: String,
    val residents: List<String>
)