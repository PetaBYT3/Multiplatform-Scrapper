package org.scrapper.multiplatform.template

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

data class DropDownItem(
    val imageVector: ImageVector,
    val title: String,
    val onClick: () -> Unit
)
@Composable
fun CustomDropDownMenu(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    dropDownList: List<DropDownItem>
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = modifier
    ) {
        CustomIconButton(
            imageVector = icon,
            onClick = { expanded = true }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            dropDownList.forEach { item ->
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            imageVector = item.imageVector,
                            contentDescription = null
                        )
                    },
                    text = { Text(text = item.title) },
                    onClick = {
                        item.onClick()
                        expanded = false
                    }
                )
            }
        }
    }
}