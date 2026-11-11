@file:OptIn(ExperimentalMaterial3Api::class)

package com.davisouzah.pauta

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsSheet(vm: NotesViewModel) {
  val settings = vm.settings
  ModalBottomSheet(
    onDismissRequest = vm::closeSettings,
    containerColor = MaterialTheme.colorScheme.surface,
    dragHandle = { BottomSheetDefaults.DragHandle() },
  ) {
    Column(
      Modifier
        .navigationBarsPadding()
        .padding(horizontal = 24.dp)
        .padding(bottom = 24.dp),
    ) {
      Text(
        "Ajustes",
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onSurface,
      )
      Spacer(Modifier.height(22.dp))

      SettingLabel("Tema")
      Spacer(Modifier.height(10.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Pill("Sistema", settings.theme == ThemeMode.SYSTEM) { vm.setTheme(ThemeMode.SYSTEM) }
        Pill("Claro", settings.theme == ThemeMode.LIGHT) { vm.setTheme(ThemeMode.LIGHT) }
        Pill("Escuro", settings.theme == ThemeMode.DARK) { vm.setTheme(ThemeMode.DARK) }
      }

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Spacer(Modifier.height(24.dp))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { vm.setDynamicColor(!settings.dynamicColor) }
            .padding(vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(Modifier.weight(1f)) {
            Text(
              "Cores do sistema",
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              "Usa a paleta do seu papel de parede",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          Spacer(Modifier.width(16.dp))
          Switch(checked = settings.dynamicColor, onCheckedChange = vm::setDynamicColor)
        }
      }

      Spacer(Modifier.height(24.dp))
      SettingLabel("Ordenar por")
      Spacer(Modifier.height(10.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Pill("Editadas", settings.sort == SortMode.EDITED) { vm.setSort(SortMode.EDITED) }
        Pill("Criadas", settings.sort == SortMode.CREATED) { vm.setSort(SortMode.CREATED) }
        Pill("Título", settings.sort == SortMode.TITLE) { vm.setSort(SortMode.TITLE) }
      }

      Spacer(Modifier.height(28.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
      Spacer(Modifier.height(18.dp))
      Text(
        "Pauta 1.0",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
      )
      Spacer(Modifier.height(4.dp))
      Text(
        "Leve, offline e feito para durar. Suas notas ficam só neste aparelho.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
private fun SettingLabel(text: String) {
  Text(
    text.uppercase(PT_BR),
    style = MaterialTheme.typography.labelSmall,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
  )
}