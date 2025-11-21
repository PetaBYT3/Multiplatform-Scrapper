package org.scrapper.multiplatform.template

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip

@Composable
fun CustomRadioButton(
    itemList: List<String>,
    selectedValue: String,
    onItemSelected: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        itemList.forEach {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = it == selectedValue,
                        onClick = {
                            onItemSelected(it)
                        }
                    )
                    .clip(RoundedCornerShape(10)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedValue == it,
                    onClick = { onItemSelected(it) }
                )
                HorizontalSpacer(10)
                CustomTextContent(text = it)
            }
        }
    }
}
