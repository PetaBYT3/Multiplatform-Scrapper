package org.scrapper.multiplatform.template

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun CustomTextLarge(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.displayLarge
    )
}

@Composable
fun CustomTextMedium(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.displayMedium
    )
}

@Composable
fun CustomTextTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge
    )
}

@Composable
fun CustomTextContent(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
fun CustomTextHint(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}