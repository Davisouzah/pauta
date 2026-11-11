@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package com.davisouzah.pauta

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(vm: NotesViewModel) {
  val notes = vm.visibleNotes
  val tags = vm.allTags
  val context = LocalContext.current
  val haptics = LocalHapticFeedback.current
  var sheetNote by remember { mutableStateOf<Note?>(null) }
  var confirmDelete by remember { mutableStateOf<Note?>(null) }
  var confirmEmptyTrash by remember { mutableStateOf(false) }

  BackHandler(enabled = vm.showTrash) { vm.closeTrash() }

  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    contentWindowInsets = WindowInsets.safeDrawing,
    floatingActionButton = {
      if (!vm.showTrash) {
        ExtendedFloatingActionButton(
          onClick = vm::newNote,
          icon = { Icon(Icons.Default.Add, contentDescription = null) },
          text = { Text("Nova nota") },
          shape = RoundedCornerShape(20.dp),
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary,
        )
      }
    },
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding),
      contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 110.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      item(key = "header") {
        HomeHeader(
          vm = vm,
          count = notes.size,
          onEmptyTrash = { confirmEmptyTrash = true },
        )
      }

      item(key = "search") {
        SearchField(query = vm.query, onQuery = vm::updateQuery)
      }

      if (tags.isNotEmpty() && !vm.showTrash) {
        item(key = "tags") {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Pill(
              text = "Todas",
              selected = vm.activeTag == null,
              onClick = { vm.setTag(null) },
            )
            tags.forEach { (tag, count) ->
              Pill(
                text = "#$tag · $count",
                selected = vm.activeTag == tag,
                onClick = { vm.setTag(if (vm.activeTag == tag) null else tag) },
              )
            }
          }
        }
      }

      if (vm.showTrash && notes.isNotEmpty()) {
        item(key = "trash-note") {
          Text(
            "Itens na lixeira são removidos automaticamente após 30 dias.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
          )
        }
      }

      if (notes.isEmpty()) {
        item(key = "empty") {
          EmptyState(
            trash = vm.showTrash,
            searching = vm.query.isNotBlank() || vm.activeTag != null,
            tag = vm.activeTag,
            onCreate = vm::newNote,
          )
        }
      } else {
        val pinned = notes.filter { it.pinned }
        val others = notes.filterNot { it.pinned }

        if (pinned.isNotEmpty()) {
          item(key = "pinned-label") { SectionLabel("Fixadas") }
          items(pinned, key = { it.id }) { note ->
            NoteCard(
              note = note,
              onClick = { vm.open(note.id) },
              onLongClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                sheetNote = note
              },
            )
          }
        }
        if (others.isNotEmpty()) {
          if (pinned.isNotEmpty()) item(key = "others-label") { SectionLabel("Outras") }
          items(others, key = { it.id }) { note ->
            NoteCard(
              note = note,
              onClick = { vm.open(note.id) },
              onLongClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                sheetNote = note
              },
            )
          }
        }
      }
    }
  }

  sheetNote?.let { note ->
    ModalBottomSheet(
      onDismissRequest = { sheetNote = null },
      containerColor = MaterialTheme.colorScheme.surface,
      dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
      Column(
        Modifier
          .windowInsetsPadding(WindowInsets.navigationBars)
          .padding(start = 20.dp, end = 20.dp, bottom = 16.dp),
      ) {
        Text(
          note.displayTitle,
          style = MaterialTheme.typography.titleLarge,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(4.dp))
        Text(
          if (note.trashedAt != null) "Na lixeira" else formatNoteDate(note.updatedAt),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))

        if (note.trashedAt == null) {
          SheetAction(
            icon = if (note.pinned) Icons.Filled.Star else Icons.Outlined.Star,
            label = if (note.pinned) "Desafixar" else "Fixar",
          ) {
            vm.setPinned(note.id, !note.pinned)
            sheetNote = null
          }
          SheetAction(Icons.Outlined.Share, "Compartilhar") {
            shareNote(context, note)
            sheetNote = null
          }
          SheetAction(Icons.Outlined.Edit, "Copiar texto") {
            copyNote(context, note)
            sheetNote = null
          }
          Spacer(Modifier.height(6.dp))
          SheetAction(Icons.Outlined.Delete, "Mover para a lixeira", danger = true) {
            vm.moveToTrash(note.id)
            sheetNote = null
            Toast.makeText(context, "Nota movida para a lixeira", Toast.LENGTH_SHORT).show()
          }
        } else {
          SheetAction(Icons.Outlined.Refresh, "Restaurar") {
            vm.restore(note.id)
            sheetNote = null
          }
          SheetAction(Icons.Outlined.Delete, "Excluir para sempre", danger = true) {
            confirmDelete = note
            sheetNote = null
          }
        }
      }
    }
  }

  confirmDelete?.let { note ->
    AlertDialog(
      onDismissRequest = { confirmDelete = null },
      title = { Text("Excluir para sempre?") },
      text = { Text("“${note.displayTitle}” não poderá ser recuperada.") },
      confirmButton = {
        TextButton(
          onClick = {
            vm.deleteForever(note.id)
            confirmDelete = null
          },
        ) { Text("Excluir", color = MaterialTheme.colorScheme.error) }
      },
      dismissButton = {
        TextButton(onClick = { confirmDelete = null }) { Text("Cancelar") }
      },
    )
  }

  if (confirmEmptyTrash) {
    AlertDialog(
      onDismissRequest = { confirmEmptyTrash = false },
      title = { Text("Esvaziar a lixeira?") },
      text = { Text("Todas as notas da lixeira serão excluídas para sempre.") },
      confirmButton = {
        TextButton(
          onClick = {
            vm.emptyTrash()
            confirmEmptyTrash = false
          },
        ) { Text("Esvaziar", color = MaterialTheme.colorScheme.error) }
      },
      dismissButton = {
        TextButton(onClick = { confirmEmptyTrash = false }) { Text("Cancelar") }
      },
    )
  }
}

