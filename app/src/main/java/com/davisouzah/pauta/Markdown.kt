package com.davisouzah.pauta

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

sealed interface MdBlock {
  data class Heading(val level: Int, val text: String) : MdBlock
  data class Task(val line: Int, val checked: Boolean, val text: String) : MdBlock
  data class Bullet(val text: String) : MdBlock
  data class Numbered(val marker: String, val text: String) : MdBlock
  data class Quote(val text: String) : MdBlock
  data class Code(val text: String) : MdBlock
  data class Paragraph(val text: String) : MdBlock
  data object Divider : MdBlock
  data object Space : MdBlock
}

private val TASK_LINE = Regex("""^[-*]\s+\[([ xX])]\s*(.*)$""")
private val BULLET_LINE = Regex("""^[-*]\s+(.+)$""")
private val NUMBER_LINE = Regex("""^(\d+[.)])\s+(.+)$""")

fun parseMarkdown(text: String): List<MdBlock> {
  val blocks = mutableListOf<MdBlock>()
  val code = StringBuilder()
  var inCode = false

  text.lines().forEachIndexed { index, raw ->
    val line = raw.trimEnd()
    val trimmed = line.trimStart()

    if (trimmed.startsWith("```")) {
      if (inCode) {
        blocks += MdBlock.Code(code.toString().trimEnd())
        code.clear()
      }
      inCode = !inCode
      return@forEachIndexed
    }
    if (inCode) {
      code.appendLine(line)
      return@forEachIndexed
    }

    when {
      trimmed.isBlank() -> blocks += MdBlock.Space
      trimmed == "---" || trimmed == "***" -> blocks += MdBlock.Divider
      trimmed.startsWith("### ") -> blocks += MdBlock.Heading(3, trimmed.removePrefix("### "))
      trimmed.startsWith("## ") -> blocks += MdBlock.Heading(2, trimmed.removePrefix("## "))
      trimmed.startsWith("# ") -> blocks += MdBlock.Heading(1, trimmed.removePrefix("# "))
      TASK_LINE.matches(trimmed) -> {
        val match = TASK_LINE.find(trimmed)!!
        blocks += MdBlock.Task(index, match.groupValues[1].equals("x", true), match.groupValues[2])
      }
      trimmed.startsWith("> ") -> blocks += MdBlock.Quote(trimmed.removePrefix("> "))
      BULLET_LINE.matches(trimmed) -> blocks += MdBlock.Bullet(trimmed.replaceFirst(BULLET_LINE, "$1"))
      NUMBER_LINE.matches(trimmed) -> {
        val match = NUMBER_LINE.find(trimmed)!!
        blocks += MdBlock.Numbered(match.groupValues[1], match.groupValues[2])
      }
      else -> blocks += MdBlock.Paragraph(trimmed)
    }
  }

  if (inCode && code.isNotEmpty()) blocks += MdBlock.Code(code.toString().trimEnd())
  return blocks
}

private val INLINE = Regex("""\*\*(.+?)\*\*|\*(.+?)\*|`([^`]+)`|~~(.+?)~~|#([\p{L}\p{N}_-]+)""")

fun inlineMarkdown(text: String, accent: Color, codeBackground: Color): AnnotatedString =
  buildAnnotatedString {
    var last = 0
    INLINE.findAll(text).forEach { match ->
      if (match.range.first > last) append(text.substring(last, match.range.first))
      val bold = match.groups[1]?.value
      val italic = match.groups[2]?.value
      val code = match.groups[3]?.value
      val strike = match.groups[4]?.value
      val tag = match.groups[5]?.value
      when {
        bold != null -> withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(bold) }
        italic != null -> withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(italic) }
        code != null -> withStyle(
          SpanStyle(fontFamily = FontFamily.Monospace, background = codeBackground)
        ) { append(code) }
        strike != null -> withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) { append(strike) }
        tag != null -> withStyle(SpanStyle(color = accent, fontWeight = FontWeight.Medium)) {
          append(match.value)
        }
        else -> append(match.value)
      }
      last = match.range.last + 1
    }
    if (last < text.length) append(text.substring(last))
  }

