package ar.edu.unlam.scaffoldingandroid3.data.remote.dto.overpass

data class Element(
    val type: String,
    val id: Long,
    val lat: Double?,
    val lon: Double?,
    val tags: Tags?,
    val nodes: List<Long>?,
    val geometry: List<Geometry>?,
    val members: List<Member>?,
)