@Composable
private fun HomeHeader(
  vm: NotesViewModel,
  count: Int,
  onEmptyTrash: () -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    if (vm.showTrash) {
      IconButton(onClick = vm::closeTrash) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
      }
    }
    Column(Modifier.weight(1f)) {
      Text(
        if (vm.showTrash) "Lixeira" else "Notas",
        style = MaterialTheme.typography.displaySmall,
        color = MaterialTheme.colorScheme.onBackground,
      )
      Text(
        if (vm.showTrash) "Excluídas ficam aqui por 30 dias"
        else "${if (count == 1) "1 nota" else "$count notas"} · ${todayLabel()}",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
    if (vm.showTrash) {
      if (count > 0) {
        TextButton(onClick = onEmptyTrash) {
          Text("Esvaziar", color = MaterialTheme.colorScheme.error)
        }
      }
    } else {
      Box {
        IconButton(onClick = vm::openTrash) {
          Icon(Icons.Outlined.Delete, contentDescription = "Lixeira")
        }
        if (vm.trashCount > 0) {
          Box(
            Modifier
              .align(Alignment.TopEnd)
              .padding(top = 10.dp, end = 10.dp)
              .size(7.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.secondary),
          )
        }
      }
      IconButton(onClick = vm::openSettings) {
        Icon(Icons.Outlined.Settings, contentDescription = "Ajustes")
      }
    }
  }
}

@Composable
private fun SearchField(query: String, onQuery: (String) -> Unit) {
  Surface(
    modifier = Modifier.fillMaxWidth().height(50.dp),
    shape = RoundedCornerShape(18.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 14.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        Icons.Outlined.Search,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(20.dp),
      )
      BasicTextField(
        value = query,
        onValueChange = onQuery,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
          color = MaterialTheme.colorScheme.onSurface,
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        modifier = Modifier.weight(1f).padding(horizontal = 10.dp),
        decorationBox = { inner ->
          Box(contentAlignment = Alignment.CenterStart) {
            if (query.isEmpty()) {
              Text(
                "Buscar notas, #tags…",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
              )
            }
            inner()
          }
        },
      )
      if (query.isNotEmpty()) {
        IconButton(onClick = { onQuery("") }, modifier = Modifier.size(28.dp)) {
          Icon(
            Icons.Default.Close,
            contentDescription = "Limpar busca",
            modifier = Modifier.size(18.dp),
          )
        }
      }
    }
  }
}

