package snd.komelia.ui.reader.image.continuous

import androidx.compose.ui.input.key.Key
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class ContinuousKeyBindings(
    val bindings: Map<ContinuousShortcutAction, Set<Long>> = defaultBindings()
) {
    private val keyToAction: Map<Long, ContinuousShortcutAction> by lazy {
        bindings.flatMap { (action, keys) -> keys.map { it to action } }.toMap()
    }

    fun actionFor(key: Key): ContinuousShortcutAction? = keyToAction[key.keyCode]

    companion object {
        fun defaultBindings(): Map<ContinuousShortcutAction, Set<Long>> = mapOf(
            ContinuousShortcutAction.SCROLL_UP to setOf(Key.DirectionUp.keyCode),
            ContinuousShortcutAction.SCROLL_DOWN to setOf(Key.DirectionDown.keyCode),
            ContinuousShortcutAction.SCROLL_LEFT to setOf(Key.DirectionLeft.keyCode),
            ContinuousShortcutAction.SCROLL_RIGHT to setOf(Key.DirectionRight.keyCode),
            ContinuousShortcutAction.FIRST_PAGE to setOf(Key.MoveHome.keyCode),
            ContinuousShortcutAction.LAST_PAGE to setOf(Key.MoveEnd.keyCode),
            ContinuousShortcutAction.READING_DIRECTION_TOP_TO_BOTTOM to setOf(Key.V.keyCode),
            ContinuousShortcutAction.READING_DIRECTION_LEFT_TO_RIGHT to setOf(Key.L.keyCode),
            ContinuousShortcutAction.READING_DIRECTION_RIGHT_TO_LEFT to setOf(Key.R.keyCode),
            ContinuousShortcutAction.ZOOM_IN to setOf(Key.Plus.keyCode, Key.Equals.keyCode),
            ContinuousShortcutAction.ZOOM_OUT to setOf(Key.Minus.keyCode),
            ContinuousShortcutAction.ZOOM_RESET to setOf(Key.Zero.keyCode),
        )
    }
}

fun ContinuousKeyBindings.toJson(): String = Json.encodeToString(this)

fun fromJsonToJson(json: String): ContinuousKeyBindings = try {
    val decoded = Json.decodeFromString<ContinuousKeyBindings>(json)
    // Merge with defaults so bindings persisted before new actions existed
    // still get sensible defaults for the missing actions.
    ContinuousKeyBindings(bindings = ContinuousKeyBindings.defaultBindings() + decoded.bindings)
} catch (e: Exception) {
    ContinuousKeyBindings()
}
