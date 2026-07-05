package de.avanzu.woolstash.ui.inventory

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.avanzu.woolstash.R

@Composable
internal fun DeleteItemsConfirmationDialog(
    itemCount: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (itemCount == 1) {
                    stringResource(R.string.delete_item_dialog_title)
                } else {
                    stringResource(R.string.delete_items_dialog_title)
                },
            )
        },
        text = {
            Text(
                text = if (itemCount == 1) {
                    stringResource(R.string.delete_item_dialog_message)
                } else {
                    stringResource(
                        R.string.delete_items_dialog_message,
                        itemCount,
                    )
                },
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
            ) {
                Text(
                    text = stringResource(R.string.action_delete),
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
            ) {
                Text(
                    text = stringResource(R.string.action_cancel),
                )
            }
        },
    )
}
