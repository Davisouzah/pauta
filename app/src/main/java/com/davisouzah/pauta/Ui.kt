package com.davisouzah.pauta

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Pill(
  text: String,
  selected: Boolean,
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
) {
  Surface(
    onClick = onClick,
    modifier = modifier,
    shape = CircleShape,
    color = if (selected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
    contentColor = if (selected) MaterialTheme.colorScheme.onPrimary
    else MaterialTheme.colorScheme.onSurfaceVariant,
  ) {
    Box(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
      contentAlignment = Alignment.Center,
    ) {
      Text(text, style = MaterialTheme.typography.labelLarge, maxLines = 1)
    }
  }
}

@Composable
fun SectionLabel(text: String) {
  Text(
    text.uppercase(PT_BR),
    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
    color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier = Modifier.padding(start = 4.dp, top = 10.dp, bottom = 2.dp),
  )
}

@Composable
fun SheetAction(
  icon: ImageVector,
  label: String,
  danger: Boolean = false,
  onClick: () -> Unit,
) {
  val color = if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .clickable(onClick = onClick)
      .padding(horizontal = 8.dp, vertical = 14.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
    Spacer(Modifier.width(16.dp))
    Text(label, style = MaterialTheme.typography.bodyLarge, color = color)
  }
}

private fun noteText(note: Note): String =
  listOf(note.displayTitle, note.body)
    .filter { it.isNotBlank() }
    .joinToString("\n\n")

fun shareNote(context: Context, note: Note) {
  val intent = Intent(Intent.ACTION_SEND).apply {
    type = "text/plain"
    putExtra(Intent.EXTRA_SUBJECT, note.displayTitle)
    putExtra(Intent.EXTRA_TEXT, noteText(note))
  }
  context.startActivity(Intent.createChooser(intent, "Compartilhar nota"))
}

fun copyNote(context: Context, note: Note) {
  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
  clipboard.setPrimaryClip(ClipData.newPlainText("Pauta", noteText(note)))
  Toast.makeText(context, "Texto copiado", Toast.LENGTH_SHORT).show()
}