@Composable
fun MarkdownBody(
  text: String,
  onToggleTask: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  val blocks = remember(text) { parseMarkdown(text) }
  val colors = MaterialTheme.colorScheme
  val accent = colors.secondary
  val codeBackground = colors.surfaceVariant

  Column(modifier = modifier.fillMaxWidth()) {
    blocks.forEach { block ->
      when (block) {
        is MdBlock.Heading -> Text(
          text = inlineMarkdown(block.text, accent, codeBackground),
          style = when (block.level) {
            1 -> MaterialTheme.typography.headlineMedium
            2 -> MaterialTheme.typography.titleLarge
            else -> MaterialTheme.typography.titleMedium
          },
          color = colors.onSurface,
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = if (block.level == 1) 18.dp else 14.dp, bottom = 4.dp),
        )

        is MdBlock.Paragraph -> Text(
          text = inlineMarkdown(block.text, accent, codeBackground),
          style = MaterialTheme.typography.bodyLarge,
          color = colors.onSurface,
          modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
        )

        is MdBlock.Task -> Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onToggleTask(block.line) }
            .padding(vertical = 4.dp),
          verticalAlignment = Alignment.Top,
        ) {
          Box(
            modifier = Modifier
              .padding(top = 3.dp)
              .size(20.dp)
              .clip(RoundedCornerShape(6.dp))
              .then(
                if (block.checked) Modifier.background(colors.primary)
                else Modifier.border(1.5.dp, colors.outline, RoundedCornerShape(6.dp))
              ),
            contentAlignment = Alignment.Center,
          ) {
            if (block.checked) {
              Icon(
                Icons.Default.Check,
                contentDescription = "Tarefa concluída",
                tint = colors.onPrimary,
                modifier = Modifier.size(14.dp),
              )
            }
          }
          Spacer(Modifier.width(10.dp))
          Text(
            text = inlineMarkdown(block.text, accent, codeBackground),
            style = MaterialTheme.typography.bodyLarge.copy(
              textDecoration = if (block.checked) TextDecoration.LineThrough else null,
            ),
            color = if (block.checked) colors.onSurfaceVariant else colors.onSurface,
          )
        }

        is MdBlock.Bullet -> Row(
          Modifier.fillMaxWidth().padding(vertical = 2.dp),
          verticalAlignment = Alignment.Top,
        ) {
          Text(
            "•",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurfaceVariant,
            modifier = Modifier.width(18.dp),
          )
          Text(
            inlineMarkdown(block.text, accent, codeBackground),
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurface,
          )
        }

        is MdBlock.Numbered -> Row(
          Modifier.fillMaxWidth().padding(vertical = 2.dp),
          verticalAlignment = Alignment.Top,
        ) {
          Text(
            block.marker,
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurfaceVariant,
            modifier = Modifier.widthIn(min = 24.dp),
          )
          Spacer(Modifier.width(6.dp))
          Text(
            inlineMarkdown(block.text, accent, codeBackground),
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurface,
          )
        }

        is MdBlock.Quote -> Text(
          text = inlineMarkdown(block.text, accent, codeBackground),
          style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
          color = colors.onSurfaceVariant,
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .drawBehind {
              drawRect(
                color = accent.copy(alpha = 0.55f),
                topLeft = Offset.Zero,
                size = Size(3.dp.toPx(), size.height),
              )
            }
            .padding(start = 14.dp, top = 2.dp, bottom = 2.dp),
        )

        is MdBlock.Code -> Surface(
          color = codeBackground,
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        ) {
          Text(
            block.text,
            style = MaterialTheme.typography.bodyMedium.copy(
              fontFamily = FontFamily.Monospace,
              lineHeight = 19.sp,
            ),
            color = colors.onSurface,
            modifier = Modifier.padding(12.dp),
          )
        }

        MdBlock.Divider -> HorizontalDivider(
          modifier = Modifier.padding(vertical = 12.dp),
          color = colors.outlineVariant,
        )

        MdBlock.Space -> Spacer(Modifier.height(9.dp))
      }
    }
  }
}