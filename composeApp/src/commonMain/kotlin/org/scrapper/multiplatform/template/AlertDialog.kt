package org.scrapper.multiplatform.template

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomAlertDialogMessage(
   title: String,
   message: String,
   onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onConfirm.invoke() },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(15.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )
                VerticalSpacer(15)
                CustomTextContent(text = message)
                VerticalSpacer(15)
                Row() {
                    Spacer(Modifier.weight(1f))
                    Button(
                        onClick = {
                            onConfirm.invoke()
                        }
                    ) {
                        Text(text = "Ok")
                    }
                }
            }
        }
    )
}