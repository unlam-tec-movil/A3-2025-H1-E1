package ar.edu.unlam.scaffoldingandroid3.ui.history

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ar.edu.unlam.scaffoldingandroid3.domain.model.History
import androidx.compose.ui.draw.clip

/**
 * Ítem de historial con soporte de gesto swipe-to-delete (tipo Gmail).
 */

@Composable
fun DismissibleHistoryItem(
    history: History,
    onItemClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dismissState: SwipeToDismissBoxState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.StartToEnd || value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        },
        positionalThreshold = { totalDistance: Float -> totalDistance * 0.5f },
    )

    val progress by animateFloatAsState(targetValue = dismissState.progress)

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier.padding(vertical = 4.dp, horizontal = 8.dp),
        enableDismissFromStartToEnd = true,
        backgroundContent = {
            val eased = progress.coerceIn(0f, 1f)
            val scale = 0.9f + (0.7f * kotlin.math.sqrt(eased))
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Red)
                        .padding(start = 24.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.scale(scale).padding(4.dp),
                )
            }
        },
        content = {
            HistoryCard(
                history = history,
                onClickItem = onItemClick,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    )
}
