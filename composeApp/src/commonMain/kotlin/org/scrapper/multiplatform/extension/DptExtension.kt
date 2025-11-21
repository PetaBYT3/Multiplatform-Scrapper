package org.scrapper.multiplatform.extension

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.scrapper.multiplatform.action.DptAction
import org.scrapper.multiplatform.state.DptState

@Composable
expect fun DptWebPageExtension(
    modifier: Modifier = Modifier,
    state: DptState,
    onAction: (DptAction) -> Unit
)