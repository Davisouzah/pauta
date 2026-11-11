# Pauta

Notas leves, bonitas e 100% offline. Uma alternativa ao Apple Notes com Markdown de verdade, tags e tarefas interativas.

![Kotlin](https://img.shields.io/badge/Kotlin-2.2-7F52FF?logo=kotlin&logoColor=white)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)
![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white)

## O que tem

- **Markdown com pré-visualização** — títulos, listas, citações, código, negrito, itálico e riscado.
- **Tarefas interativas** — marque e desmarque checkboxes direto na visualização.
- **Tags automáticas** — escreva `#ideia` e filtre por ela na tela inicial.
- **Busca instantânea** — ignora acentos e procura em título e corpo.
- **Fixar notas** no topo da lista.
- **Lixeira com 30 dias** — restaurar ou excluir para sempre, com confirmação.
- **Salvamento automático** com indicador de estado.
- **Tema claro, escuro ou do sistema**, com Material You quando disponível.
- **Compartilhar e copiar** qualquer nota.
- **Sem login, sem nuvem, sem permissões de rede** — suas notas ficam no aparelho.

## Instalação

Baixe o APK mais recente em **Releases** ou em **Actions → Android CI → Artifacts**.

O APK de release é assinado com a chave de debug para instalação pessoal (veja `app/build.gradle.kts`). Para publicar na Play Store, gere uma keystore própria.

## Compilando

Requisitos: JDK 17+, Android SDK 36.1, Gradle 9.8.2 (wrapper incluso).

```bash
./gradlew assembleRelease
```

Saída: `app/build/outputs/apk/release/app-release.apk`.

No Termux, o Gradle pode usar o `aapt2` do sistema:

```properties
# ~/.gradle/gradle.properties
android.aapt2FromMavenOverride=/data/data/com.termux/files/usr/bin/aapt2
```

O build também roda automaticamente no GitHub Actions a cada push na `main`.

## Como funciona

- Kotlin + Jetpack Compose + Material 3, uma única Activity.
- Persistência local em `filesDir/notes.json`, com escrita atômica em arquivo temporário.
- Nenhuma biblioteca de banco, rede ou injeção de dependência: o app inteiro cabe em poucos arquivos.
- Teste unitário do parser de Markdown em `app/src/test`.

## Estrutura

```
app/src/main/java/com/davisouzah/pauta/
├── MainActivity.kt      # tema, navegação e ciclo de vida
├── Notes.kt             # modelo, JSON local, datas e busca
├── NotesViewModel.kt    # estado, autosave, fixar, lixeira
├── HomeScreen.kt        # lista, busca, tags e menus
├── EditorScreen.kt      # edição, formatação e ações
├── Markdown.kt          # parser e renderização
├── Settings.kt          # tema, cores e ordenação
├── Theme.kt             # paleta, tipografia e formas
└── Ui.kt                # componentes e compartilhamento
```

## Licença

MIT.