package org.scrapper.multiplatform.template

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun CustomFilledButton(
    title: String,
    onClick: () -> Unit
) {
    Button(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = { onClick.invoke() }
    ) {
        Text(text = title)
    }
}

@Composable
fun CustomIconButton(
    imageVector: ImageVector,
    onClick: () -> Unit
) {
    IconButton(
        onClick = { onClick.invoke() }
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = null
        )
    }
}