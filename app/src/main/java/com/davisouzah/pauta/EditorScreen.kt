@file:OptIn(ExperimentalFoundationApi::class)

package com.davisouzah.pauta

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun EditorScreen(vm: NotesViewModel, noteId: String) {
  val note = vm.notes.firstOrNull { it.id == noteId }
  if (note == null) {
    LaunchedEffect(noteId) { vm.back() }
    return
  }

  val colors = MaterialTheme.colorScheme
  val context = LocalContext.current
  val focusManager = LocalFocusManager.current
  val titleFocus = remember { FocusRequester() }
  val bodyFocus = remember { FocusRequester() }

  var title by remember(noteId) { mutableStateOf(TextFieldValue(note.title, TextRange(note.title.length))) }
  var body by remember(noteId) { mutableStateOf(TextFieldValue(note.body, TextRange(note.body.length))) }
  var preview by remember(noteId) { mutableStateOf(false) }
  var menuOpen by remember { mutableStateOf(false) }
  var confirmTrash by remember { mutableStateOf(false) }

  BackHandler { vm.back() }

  LaunchedEffect(Unit) {
    if (note.title.isBlank() && note.body.isBlank()) {
      delay(160)
      titleFocus.requestFocus()
    }
  }

  LaunchedEffect(note.body) {
    if (note.body != body.text) {
      body = TextFieldValue(note.body, TextRange(note.body.length))
    }
  }

  Surface(modifier = Modifier.fillMaxSize(), color = colors.surface) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
        .windowInsetsPadding(
          WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom).union(WindowInsets.ime)
        ),
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .statusBarsPadding()
          .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        IconButton(onClick = vm::back) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
        }
        Column(
          modifier = Modifier.weight(1f),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Text(
            if (vm.saving) "Salvando…" else "Salvo",
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant,
          )
          Text(
            "${note.wordCount} ${if (note.wordCount == 1) "palavra" else "palavras"}",
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant.copy(alpha = 0.7f),
          )
        }
        IconButton(onClick = { vm.setPinned(note.id, !note.pinned) }) {
          Icon(
            if (note.pinned) Icons.Filled.Star else Icons.Outlined.Star,
            contentDescription = if (note.pinned) "Desafixar" else "Fixar",
            tint = if (note.pinned) colors.secondary else colors.onSurfaceVariant,
          )
        }
        IconButton(
          onClick = {
            preview = !preview
            if (preview) focusManager.clearFocus()
          },
        ) {
          Icon(
            if (preview) Icons.Outlined.Edit else Icons.Outlined.List,
            contentDescription = if (preview) "Editar" else "Visualizar",
          )
        }
        Box {
          IconButton(onClick = { menuOpen = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = "Mais opções")
          }
          DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
            DropdownMenuItem(
              text = { Text("Compartilhar") },
              leadingIcon = { Icon(Icons.Outlined.Share, contentDescription = null) },
              onClick = {
                menuOpen = false
                shareNote(context, note)
              },
            )
            DropdownMenuItem(
              text = { Text("Copiar texto") },
              leadingIcon = { Icon(Icons.Outlined.Create, contentDescription = null) },
              onClick = {
                menuOpen = false
                copyNote(context, note)
              },
            )
            HorizontalDivider(Modifier.padding(vertical = 6.dp))
            DropdownMenuItem(
              text = { Text("Mover para a lixeira", color = colors.error) },
              leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = colors.error) },
              onClick = {
                menuOpen = false
                confirmTrash = true
              },
            )
          }
        }
      }

      if (preview) {
        Column(
          modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        ) {
          if (note.title.isNotBlank()) {
            Text(
              note.title,
              style = MaterialTheme.typography.headlineMedium,
              color = colors.onSurface,
            )
            Spacer(Modifier.height(10.dp))
          }
          if (note.body.isBlank()) {
            Text(
              "Nada para visualizar ainda.",
              style = MaterialTheme.typography.bodyLarge,
              color = colors.onSurfaceVariant,
            )
          } else {
            MarkdownBody(
              text = note.body,
              onToggleTask = { vm.toggleTask(note.id, it) },
            )
          }
          Spacer(Modifier.height(40.dp))
        }
      } else {
        Column(
          modifier = Modifier
            .weight(1f)
            .padding(horizontal = 20.dp),
        ) {
          BasicTextField(
            value = title,
            onValueChange = {
              title = it
              vm.update(note.id, title = it.text)
            },
            singleLine = true,
            textStyle = MaterialTheme.typography.headlineMedium.copy(color = colors.onSurface),
            cursorBrush = SolidColor(colors.primary),
            modifier = Modifier
              .fillMaxWidth()
              .focusRequester(titleFocus),
            decorationBox = { inner ->
              Box {
                if (title.text.isEmpty()) {
                  Text(
                    "Título",
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.onSurfaceVariant.copy(alpha = 0.4f),
                  )
                }
                inner()
              }
            },
          )

          if (note.tags.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(
              note.tags.joinToString("   ") { "#$it" },
              style = MaterialTheme.typography.labelMedium,
              color = colors.secondary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }

          Spacer(Modifier.height(10.dp))

          BasicTextField(
            value = body,
            onValueChange = {
              body = it
              vm.update(note.id, body = it.text)
            },
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.onSurface),
            cursorBrush = SolidColor(colors.primary),
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f)
              .focusRequester(bodyFocus),
            decorationBox = { inner ->
              Box {
                if (body.text.isEmpty()) {
                  Text(
                    "Comece a escrever…",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.onSurfaceVariant.copy(alpha = 0.45f),
                  )
                }
                inner()
              }
            },
          )
        }

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
          horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
        ) {
          FormatChip("Título") { body = applyFormat(body, MdAction.Heading, vm, note.id) }
          FormatChip("Lista") { body = applyFormat(body, MdAction.Bullet, vm, note.id) }
          FormatChip("Tarefa") { body = applyFormat(body, MdAction.Task, vm, note.id) }
          FormatChip("Negrito") { body = applyFormat(body, MdAction.Bold, vm, note.id) }
          FormatChip("Itálico") { body = applyFormat(body, MdAction.Italic, vm, note.id) }
          FormatChip("Código") { body = applyFormat(body, MdAction.Code, vm, note.id) }
          FormatChip("Divisor") { body = applyFormat(body, MdAction.Divider, vm, note.id) }
        }
      }
    }
  }

  if (confirmTrash) {
    AlertDialog(
      onDismissRequest = { confirmTrash = false },
      title = { Text("Mover para a lixeira?") },
      text = { Text("A nota fica na lixeira por 30 dias antes de ser excluída.") },
      confirmButton = {
        TextButton(
          onClick = {
            confirmTrash = false
            vm.moveToTrash(note.id)
            Toast.makeText(context, "Nota movida para a lixeira", Toast.LENGTH_SHORT).show()
          },
        ) { Text("Mover", color = colors.error) }
      },
      dismissButton = {
        TextButton(onClick = { confirmTrash = false }) { Text("Cancelar") }
      },
    )
  }
}

