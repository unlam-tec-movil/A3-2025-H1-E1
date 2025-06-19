package ar.edu.unlam.scaffoldingandroid3.ui.shared

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import ar.edu.unlam.scaffoldingandroid3.R
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
    @DrawableRes vectorResId: Int
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
    @DrawableRes vectorResId: Int
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