@Composable
private fun NoteCard(note: Note, onClick: () -> Unit, onLongClick: () -> Unit) {
  val shape = RoundedCornerShape(22.dp)
  val colors = MaterialTheme.colorScheme
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .clip(shape)
      .combinedClickable(onClick = onClick, onLongClick = onLongClick),
    shape = shape,
    color = if (note.pinned) colors.primaryContainer.copy(alpha = 0.5f) else colors.surface,
    border = BorderStroke(
      1.dp,
      if (note.pinned) colors.primaryContainer.copy(alpha = 0.7f)
      else colors.outlineVariant.copy(alpha = 0.6f),
    ),
  ) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          note.displayTitle,
          style = MaterialTheme.typography.titleMedium,
          color = colors.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f),
        )
        if (note.pinned) {
          Spacer(Modifier.width(6.dp))
          Icon(
            Icons.Filled.Star,
            contentDescription = "Fixada",
            tint = colors.secondary,
            modifier = Modifier.size(15.dp),
          )
        }
      }
      val snippet = note.snippet
      if (snippet.isNotBlank()) {
        Spacer(Modifier.height(3.dp))
        Text(
          snippet.take(160),
          style = MaterialTheme.typography.bodyMedium,
          color = colors.onSurfaceVariant,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )
      }
      Spacer(Modifier.height(10.dp))
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          formatNoteDate(note.updatedAt),
          style = MaterialTheme.typography.labelSmall,
          color = colors.onSurfaceVariant,
          maxLines = 1,
        )
        if (note.tags.isNotEmpty()) {
          Spacer(Modifier.width(8.dp))
          Text("·", style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
          Spacer(Modifier.width(8.dp))
          Text(
            note.tags.take(2).joinToString("  ") { "#$it" },
            style = MaterialTheme.typography.labelSmall,
            color = colors.secondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
          )
        } else {
          Spacer(Modifier.weight(1f))
        }
        if (note.trashedAt != null) {
          Text(
            "Na lixeira",
            style = MaterialTheme.typography.labelSmall,
            color = colors.error,
          )
        }
      }
    }
  }
}

@Composable
private fun EmptyState(
  trash: Boolean,
  searching: Boolean,
  tag: String?,
  onCreate: () -> Unit,
) {
  val title: String
  val subtitle: String
  when {
    trash -> {
      title = "Lixeira vazia"
      subtitle = "Notas excluídas aparecem aqui por 30 dias antes de sumirem."
    }
    searching && tag != null -> {
      title = "Nenhuma nota com #$tag"
      subtitle = "Experimente outra tag ou limpe os filtros."
    }
    searching -> {
      title = "Nada encontrado"
      subtitle = "Tente outro termo ou busque por uma #tag."
    }
    else -> {
      title = "Comece por aqui"
      subtitle = "Uma ideia, uma lista, um plano. Sua primeira nota leva um toque."
    }
  }

  Column(
    modifier = Modifier.fillMaxWidth().padding(top = 64.dp, bottom = 24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Box(
      modifier = Modifier
        .size(86.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        if (trash) Icons.Outlined.Delete else Icons.Outlined.Edit,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(34.dp),
      )
    }
    Spacer(Modifier.height(18.dp))
    Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
    Spacer(Modifier.height(6.dp))
    Text(
      subtitle,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
      modifier = Modifier.padding(horizontal = 28.dp),
    )
    if (!trash && !searching) {
      Spacer(Modifier.height(20.dp))
      Button(onClick = onCreate, shape = CircleShape) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text("Criar nota")
      }
    }
  }
}