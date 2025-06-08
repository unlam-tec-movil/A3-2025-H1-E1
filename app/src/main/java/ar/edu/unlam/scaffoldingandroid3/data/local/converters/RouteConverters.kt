package ar.edu.unlam.scaffoldingandroid3.data.local.converters

import androidx.room.TypeConverter
import ar.edu.unlam.scaffoldingandroid3.domain.model.Route
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// Room no soporta listas de objetos personalizados (como tu List<Point>), así que se necesita un
// TypeConverter y se almacena en formato Json (String).
class RouteConverters {
    @TypeConverter
    fun fromPointsList(points: List<Route.Point>): String =
        Gson().toJson(points)

    @TypeConverter
    fun toPointsList(data: String): List<Route.Point> {
        val type = object : TypeToken<List<Route.Point>>() {}.type
        return Gson().fromJson(data, type)
    }
}