package ar.edu.unlam.scaffoldingandroid3.ui.shared

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import ar.edu.unlam.scaffoldingandroid3.R
import coil.compose.AsyncImage

@Composable
fun RouteImage(
    modifier: Modifier = Modifier,
    image: String,
) {
    AsyncImage(
        modifier = modifier,
        model = if (image.isNotEmpty()) image else R.drawable.img_default_route,
        contentDescription = "Foto de la ruta",
        placeholder = painterResource(R.drawable.img_default_route),
        error = painterResource(R.drawable.img_default_route),
        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
    )
}
