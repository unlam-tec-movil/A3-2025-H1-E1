package ar.edu.unlam.scaffoldingandroid3.ui.explore

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import ar.edu.unlam.scaffoldingandroid3.R
import ar.edu.unlam.scaffoldingandroid3.domain.model.Route
import ar.edu.unlam.scaffoldingandroid3.ui.theme.LabelBackgroundColor
import ar.edu.unlam.scaffoldingandroid3.ui.theme.dimens
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline

@Composable
internal fun MapContent(
    modifier: Modifier = Modifier,
    uiState: MapUiState,
    cameraPositionState: CameraPositionState,
    mapStyleOptions: MapStyleOptions?,
    onRouteClick: (Route) -> Unit,
    onMapIdle: (CameraPosition) -> Unit,
    markerBitmap: Bitmap?,
    hikerBitmap: Bitmap?,
) {
    // This LaunchedEffect is tied to the lifecycle of MapContent
    LaunchedEffect(cameraPositionState) {
        snapshotFlow { cameraPositionState.isMoving }
            .collect { isMoving ->
                if (!isMoving) {
                    onMapIdle(cameraPositionState.position)
                }
            }
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        contentPadding = PaddingValues(bottom = MaterialTheme.dimens.mapContentPaddingBottom),
        properties =
            MapProperties(
                isMyLocationEnabled = uiState.isLocationEnabled,
                mapStyleOptions = mapStyleOptions,
            ),
        uiSettings =
            MapUiSettings(
                myLocationButtonEnabled = uiState.isLocationEnabled,
                zoomControlsEnabled = true,
            ),
    ) {
        // Dibujar Polyline dentro del contexto del mapa
        uiState.selectedRoute?.let { selRoute ->
            val polyPoints = selRoute.points.map { LatLng(it.latitude, it.longitude) }
            if (polyPoints.size >= 2) {
                Polyline(points = polyPoints, color = Color.Blue, width = 8f)
            }
        }
    }

    // The logic for displaying markers is now contained here
    val density = LocalDensity.current
    uiState.nearbyRoutes.forEach { route ->
        route.points.firstOrNull()?.let { startPoint ->
            val routeLatLng = LatLng(startPoint.latitude, startPoint.longitude)

            val screenPos = cameraPositionState.projection?.toScreenLocation(routeLatLng)

            if (screenPos != null && markerBitmap != null && hikerBitmap != null) {
                val xDp = with(density) { (screenPos.x - markerBitmap.width / 2).toDp() }
                val yDp = with(density) { (screenPos.y - markerBitmap.height).toDp() }
                Box(
                    modifier = Modifier.offset(x = xDp, y = yDp),
                ) {
                    CustomMarkerView(
                        routeName = route.name,
                        showLabel = cameraPositionState.position.zoom > 11f,
                        markerBitmap = markerBitmap,
                        hikerBitmap = hikerBitmap,
                        onIconClick = { onRouteClick(route) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomMarkerView(
    routeName: String,
    showLabel: Boolean,
    markerBitmap: Bitmap,
    hikerBitmap: Bitmap,
    onIconClick: () -> Unit,
) {
    Box(contentAlignment = Alignment.TopCenter) {
        if (showLabel) {
            RouteLabel(
                name = routeName,
                modifier = Modifier.offset(y = MaterialTheme.dimens.labelOffset),
            )
        }
        Image(
            bitmap = markerBitmap.asImageBitmap(),
            contentDescription = stringResource(id = R.string.map_route_pin_content_description),
            modifier = Modifier.size(MaterialTheme.dimens.markerSize),
        )
        Image(
            bitmap = hikerBitmap.asImageBitmap(),
            contentDescription = stringResource(R.string.map_hiker_icon_content_description),
            modifier =
                Modifier
                    .offset(y = MaterialTheme.dimens.paddingExtraSmall)
                    .size(MaterialTheme.dimens.iconSizeMedium)
                    .clickable(
                        onClick = onIconClick,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ),
        )
    }
}

@Composable
private fun RouteLabel(
    name: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier =
            modifier
                .widthIn(max = MaterialTheme.dimens.labelMaxWidth),
        shape = RoundedCornerShape(MaterialTheme.dimens.cornerRadiusSmall),
        color = LabelBackgroundColor,
        contentColor = Color.White,
        shadowElevation = MaterialTheme.dimens.elevationMedium,
    ) {
        Text(
            text = name,
            modifier =
                Modifier.padding(
                    horizontal = MaterialTheme.dimens.paddingSmall,
                    vertical = MaterialTheme.dimens.paddingExtraSmall,
                ),
            style =
                MaterialTheme.typography.bodySmall.copy(
                    shadow =
                        Shadow(
                            color = Color.Black,
                            offset = Offset(2f, 2f),
                            blurRadius = 4f,
                        ),
                ),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
