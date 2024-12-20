package com.ddkric.play2plat.api

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GameInfo (
    val id: Int,
    val name: String,
    val description: String,
    val isFree: Boolean,
    val releaseDate: String, // ou pode ser um tipo Date/LocalDate dependendo do uso
    val pegiInfo: Int,
    val coverImage: String?, // Use '?' para torná-lo opcional
    val sequence: String,
    val company: String,
    val genres: List<String>, // Lista de gêneros
    val platforms: List<String>,
    val averageStars: Float
) : Parcelable
