package de.avanzu.woolstash.ui.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.avanzu.woolstash.R

@Composable
internal fun InventoryEditorActions(
    canSubmit: Boolean,
    submitLabel: String,
    onSubmit: () -> Unit,
    onCancel: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (onCancel != null) {
            TextButton(
                modifier = Modifier.weight(1f),
                onClick = onCancel,
            ) {
                Text(stringResource(R.string.action_cancel))
            }
        }

        Button(
            modifier = Modifier.weight(1f),
            enabled = canSubmit,
            onClick = onSubmit,
        ) {
            Text(submitLabel)
        }
    }
}
