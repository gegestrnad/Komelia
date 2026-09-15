package snd.komelia.ui.reader.image.continuous

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.focusable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType.Companion.KeyDown
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import snd.komelia.ui.dialogs.AppDialog

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ContinuousShortcutsDialog(
    keyBindings: ContinuousKeyBindings,
    onKeyBindingsChange: (ContinuousKeyBindings) -> Unit,
    onDismissRequest: () -> Unit,
) {
    var capturingForAction by remember { mutableStateOf<ContinuousShortcutAction?>(null) }

    AppDialog(
        modifier = Modifier.fillMaxWidth(.6f),
        color = MaterialTheme.colorScheme.surfaceVariant,
        onDismissRequest = onDismissRequest,
        content = {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Configure Shortcuts", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Click 'Add key' and press the desired key. Keys already bound to other actions will be reassigned.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Column(
                    modifier = Modifier.weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ContinuousShortcutAction.entries.forEach { action ->
                        ShortcutRow(
                            action = action,
                            keyBindings = keyBindings,
                            isCapturing = capturingForAction == action,
                            onAddKeyClick = { capturingForAction = action },
                            onRemoveKey = { keyCode ->
                                // Keep the (possibly empty) entry so an intentionally
                                // unbound action is not resurrected from defaults on reload.
                                val newBindings = keyBindings.copy(
                                    bindings = keyBindings.bindings.mapValues { (act, keys) ->
                                        if (act == action) keys - keyCode else keys
                                    }
                                )
                                onKeyBindingsChange(newBindings)
                            },
                            onKeyCaptured = { capturedKey ->
                                // Remove this key from any other action first
                                val cleanedBindings = keyBindings.bindings.mapValues { (act, keys) ->
                                    keys.filter { it != capturedKey.keyCode }.toSet()
                                }
                                val newBindings = ContinuousKeyBindings(
                                    bindings = cleanedBindings + (action to (cleanedBindings[action] ?: emptySet()) + capturedKey.keyCode)
                                )
                                onKeyBindingsChange(newBindings)
                                capturingForAction = null
                            },
                            onCaptureCancel = { capturingForAction = null },
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = {
                        onKeyBindingsChange(ContinuousKeyBindings())
                    }) {
                        Text("Reset to defaults")
                    }

                    Button(onClick = onDismissRequest) {
                        Text("Close")
                    }
                }
            }
        }
    )
}

@Composable
private fun ShortcutRow(
    action: ContinuousShortcutAction,
    keyBindings: ContinuousKeyBindings,
    isCapturing: Boolean,
    onAddKeyClick: () -> Unit,
    onRemoveKey: (Long) -> Unit,
    onKeyCaptured: (Key) -> Unit,
    onCaptureCancel: () -> Unit,
) {
    val boundKeys = keyBindings.bindings[action] ?: emptySet()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = actionLabel(action),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(2f)
        ) {
            boundKeys.forEach { keyCode ->
                val keyName = keyNameForKeyCode(keyCode)
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(keyName, style = MaterialTheme.typography.labelMedium)
                        IconButton(
                            onClick = { onRemoveKey(keyCode) },
                            modifier = Modifier.size(16.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove",
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = onAddKeyClick,
            enabled = !isCapturing
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(if (boundKeys.isEmpty()) "Add key" else "Add another")
        }

        if (isCapturing) {
            // Invisible capture box
            BoxWithKeyCapture(
                onKeyCaptured = onKeyCaptured,
                onCancel = onCaptureCancel
            )
        }
    }
}

private fun actionLabel(action: ContinuousShortcutAction): String = when (action) {
    ContinuousShortcutAction.SCROLL_UP -> "Scroll Up"
    ContinuousShortcutAction.SCROLL_DOWN -> "Scroll Down"
    ContinuousShortcutAction.SCROLL_LEFT -> "Scroll Left"
    ContinuousShortcutAction.SCROLL_RIGHT -> "Scroll Right"
    ContinuousShortcutAction.FIRST_PAGE -> "First Page"
    ContinuousShortcutAction.LAST_PAGE -> "Last Page"
    ContinuousShortcutAction.READING_DIRECTION_TOP_TO_BOTTOM -> "Reading Direction: Top to Bottom"
    ContinuousShortcutAction.READING_DIRECTION_LEFT_TO_RIGHT -> "Reading Direction: Left to Right"
    ContinuousShortcutAction.READING_DIRECTION_RIGHT_TO_LEFT -> "Reading Direction: Right to Left"
    ContinuousShortcutAction.ZOOM_IN -> "Zoom In"
    ContinuousShortcutAction.ZOOM_OUT -> "Zoom Out"
    ContinuousShortcutAction.ZOOM_RESET -> "Reset Zoom"
}

@Composable
private fun BoxWithKeyCapture(
    onKeyCaptured: (Key) -> Unit,
    onCancel: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    val captureText = "Press a key... (Esc to cancel)"

    Card(
        modifier = Modifier
            .padding(start = 8.dp)
            .focusRequester(focusRequester)
            .focusable()
            .onPreviewKeyEvent { event ->
                if (event.type == KeyDown) {
                    if (event.key == Key.Escape) {
                        onCancel()
                    } else {
                        onKeyCaptured(event.key)
                    }
                    true
                } else {
                    false
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Text(
            captureText,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

private fun keyNameForKeyCode(keyCode: Long): String {
    // Compose has no public key-label API in this version (Key.keyLabel
    // does not exist), so map the common keys explicitly.
    return when (keyCode) {
        Key.DirectionUp.keyCode -> "Up"
        Key.DirectionDown.keyCode -> "Down"
        Key.DirectionLeft.keyCode -> "Left"
        Key.DirectionRight.keyCode -> "Right"
        Key.PageUp.keyCode -> "Page Up"
        Key.PageDown.keyCode -> "Page Down"
        Key.MoveHome.keyCode -> "Home"
        Key.MoveEnd.keyCode -> "End"
        Key.Spacebar.keyCode -> "Space"
        Key.Enter.keyCode -> "Enter"
        Key.Tab.keyCode -> "Tab"
        Key.Backspace.keyCode -> "Backspace"
        Key.Escape.keyCode -> "Esc"
        Key.Plus.keyCode -> "+"
        Key.Equals.keyCode -> "="
        Key.Minus.keyCode -> "-"
        Key.Zero.keyCode -> "0"
        Key.V.keyCode -> "V"
        Key.L.keyCode -> "L"
        Key.R.keyCode -> "R"
        Key.W.keyCode -> "W"
        Key.A.keyCode -> "A"
        Key.S.keyCode -> "S"
        Key.D.keyCode -> "D"
        else -> "Key $keyCode"
    }
}
