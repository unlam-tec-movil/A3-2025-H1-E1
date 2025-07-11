package ar.edu.unlam.scaffoldingandroid3.data.remote.mapper

import ar.edu.unlam.scaffoldingandroid3.data.remote.dto.overpass.OverpassResponse
import ar.edu.unlam.scaffoldingandroid3.domain.logic.RouteDistanceCalculator
import ar.edu.unlam.scaffoldingandroid3.domain.model.Route

fun OverpassResponse.toDomain(calculator: RouteDistanceCalculator): List<Route> {
    // 1. Create maps for easy lookup of nodes and ways.
    val nodes = elements.filter { it.type == "node" }.associateBy { it.id }
    val ways = elements.filter { it.type == "way" }.associateBy { it.id }

    // 2. Filter for relations that are hiking routes.
    val routeRelations = elements.filter { it.type == "relation" && it.tags?.routeType == "hiking" }

    // 3. Map each relation to a domain Route object.
    return routeRelations.mapNotNull { relation ->
        // A relation needs members and tags to be a valid route.
        if (relation.members == null || relation.tags?.name == null) {
            return@mapNotNull null
        }

        val routePoints = mutableListOf<Route.Point>()

        // 4. Collect all points from the ways that are members of this relation.
        relation.members.forEach { member ->
            if (member.type == "way") {
                val way = ways[member.ref]
                // If the way has its own geometry, use it directly.
                way?.geometry?.forEach { geoPoint ->
                    routePoints.add(
                        Route.Point(
                            latitude = geoPoint.lat,
                            longitude = geoPoint.lon,
                            timestamp = 0L,
                        ),
                    )
                }
                // If not, reconstruct geometry from its nodes.
                way?.nodes?.forEach { nodeId ->
                    nodes[nodeId]?.let { node ->
                        if (node.lat != null && node.lon != null) {
                            routePoints.add(
                                Route.Point(
                                    latitude = node.lat,
                                    longitude = node.lon,
                                    timestamp = 0L,
                                ),
                            )
                        }
                    }
                }
            }
        }

        if (routePoints.size < 2) {
            return@mapNotNull null // Una ruta necesita al menos 2 puntos para tener una distancia
        }

        val distance = calculator.calculate(routePoints)

        Route(
            id = relation.id.toString(),
            name = relation.tags.name,
            points = routePoints,
            distance = distance,
            duration = 0L,
        )
    }
}
