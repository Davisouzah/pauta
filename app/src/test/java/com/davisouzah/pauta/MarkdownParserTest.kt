package com.davisouzah.pauta

import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownParserTest {
  @Test
  fun parsesCommonBlocks() {
    val blocks = parseMarkdown(
      """
      # Título
      Texto **forte**

      - [x] Feito
      - [ ] Aberto
      - item
      1. primeiro
      > citação
      ```
      val x = 1
      ```
      ---
      """.trimIndent()
    )

    assertTrue(blocks.any { it is MdBlock.Heading && it.level == 1 })
    assertTrue(blocks.any { it is MdBlock.Task && it.checked && it.line == 3 })
    assertTrue(blocks.any { it is MdBlock.Task && !it.checked })
    assertTrue(blocks.any { it is MdBlock.Bullet })
    assertTrue(blocks.any { it is MdBlock.Numbered && it.marker == "1." })
    assertTrue(blocks.any { it is MdBlock.Quote })
    assertTrue(blocks.any { it is MdBlock.Code && it.text.contains("val x = 1") })
    assertTrue(blocks.any { it is MdBlock.Divider })
  }
}