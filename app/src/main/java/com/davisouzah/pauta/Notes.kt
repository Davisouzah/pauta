package com.davisouzah.pauta

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.Normalizer
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class Note(
  val id: String,
  val title: String,
  val body: String,
  val createdAt: Long,
  val updatedAt: Long,
  val pinned: Boolean = false,
  val trashedAt: Long? = null,
)

val PT_BR: Locale = Locale.forLanguageTag("pt-BR")

private val TAG_REGEX = Regex("""(?<![\p{L}\p{N}_])#([\p{L}\p{N}_-]{1,32})""")
private val SPACES = Regex("""\s+""")
private val MARKERS = Regex("""[*_`#>~]""")
private val PREFIX = Regex("""^\s*[-+]\s*(\[[ xX]]\s*)?""", RegexOption.MULTILINE)

val Note.tags: List<String>
  get() = TAG_REGEX.findAll("$title\n$body")
    .map { it.groupValues[1].lowercase(PT_BR) }
    .distinct()
    .toList()

val Note.displayTitle: String
  get() = title.trim().ifBlank {
    body.lineSequence().firstOrNull { it.isNotBlank() }?.let { plainText(it).take(52) } ?: "Sem título"
  }

val Note.snippet: String
  get() = plainText(body.replace(TAG_REGEX, ""))

val Note.wordCount: Int
  get() = body.trim().split(SPACES).count { it.isNotBlank() }

fun plainText(value: String): String =
  value.replace(PREFIX, "")
    .replace(MARKERS, "")
    .replace(SPACES, " ")
    .trim()

fun normalize(value: String): String =
  Normalizer.normalize(value, Normalizer.Form.NFD)
    .replace(Regex("""\p{Mn}+"""), "")
    .lowercase(PT_BR)

fun formatNoteDate(timestamp: Long): String {
  val zone = ZoneId.systemDefault()
  val moment = Instant.ofEpochMilli(timestamp).atZone(zone)
  val date = moment.toLocalDate()
  val today = LocalDate.now(zone)
  val time = moment.format(DateTimeFormatter.ofPattern("HH:mm", PT_BR))
  return when {
    date == today -> "Hoje, $time"
    date == today.minusDays(1) -> "Ontem, $time"
    date.year == today.year -> date.format(DateTimeFormatter.ofPattern("d 'de' MMM", PT_BR))
    else -> date.format(DateTimeFormatter.ofPattern("d 'de' MMM yyyy", PT_BR))
  }
}

fun todayLabel(): String =
  LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", PT_BR))
    .replaceFirstChar { it.titlecase(PT_BR) }

class NoteStore(context: Context) {
  private val file = File(context.filesDir, "notes.json")

  @Synchronized
  fun load(): List<Note> = runCatching {
    if (!file.exists()) return emptyList()
    val array = JSONArray(file.readText())
    buildList {
      for (i in 0 until array.length()) {
        val item = array.getJSONObject(i)
        add(
          Note(
            id = item.getString("id"),
            title = item.optString("title"),
            body = item.optString("body"),
            createdAt = item.optLong("createdAt"),
            updatedAt = item.optLong("updatedAt"),
            pinned = item.optBoolean("pinned"),
            trashedAt = if (item.isNull("trashedAt")) null else item.optLong("trashedAt"),
          )
        )
      }
    }
  }.getOrDefault(emptyList())

  @Synchronized
  fun save(notes: List<Note>) {
    val array = JSONArray()
    notes.forEach { note ->
      array.put(
        JSONObject().apply {
          put("id", note.id)
          put("title", note.title)
          put("body", note.body)
          put("createdAt", note.createdAt)
          put("updatedAt", note.updatedAt)
          put("pinned", note.pinned)
          note.trashedAt?.let { put("trashedAt", it) }
        }
      )
    }
    val temp = File(file.parentFile, "notes.json.tmp")
    runCatching {
      temp.writeText(array.toString())
      if (!temp.renameTo(file)) {
        file.writeText(array.toString())
        temp.delete()
      }
    }
  }
}