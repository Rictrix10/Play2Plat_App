package com.ddkric.play2plat.api

data class PostalCode(
    val postalCode: String,
    val placeName: String,
    val adminName1: String, // State/Province
    val adminName2: String, // County/District
    val adminName3: String, // Municipality
    val countryCode: String,
    val lat: Double,
    val lng: Double
)