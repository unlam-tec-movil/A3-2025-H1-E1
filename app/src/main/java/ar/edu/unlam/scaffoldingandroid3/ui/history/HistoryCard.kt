package ar.edu.unlam.scaffoldingandroid3.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import ar.edu.unlam.scaffoldingandroid3.domain.model.History
import ar.edu.unlam.scaffoldingandroid3.ui.shared.RouteImage

/**
 * Card de las rutas recorridas, se visualizan en el historial de actividad.
 */
@Composable
fun HistoryCard(
    modifier: Modifier = Modifier,
    history: History,
    onClickItem: () -> Unit = {},
    onDeleteItem: () -> Unit = {},
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable { onClickItem() },
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
                            .size(76.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.LightGray),
                ) {
                    RouteImage(modifier = Modifier.matchParentSize(), image = history.photoUri)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = history.routeName,
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = history.date,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                    )
                    Row {
                        Text(
                            text = "${String.format("%.0f", history.metrics.currentDistance * 1000)} m",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${history.metrics.currentDuration / 60000} min",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                        )
                    }
                }
            }
        }
    }
}
