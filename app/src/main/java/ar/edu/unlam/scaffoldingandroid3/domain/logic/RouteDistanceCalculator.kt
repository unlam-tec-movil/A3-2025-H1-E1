package ar.edu.unlam.scaffoldingandroid3.domain.logic

import android.location.Location
import ar.edu.unlam.scaffoldingandroid3.domain.model.Route
import javax.inject.Inject

class RouteDistanceCalculator @Inject constructor() {

    fun calculate(points: List<Route.Point>): Double {
        var totalDistance = 0.0
        if (points.size > 1) {
            for (i in 0 until points.size - 1) {
                val startPoint = points[i]
                val endPoint = points[i + 1]
                val results = FloatArray(1)
                Location.distanceBetween(
                    startPoint.latitude, startPoint.longitude,
                    endPoint.latitude, endPoint.longitude,
                    results
                )
                totalDistance += results[0]
            }
        }
        return totalDistance
    }
} 