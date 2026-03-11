package com.example.Veritas.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoadingIndicator(
    message: String = "Загрузка...",
    showProgress: Boolean = true,
    progress: Pair<Int, Int>? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (showProgress) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        progress?.let { (current, total) ->
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { current.toFloat() / total },
                modifier = Modifier.width(200.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$current/$total попыток",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoadIndicatorPreview() {
    LoadingIndicator(
        message = "Сообщение",
        showProgress = true,
        progress = Pair(0,10),
        modifier = Modifier
    )
}