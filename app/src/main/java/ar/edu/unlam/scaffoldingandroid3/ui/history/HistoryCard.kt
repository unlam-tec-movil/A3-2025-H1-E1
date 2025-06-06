package ar.edu.unlam.scaffoldingandroid3.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Card de las rutas recorridas, se visualizan en el historial de actividad.
 */
@Composable
fun HistoryCard(
    modifier: Modifier = Modifier,
    date: String,
    location: String,
    distance: String,
    duration: String,
    onPlayClick: () -> Unit = {},
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
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = date,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                    )
                    Text(
                        text = location,
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
        }
    }
}