private enum class MdAction { Heading, Bullet, Task, Bold, Italic, Code, Divider }

private fun applyFormat(
  value: TextFieldValue,
  action: MdAction,
  vm: NotesViewModel,
  noteId: String,
): TextFieldValue {
  val text = value.text
  val start = value.selection.min
  val end = value.selection.max

  fun wrap(open: String, close: String): TextFieldValue {
    val selected = text.substring(start, end)
    val newText = text.substring(0, start) + open + selected + close + text.substring(end)
    val cursor = if (selected.isEmpty()) start + open.length else end + open.length + close.length
    return TextFieldValue(newText, TextRange(cursor))
  }

  fun prefix(prefix: String): TextFieldValue {
    val lineStart = if (start == 0) 0 else text.lastIndexOf('\n', start - 1).let {
      if (it == -1) 0 else it + 1
    }
    val result = if (text.startsWith(prefix, lineStart)) {
      val removed = text.removeRange(lineStart, lineStart + prefix.length)
      TextFieldValue(removed, TextRange((start - prefix.length).coerceAtLeast(lineStart)))
    } else {
      val inserted = text.substring(0, lineStart) + prefix + text.substring(lineStart)
      TextFieldValue(inserted, TextRange(start + prefix.length))
    }
    return result
  }

  val result = when (action) {
    MdAction.Heading -> prefix("## ")
    MdAction.Bullet -> prefix("- ")
    MdAction.Task -> prefix("- [ ] ")
    MdAction.Bold -> wrap("**", "**")
    MdAction.Italic -> wrap("*", "*")
    MdAction.Code -> wrap("`", "`")
    MdAction.Divider -> {
      val insert = "\n---\n"
      TextFieldValue(
        text.substring(0, start) + insert + text.substring(end),
        TextRange(start + insert.length),
      )
    }
  }
  vm.update(noteId, body = result.text)
  return result
}

@Composable
private fun FormatChip(label: String, onClick: () -> Unit) {
  Surface(
    onClick = onClick,
    shape = CircleShape,
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
  ) {
    Text(
      label,
      style = MaterialTheme.typography.labelMedium,
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
    )
  }
}