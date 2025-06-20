package ar.edu.unlam.scaffoldingandroid3.data.remote.dto.overpass

import com.google.gson.annotations.SerializedName

data class Tags(
    val name: String?,
    @SerializedName("route") val routeType: String?,
    val from: String?,
    val to: String?,
    val distance: String?
) 