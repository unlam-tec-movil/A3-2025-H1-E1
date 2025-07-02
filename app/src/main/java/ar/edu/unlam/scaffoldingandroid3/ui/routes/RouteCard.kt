package ar.edu.unlam.scaffoldingandroid3.ui.routes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ar.edu.unlam.scaffoldingandroid3.domain.model.Route
import ar.edu.unlam.scaffoldingandroid3.ui.shared.RouteImage

/**
 * TODO implementar la funcionalidad de eliminar una ruta
 * Card de cada ruta guardada
 */

@Composable
fun RouteCard(
    modifier: Modifier = Modifier,
    route: Route,
    onPlayClick: () -> Unit,
    onDeleteItem: () -> Unit,
    routeDisplayCalculator: ar.edu.unlam.scaffoldingandroid3.domain.logic.RouteDisplayCalculator =
        ar.edu.unlam.scaffoldingandroid3.domain.logic.RouteDisplayCalculator(),
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth(),
    ) {
        Row(
            modifier =
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Imagen de la ruta
            Box(
                modifier =
                    Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray),
            ) {
                RouteImage(modifier, route.photoUri)
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Información de la ruta - ocupa el espacio restante
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = route.name,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Row {
                    Text(
                        text = "${String.format("%.0f", routeDisplayCalculator.getDistanceInMeters(route))} m",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = routeDisplayCalculator.formatDuration(routeDisplayCalculator.calculateEstimatedDuration(route)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Botón de play - posición fija
            IconButton(
                onClick = onPlayClick,
                modifier =
                    Modifier
                        .size(52.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape,
                        ),
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                )
            }
        }
    }
}

@Preview
@Composable
fun RouteCardPreview() {
    RouteCard(
        route =
            Route(
                id = "1",
                name = "Ruta de senderismo muy larga por la montaña que puede llegar a ocupar más de dos líneas",
                photoUri = "",
                distance = 10.0,
                duration = 600000,
                points = emptyList(),
            ),
        onPlayClick = {},
        onDeleteItem = {},
    )
}
