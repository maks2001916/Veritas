package com.example.Veritas.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.ui.tooling.preview.Preview
import com.example.Veritas.ui.theme.BabylonGold

@Composable
fun AddressChip(
    address: String,
    modifier: Modifier = Modifier,
    onCopy: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    AssistChip(
        onClick = {
            clipboardManager.setPrimaryClip(ClipData.newPlainText("address", address))
            onCopy?.invoke()
        },
        modifier = modifier,
        label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = address,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = "Копировать адрес",
                    modifier = Modifier.width(14.dp),
                    tint = BabylonGold
                )
            }
        },
        leadingIcon = {
            Icon(
                Icons.Default.DeleteOutline,
                contentDescription = null,
                modifier = Modifier.width(16.dp)
            )
        }
    )
}

@Preview(showBackground = true)
@Composable
fun AddressChipPreview() {
    AddressChip(
        address = "adres",
        modifier = Modifier,
        onCopy = {}
    )
}