package com.seamoon5.notevault.ui

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.seamoon5.notevault.data.Folder
import com.seamoon5.notevault.data.Note
import com.seamoon5.notevault.ui.theme.DarkNotePalette
import com.seamoon5.notevault.ui.theme.LocalIsDark
import com.seamoon5.notevault.ui.theme.LightNotePalette
import com.seamoon5.notevault.ui.theme.NotePalette
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun notePalette(colorIndex: Int, dark: Boolean = LocalIsDark.current): NotePalette {
    val list = if (dark) DarkNotePalette else LightNotePalette
    return list.getOrElse(colorIndex) { list[0] }
}

/** "Today 14:23", "Yesterday 09:02", "12 Sep 2026". */
private val MONTHS = arrayOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
)

private fun dayAndMonth(ts: Long): String {
    val c = Calendar.getInstance().apply { timeInMillis = ts }
    return "${c.get(Calendar.DAY_OF_MONTH)} ${MONTHS[c.get(Calendar.MONTH)]}"
}

private fun dayMonthYear(ts: Long): String {
    val c = Calendar.getInstance().apply { timeInMillis = ts }
    return "${c.get(Calendar.DAY_OF_MONTH)} ${MONTHS[c.get(Calendar.MONTH)]} ${c.get(Calendar.YEAR)}"
}

fun formatDateTime(ts: Long): String {
    val now = Calendar.getInstance()
    val then = Calendar.getInstance().apply { timeInMillis = ts }
    val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(ts))
    val sameYear = now.get(Calendar.YEAR) == then.get(Calendar.YEAR)
    val dayDiff = daysBetween(then, now)
    return when {
        dayDiff == 0 -> "Today $time"
        dayDiff == 1 -> "Yesterday $time"
        sameYear -> dayAndMonth(ts)
        else -> dayMonthYear(ts)
    }
}

fun formatDate(ts: Long): String {
    val time = SimpleDateFormat("HH:mm", Locale.US).format(Date(ts))
    return "${dayMonthYear(ts)}, $time"
}

private fun daysBetween(from: Calendar, to: Calendar): Int {
    val a = from.clone() as Calendar
    val b = to.clone() as Calendar
    listOf(a, b).forEach {
        it.set(Calendar.HOUR_OF_DAY, 0)
        it.set(Calendar.MINUTE, 0)
        it.set(Calendar.SECOND, 0)
        it.set(Calendar.MILLISECOND, 0)
    }
    val diff = b.timeInMillis - a.timeInMillis
    return Math.round(diff / 86_400_000.0).toInt()
}

/** First non-empty line of a note body, for the card preview. */
fun previewOf(body: String, max: Int = 90): String {
    val line = body.lineSequence().firstOrNull { it.isNotBlank() }?.trim().orEmpty()
    val flat = line.replace(Regex("\\s+"), " ")
    return if (flat.length <= max) flat else flat.take(max).trimEnd() + "..."
}

fun isBlankNote(title: String, body: String): Boolean =
    title.isBlank() && body.isBlank()

fun safeFileName(raw: String): String {
    val cleaned = raw.trim().replace(Regex("[^A-Za-z0-9 _-]"), "").trim()
    val name = if (cleaned.isEmpty()) "note" else cleaned.take(40)
    return name.replace(Regex("\\s+"), "_")
}

/**
 * One editor shape used for both normal notes and vault notes, so the editor
 * code is written (and fixed) only once.
 */
data class EditorState(
    val id: Long = 0L,
    val title: String = "",
    val body: String = "",
    val tags: String = "",
    val colorIndex: Int = 0,
    val folderId: Long? = null,
    val pinned: Boolean = false,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

// ---- grouping ----------------------------------------------------------

/** Buckets used for the section headers on the notes screen. */
enum class NoteGroup(val label: String) {
    PINNED("Pinned"),
    TODAY("Today"),
    YESTERDAY("Yesterday"),
    THIS_WEEK("Earlier this week"),
    EARLIER("Earlier")
}

fun groupOf(note: Note): NoteGroup {
    if (note.pinned) return NoteGroup.PINNED
    val now = Calendar.getInstance()
    val then = Calendar.getInstance().apply { timeInMillis = note.updatedAt }
    val startOfToday = (now.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val days = Math.round((startOfToday.timeInMillis - startOfDay(then).timeInMillis) / 86_400_000.0)
        .toInt()
    return when {
        days <= 0 -> NoteGroup.TODAY
        days == 1 -> NoteGroup.YESTERDAY
        days < 7 -> NoteGroup.THIS_WEEK
        else -> NoteGroup.EARLIER
    }
}

private fun startOfDay(c: Calendar): Calendar = (c.clone() as Calendar).apply {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}

fun groupNotes(notes: List<Note>): List<Pair<NoteGroup, List<Note>>> =
    NoteGroup.entries.mapNotNull { g ->
        val inGroup = notes.filter { groupOf(it) == g }
        if (inGroup.isEmpty()) null else g to inGroup
    }

/** "Good morning" style header, which makes the app feel alive rather than static. */
fun greeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 5 -> "Still up?"
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        hour < 22 -> "Good evening"
        else -> "Winding down"
    }
}

fun fileStamp(): String =
    SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())

// ---- export ------------------------------------------------------------

fun buildMarkdown(notes: List<Note>, folders: List<Folder>): String {
    val folderNames = folders.associate { it.id to it.name }
    val sb = StringBuilder()
    sb.append("# NoteVault export\n\n")
    sb.append("_${formatDate(System.currentTimeMillis())} - ${notes.size} notes_\n\n")
    notes.forEach { n ->
        sb.append("## ")
        sb.append(if (n.title.isBlank()) "Untitled note" else n.title)
        sb.append("\n")
        sb.append("*")
        sb.append(formatDate(n.updatedAt))
        folderNames[n.folderId]?.let { sb.append(" - Folder: $it") }
        if (n.tags.isNotBlank()) sb.append(" - Tags: ${n.tags}")
        if (n.pinned) sb.append(" - Pinned")
        sb.append("*\n\n")
        if (n.body.isNotBlank()) sb.append(n.body.trim()).append("\n\n")
        sb.append("---\n\n")
    }
    return sb.toString()
}

fun buildPlainText(notes: List<Note>, folders: List<Folder>): String {
    val folderNames = folders.associate { it.id to it.name }
    val sb = StringBuilder()
    notes.forEach { n ->
        sb.append(if (n.title.isBlank()) "Untitled note" else n.title).append('\n')
        sb.append("=".repeat(30)).append('\n')
        folderNames[n.folderId]?.let { sb.append("Folder: $it\n") }
        if (n.tags.isNotBlank()) sb.append("Tags: ${n.tags}\n")
        sb.append(formatDate(n.updatedAt)).append("\n\n")
        sb.append(n.body.trim()).append("\n\n")
        sb.append("-".repeat(30)).append("\n\n")
    }
    return sb.toString()
}

fun writeToUri(context: Context, uri: Uri, text: String): Boolean = try {
    context.contentResolver.openOutputStream(uri)?.use { out ->
        out.write(text.toByteArray(Charsets.UTF_8))
        out.flush()
    } != null
} catch (e: Exception) {
    false
}

// ---- small shared composables -----------------------------------------

@Composable
fun TagChips(tags: String, max: Int = 4) {
    val list = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    if (list.isEmpty()) return
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        list.take(max).forEach { tag ->
            Text(
                text = "#$tag",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        if (list.size > max) {
            Text(
                text = "+${list.size - max}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
