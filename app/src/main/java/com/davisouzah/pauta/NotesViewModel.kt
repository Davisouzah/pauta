package com.davisouzah.pauta

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class SortMode { EDITED, CREATED, TITLE }

data class Settings(
  val theme: ThemeMode = ThemeMode.SYSTEM,
  val dynamicColor: Boolean = true,
  val sort: SortMode = SortMode.EDITED,
)

sealed interface Screen {
  data object Home : Screen
  data class Editor(val id: String) : Screen
}

class NotesViewModel(app: Application) : AndroidViewModel(app) {
  private val store = NoteStore(app)
  private val prefs = app.getSharedPreferences("pauta_prefs", Context.MODE_PRIVATE)
  private var saveJob: Job? = null
  private var generation = 0

  var notes by mutableStateOf(store.load())
    private set
  var screen by mutableStateOf<Screen>(Screen.Home)
    private set
  var showTrash by mutableStateOf(false)
    private set
  var showSettings by mutableStateOf(false)
    private set
  var query by mutableStateOf("")
    private set
  var activeTag by mutableStateOf<String?>(null)
    private set
  var settings by mutableStateOf(loadSettings())
    private set
  var saving by mutableStateOf(false)
    private set

  init {
    val cutoff = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
    val before = notes
    notes = notes.filterNot { it.trashedAt != null && it.trashedAt < cutoff }

    if (!prefs.getBoolean("seeded", false)) {
      prefs.edit().putBoolean("seeded", true).apply()
      if (notes.isEmpty()) notes = listOf(welcomeNote())
    }
    if (notes != before) store.save(notes)
  }

  val visibleNotes: List<Note>
    get() {
      val term = normalize(query.trim())
      val filtered = notes.filter { note ->
        (if (showTrash) note.trashedAt != null else note.trashedAt == null) &&
          (activeTag == null || activeTag in note.tags) &&
          (term.isBlank() || normalize("${note.title} ${note.body}").contains(term))
      }
      val sorted = when (settings.sort) {
        SortMode.EDITED -> filtered.sortedByDescending { it.updatedAt }
        SortMode.CREATED -> filtered.sortedByDescending { it.createdAt }
        SortMode.TITLE -> filtered.sortedBy { normalize(it.displayTitle) }
      }
      return if (showTrash) sorted else sorted.sortedByDescending { it.pinned }
    }

  val allTags: List<Pair<String, Int>>
    get() = notes.asSequence()
      .filter { it.trashedAt == null }
      .flatMap { it.tags.asSequence() }
      .groupingBy { it }
      .eachCount()
      .toList()
      .sortedWith(compareByDescending<Pair<String, Int>> { it.second }.thenBy { it.first })
      .take(16)

  val trashCount: Int
    get() = notes.count { it.trashedAt != null }

  fun open(id: String) {
    screen = Screen.Editor(id)
  }

  fun back() {
    flush()
    screen = Screen.Home
  }

  fun newNote() {
    val now = System.currentTimeMillis()
    val note = Note(UUID.randomUUID().toString(), "", "", now, now)
    notes = listOf(note) + notes
    query = ""
    activeTag = null
    showTrash = false
    flush()
    screen = Screen.Editor(note.id)
  }

  fun update(id: String, title: String? = null, body: String? = null) {
    notes = notes.map { note ->
      if (note.id != id) note
      else note.copy(
        title = title ?: note.title,
        body = body ?: note.body,
        updatedAt = System.currentTimeMillis(),
      )
    }
    scheduleSave()
  }

  fun setPinned(id: String, pinned: Boolean) {
    notes = notes.map { if (it.id == id) it.copy(pinned = pinned) else it }
    flush()
  }

  fun moveToTrash(id: String) {
    notes = notes.map {
      if (it.id == id) it.copy(trashedAt = System.currentTimeMillis(), pinned = false) else it
    }
    if ((screen as? Screen.Editor)?.id == id) screen = Screen.Home
    flush()
  }

  fun restore(id: String) {
    notes = notes.map { if (it.id == id) it.copy(trashedAt = null) else it }
    flush()
  }

  fun deleteForever(id: String) {
    notes = notes.filterNot { it.id == id }
    if (notes.none { it.id == id } && (screen as? Screen.Editor)?.id == id) screen = Screen.Home
    flush()
  }

  fun emptyTrash() {
    notes = notes.filter { it.trashedAt == null }
    flush()
  }

  fun toggleTask(id: String, lineIndex: Int) {
    val note = notes.firstOrNull { it.id == id } ?: return
    val lines = note.body.lines().toMutableList()
    val line = lines.getOrNull(lineIndex) ?: return
    val open = Regex("""\[[ xX]]""")
    lines[lineIndex] = if (Regex("""\[[xX]]""").containsMatchIn(line)) {
      line.replaceFirst(open, "[ ]")
    } else {
      line.replaceFirst(open, "[x]")
    }
    update(id, body = lines.joinToString("\n"))
  }

  fun updateQuery(value: String) {
    query = value
  }

  fun setTag(tag: String?) {
    activeTag = tag
  }

  fun openTrash() {
    showTrash = true
    query = ""
    activeTag = null
  }

  fun closeTrash() {
    showTrash = false
  }

  fun openSettings() {
    showSettings = true
  }

  fun closeSettings() {
    showSettings = false
  }

  fun setTheme(mode: ThemeMode) {
    settings = settings.copy(theme = mode)
    prefs.edit().putString("theme", mode.name).apply()
  }

  fun setDynamicColor(enabled: Boolean) {
    settings = settings.copy(dynamicColor = enabled)
    prefs.edit().putBoolean("dynamic", enabled).apply()
  }

  fun setSort(mode: SortMode) {
    settings = settings.copy(sort = mode)
    prefs.edit().putString("sort", mode.name).apply()
  }

  fun flush() {
    generation++
    saveJob?.cancel()
    saving = false
    store.save(notes)
  }

  override fun onCleared() {
    flush()
    super.onCleared()
  }

  private fun scheduleSave() {
    saving = true
    val current = ++generation
    val snapshot = notes
    saveJob?.cancel()
    saveJob = viewModelScope.launch {
      delay(400)
      withContext(Dispatchers.IO) {
        if (current == generation) store.save(snapshot)
      }
      saving = false
    }
  }

  private fun loadSettings() = Settings(
    theme = runCatching { ThemeMode.valueOf(prefs.getString("theme", "SYSTEM")!!) }
      .getOrDefault(ThemeMode.SYSTEM),
    dynamicColor = prefs.getBoolean("dynamic", true),
    sort = runCatching { SortMode.valueOf(prefs.getString("sort", "EDITED")!!) }
      .getOrDefault(SortMode.EDITED),
  )

  private fun welcomeNote(): Note {
    val now = System.currentTimeMillis()
    return Note(
      id = UUID.randomUUID().toString(),
      title = "Bem-vindo à Pauta",
      body = """
        Seu caderno leve, rápido e 100% offline.

        - [ ] Fixe o que importa com a estrela
        - [ ] Organize com #tags
        - [ ] Toque em Visualizar para ler o Markdown

        ## Markdown sem complicação

        **Negrito**, *itálico*, `código` e listas funcionam de verdade.
        Marque as tarefas na pré-visualização, direto na nota.

        > Menos ruído. Mais ideias.
      """.trimIndent(),
      createdAt = now,
      updatedAt = now,
      pinned = true,
    )
  }
}