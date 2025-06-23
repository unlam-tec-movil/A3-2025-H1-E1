package ar.edu.unlam.scaffoldingandroid3.ui.routes

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Card de cada ruta guardada
 */

@Composable
fun RouteCard(
    modifier: Modifier = Modifier,
    id: String,
    name: String,
    distance: String,
    duration: String,
    onPlayClick: () -> Unit,
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(8.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.LightGray),
                ){
                    //TODO: Cargar imagen de la ruta con Glide
                    Image(
                        imageVector = Icons.Filled.Face,
                        contentDescription = "Play",
                        modifier = Modifier.fillMaxSize())
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Row {
                        Text(
                            text = distance,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = duration,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                        )
                    }
                }
            }
            IconButton(
                onClick = onPlayClick,
                modifier =
                    Modifier
                        .size(64.dp)
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
        id = "1",
        name = "Ruta 1",
        distance = "10 km",
        duration = "2 horas",
        onPlayClick = {},
    )
}
