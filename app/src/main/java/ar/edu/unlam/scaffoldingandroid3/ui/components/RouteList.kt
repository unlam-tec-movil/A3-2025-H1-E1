package ar.edu.unlam.scaffoldingandroid3.ui.components

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview(showBackground = true)
@Composable
fun RouteList() {
    LazyColumn {
        items(5) { index ->
            RouteCard(
                onPlayClick = {},
            )
        }
    }
}
