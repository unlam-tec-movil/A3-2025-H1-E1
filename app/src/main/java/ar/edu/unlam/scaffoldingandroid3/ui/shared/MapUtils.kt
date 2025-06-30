package ar.edu.unlam.scaffoldingandroid3.ui.shared

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import ar.edu.unlam.scaffoldingandroid3.R
import ar.edu.unlam.scaffoldingandroid3.domain.model.Route
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

/**
 * Convierte un recurso de drawable vectorial (Vector Drawable) a un Bitmap.
 *
 * @param context El contexto de la aplicación.
 * @param vectorResId El ID del recurso del drawable vectorial.
 * @return Un `Bitmap` o `null` si la conversión falla.
 */
fun bitmapFromVector(
    context: Context,
    @DrawableRes vectorResId: Int,
): Bitmap? {
    return ContextCompat.getDrawable(context, vectorResId)?.run {
        setBounds(0, 0, intrinsicWidth, intrinsicHeight)
        val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
        draw(Canvas(bitmap))
        bitmap
    }
}

/**
 * Convierte un recurso de drawable vectorial (Vector Drawable) a un BitmapDescriptor
 * para ser usado como ícono en un marcador de Google Maps.
 *
 * Esta versión compuesta dibuja un ícono de primer plano sobre un fondo de pin.
 *
 * @param context El contexto de la aplicación.
 * @param vectorResId El ID del recurso del drawable vectorial (el ícono de primer plano).
 * @return Un `BitmapDescriptor` compuesto o `null` si la conversión falla.
 */
fun bitmapDescriptorFromVector(
    context: Context,
    @DrawableRes vectorResId: Int,
): BitmapDescriptor? {
    val background = ContextCompat.getDrawable(context, R.drawable.ic_marker_background) ?: return null
    background.setBounds(0, 0, background.intrinsicWidth, background.intrinsicHeight)

    val vectorDrawable = ContextCompat.getDrawable(context, vectorResId) ?: return null
    val iconSize = background.intrinsicWidth / 2 // Tamaño del ícono, ej: la mitad del pin
    val halfIconSize = iconSize / 2
    val cx = background.intrinsicWidth / 2
    val cy = background.intrinsicHeight / 2 - background.intrinsicHeight / 8 // Ligeramente hacia arriba

    vectorDrawable.setBounds(cx - halfIconSize, cy - halfIconSize, cx + halfIconSize, cy + halfIconSize)

    val bitmap = Bitmap.createBitmap(background.intrinsicWidth, background.intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    background.draw(canvas)
    vectorDrawable.draw(canvas)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

/**
 * Devuelve una URL de Google Static Maps que muestra el recorrido de [route].
 *
 * Internamente construye un parámetro `path` con todos los puntos (lat,lng) y
 * agrega marcadores al inicio y fin del trayecto. Si el listado supera los 100
 * puntos se realiza un muestreo simple para evitar URLs demasiado largas.
 *
 * @param route Recorrido a dibujar.
 * @param apiKey API Key de Google Maps. Puede quedar vacío en entornos de prueba.
 * @param width Ancho del mapa en px (máx 640 sin premium).
 * @param height Alto del mapa en px.
 * @param scale Factor de escala para mapas de alta resolución.
 * @param mapType Tipo de mapa: roadmap, satellite, terrain, hybrid.
 */
fun generateStaticMapUrl(
    route: Route,
    apiKey: String? = null,
    width: Int = 640,
    height: Int = 400,
    scale: Int = 1,
    mapType: String = "roadmap",
): String {
    if (route.points.isEmpty()) return ""

    // Google Static Maps impone un límite de caracteres por URL (~16k).
    // Realizamos un muestreo simple si la ruta es muy extensa.
    val maxPoints = 100
    val sampledPoints: List<Route.Point> =
        if (route.points.size <= maxPoints) {
            route.points
        } else {
            val step = (route.points.size / maxPoints.toDouble()).coerceAtLeast(1.0)
            route.points.filterIndexed { index, _ ->
                (index % step).toInt() == 0
            }
        }

    val path =
        sampledPoints.joinToString(separator = "|") { point ->
            "${point.latitude},${point.longitude}"
        }

    val start = route.points.first()
    val end = route.points.last()

    val baseUrl = "https://maps.googleapis.com/maps/api/staticmap"

    // Google impone 640x640 sin API key. Ajustamos
    val (finalW, finalH) =
        if (apiKey.isNullOrBlank()) {
            width.coerceAtMost(640) to height.coerceAtMost(640)
        } else {
            width to height
        }

    val params =
        buildString {
            append("size=${finalW}x$finalH")
            if (scale in 2..4) append("&scale=$scale")
            append("&maptype=$mapType")
            append("&path=weight:5|color:0x0000FF80|$path")
            // Marcador de inicio (verde) y fin (rojo)
            append("&markers=color:green|label:S|${start.latitude},${start.longitude}")
            append("&markers=color:red|label:E|${end.latitude},${end.longitude}")
            if (!apiKey.isNullOrBlank()) {
                append("&key=$apiKey")
            }
        }
    return "$baseUrl?$params"
}
