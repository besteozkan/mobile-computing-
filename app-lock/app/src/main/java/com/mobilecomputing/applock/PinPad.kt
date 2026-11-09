package com.mobilecomputing.applock

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val KEY_EXTRA = "extra"
private const val KEY_DELETE = "⌫"

/**
 * PIN girişi için nokta göstergesi + sayısal tuş takımı.
 * [PIN_LENGTH] hane girilince [onComplete] çağrılır ve giriş temizlenir.
 * [extraKey] sol alttaki boş tuşa bir işlev koyar (ör. parmak izi).
 */
@Composable
fun PinPad(
    onComplete: (String) -> Unit,
    enabled: Boolean = true,
    extraKey: Pair<String, () -> Unit>? = null,
) {
    var entered by remember { mutableStateOf("") }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(PIN_LENGTH) { i ->
                Box(
                    Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(
                            if (i < entered.length) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant,
                        ),
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        val rows = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf(KEY_EXTRA, "0", KEY_DELETE),
        )
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                row.forEach { key ->
                    when {
                        key == KEY_EXTRA && extraKey == null -> Spacer(Modifier.size(72.dp))
                        key == KEY_EXTRA -> PadKey(extraKey!!.first, enabled) { extraKey.second() }
                        key == KEY_DELETE -> PadKey(key, enabled && entered.isNotEmpty()) {
                            entered = entered.dropLast(1)
                        }
                        else -> PadKey(key, enabled) {
                            if (entered.length < PIN_LENGTH) {
                                entered += key
                                if (entered.length == PIN_LENGTH) {
                                    val pin = entered
                                    entered = ""
                                    onComplete(pin)
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PadKey(label: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(enabled = enabled, onClick = onClick)
            .alpha(if (enabled) 1f else 0.4f),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontSize = 26.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
