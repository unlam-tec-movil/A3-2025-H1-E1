package ar.edu.unlam.scaffoldingandroid3.data.remote.dto

import com.google.gson.annotations.SerializedName

data class OverpassResponse(
    val elements: List<Element>
)

data class Element(
    val type: String,
    val id: Long,
    val lat: Double?,
    val lon: Double?,
    val tags: Tags?,
    val nodes: List<Long>?,
    val geometry: List<Geometry>?,
    val members: List<Member>?
)

data class Tags(
    val name: String?,
    @SerializedName("route") val routeType: String?,
    val from: String?,
    val to: String?,
    val distance: String?
)

data class Geometry(
    val lat: Double,
    val lon: Double
)

data class Member(
    val type: String,
    val ref: Long,
    val role: String
) 