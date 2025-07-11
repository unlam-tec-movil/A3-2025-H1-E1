package ar.edu.unlam.scaffoldingandroid3.ui.routes

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ar.edu.unlam.scaffoldingandroid3.domain.model.Route

@Composable
fun RouteList(
    routeList: List<Route>,
    onPlayClick: (Route) -> Unit,
    onDeleteItem: (String) -> Unit,
) {
    LazyColumn {
        items(routeList, key = { it.id }) { route ->
            DismissibleRouteItem(
                route = route,
                onPlayClick = { onPlayClick(route) },
                onDelete = { onDeleteItem(route.id) },
            )
        }
    }
}

@Preview
@Composable
fun RouteListPreview() {
    val list =
        listOf(
            Route("1", "Ruta 1", emptyList(), 10.0, 6),
            Route("2", "Ruta 2", emptyList(), 20.0, 12),
            Route("3", "Ruta 3", emptyList(), 30.0, 18),
        )
    RouteList(routeList = list, onPlayClick = {}, onDeleteItem = {})
}
