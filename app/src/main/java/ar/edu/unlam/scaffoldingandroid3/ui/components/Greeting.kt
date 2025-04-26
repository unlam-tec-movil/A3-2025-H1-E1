package ar.edu.unlam.scaffoldingandroid3.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ar.edu.unlam.scaffoldingandroid3.ui.theme.ScaffoldingAndroid3Theme

@Composable
fun Greeting(
    name: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = "Hello $name!",
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ScaffoldingAndroid3Theme {
        Greeting("Android")
    }
}
