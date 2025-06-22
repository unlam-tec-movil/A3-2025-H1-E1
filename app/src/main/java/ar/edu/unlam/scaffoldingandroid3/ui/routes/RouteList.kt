package ar.edu.unlam.scaffoldingandroid3.ui.routes

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import ar.edu.unlam.scaffoldingandroid3.domain.model.Route

/**
 * TODO
 * Este routeList tendriamos que refactorizarlo para que reciba por parametro un
 * data class History que tenga toda la info para pasarle al HistoryCard y un onPlayClick Dinamico,
 * para que el composable no sea el que decida a donde va, si no, que sea la screen cuando lo
 * llame el que decida que info va y a donde redirige.
 * Para esta refactorizacion necesitamos crear un objeto de dominio, implementar la base de datos,
 * implementar un viewModel que se conecte con la screen, llamar con un mapper a los objetos de
 * la base de datos y pasarlos a dominio para que despues el view model se los pueda pasar a la screen
 */
@Composable
fun RouteList(
    list: List<Route>,
    onPlayClick: (String) -> Unit,
) {
    LazyColumn {
        items(list) { route ->
            RouteCard(
                id = route.id,
                name = route.name,
                distance = route.distance.toString(),
                duration = route.duration.toString(),
                onPlayClick = { onPlayClick(route.id) },
            )
        }
    }
}
