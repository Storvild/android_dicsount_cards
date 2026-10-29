# Реорганизация `SearchBarComponent` и диалогов списка карт

## Контекст

Текущее состояние пакета `ui/list/components/`:
- `SearchBarComponent.kt` (557 строк) — `SearchBarComponent` + inline `DropdownMenu` (11 пунктов) + inline `AboutDialog` + `SortSelectionDialog` + `ThemeOptionRow`. Имя не отражает содержимое: это фактически **TopAppBar** экрана списка.
- `ImportExportDialogs.kt` (1010 строк) — 7 независимых Composable: `SelectionControlBar`, `ExportSelectionDialog`, `ImportSelectionDialog`, `ImportModeDialog`, `ExportDialog`, `DeleteSelectionDialog`, `ImportDialog`, `ShareSelectionDialog`.
- `CardListScreen.kt` (331 строки) — содержит inline-рендер `ExportDialog` и `ImportDialog` (fallback-диалоги), `LaunchedEffect` для тостов, и все 4 selection-диалога.

**Цель:** разбить на независимые Composable в отдельных файлах, вынести dialogs в собственный пакет, переименовать TopBar, добавить composeTestRule-тесты **до** рефакторинга (как «якорь»).

> [!IMPORTANT]
> Рефакторинг **строго построчный** — каждый шаг компилируется и тесты проходят. Никаких «больших взрывов».

> [!CAUTION]
> `SearchBarComponent` используется в **двух** местах: `CardListScreen` и `ArchiveScreen`. При переименовании `SearchBarComponent` → `CardListTopBar` + `SearchField` нужно обновить оба места. `ArchiveScreen` использует его **только для поиска** (все остальные коллбэки — `{}`), поэтому после рефакторинга там должен вызываться `SearchField`, а не `CardListTopBar`.

---

## User Review Required

> [!IMPORTANT]
> **Подтверждение структуры новых файлов** (взято из предыдущего обсуждения):
> ```
> ui/list/
> ├── CardListScreen.kt            (обновлён: импорты + FallbackDialogs)
> ├── CardListUiState.kt
> ├── CardListViewModel.kt
> └── components/
>     ├── CardListTopBar.kt        [переименован SearchBarComponent.kt]
>     │     ├── CardListTopBar
>     │     ├── SearchField        (private)
>     │     └── CardListOverflowMenu (private)
>     ├── SortSelectionDialog.kt   [вынесен из SearchBarComponent.kt]
>     ├── AboutDialog.kt           [вынесен из SearchBarComponent.kt]
>     ├── FallbackDialogs.kt       [вынесен из CardListScreen.kt]
>     ├── EmptyState.kt
>     ├── CardListItem.kt
>     └── dialogs/
>         ├── ExportSelectionDialog.kt  (ExportSelectionDialog + ExportDialog)
>         ├── ImportDialogs.kt          (ImportSelectionDialog + ImportDialog)
>         ├── DeleteSelectionDialog.kt
>         └── ShareSelectionDialog.kt
> ```

> [!WARNING]
> **Тесты** — сейчас в проекте **нет** ни `unitTest`, ни `androidTest` (в `gradle/libs.versions.toml` уже есть все нужные артефакты, в `app/build.gradle.kts` уже подключены `compose-ui-test-junit4` и `espresso-core`, но `src/test` и `src/androidTest` отсутствуют). Создаём их с нуля.

> [!NOTE]
> `ThemeMode`/`SortOrder` сейчас пробрасываются через `CardListScreen` → `SearchBarComponent` «насквозь» (values + callbacks). Это нарушение Single Source of Truth, но **не трогаем** в этом рефакторинге — оставим как есть. Это отдельная задача (нужен Hilt/Inject, а в проекте Hilt отсутствует).

---

## Open Questions

- [ ] Нужны ли **screenshot-тесты** (Pixel 5 / Pixel 7) в `androidTest`, или достаточно smoke-тестов `composeTestRule`?
- [ ] Тесты `CardListTopBar` — проверяем только **состояния** (query, clear button, menu open/close), или также **коллеки** (export/import/share/archive/add)? Предлагаю только состояния — коллбэки покрываются через `CardListScreen` с fake ViewModel.
- [ ] **Удалить** `ImportModeDialog` из `ImportDialogs.kt` — подтверждено как мёртвый код (не вызывается нигде в проекте).

---

## Proposed Changes

### 1. Тестовая инфраструктура (сначала)

#### [NEW] [build.gradle.kts](file:///E:/virtualenvs/discount_cards/DiscountCards/app/build.gradle.kts)
Добавляем `androidx.compose.ui:ui-test-junit4` (уже есть) и `androidx.compose.ui:ui-test-manifest` (уже есть). Дополнительно добавляем `screenshot-testing` (shibui) — **НЕ** (это сторонняя зависимость, не нужна). Используем **нативный** `createScreenshot` из `androidx.compose.ui.test.junit4` (доступен с compose BOM 2024.09+).

#### [NEW] [ScreenshotTests.kt](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/androidTest/java/com/example/discountcards/ScreenshotTests.kt)
**Screenshot-тесты** (сравнение скриншотов через `createScreenshot` из `ui-test-junit4`):
- `CardListTopBar_screenshot` — TopBar с query="" и query="Пятёрочка" (2 скриншота)
- `CardListScreen_screenshot_empty` — пустое состояние
- `CardListScreen_screenshot_withCards` — 2-3 карты в сетке
- `AboutDialog_screenshot` — открытый диалог
- `SortSelectionDialog_screenshot` — открытый диалог
- `CardListItem_screenshot` — одна карточка с штрихкодом
- `CardListItem_screenshot_compact` — карточка без штрихкода (showBarcode=false)
- `EmptyState_screenshot` — пустое состояние
- `CardListItem_screenshot_favorite` — карточка с фавориткой
- `CardListItem_screenshot_archived` — карточка с бейджем «Архив»

Каждый тест: `createScreenshot()`, сравнение с baseline в `app/src/androidTest/snapshots/`. Первый запуск создаёт baselines (тест пройдёт), последующие — сравнивают.

#### [NEW] [CardListTopBarTest.kt](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/androidTest/java/com/example/discountcards/ui/list/CardListTopBarTest.kt)
Smoke-тесты `CardListTopBar` (до рефакторинга — тестируем текущий `SearchBarComponent`):
- `searchField_showsPlaceholder_whenEmpty`
- `searchField_displaysQuery`
- `clearButton_visible_whenQueryNotEmpty`
- `clearButton_triggersOnClearQuery`
- `menuButton_openMenu_showsAllItems` (Файл, Поделиться, Архив, Добавить, Тема, Сортировка, Настройки, О программе)
- `menu_exportJsonClick_invokesCallback`
- `menu_importJsonClick_invokesCallback`
- `menu_shareClick_invokesCallback`
- `menu_addCardClick_invokesCallback`
- `menu_archiveClick_invokesCallback`
- `menu_settingsClick_invokesCallback`
- `menu_aboutClick_showsAboutDialog`
- `menu_sortClick_showsSortDialog`
- `themeCheckbox_reflectsCurrentMode`
- `themeCheckbox_toggle_changesMode`

#### [NEW] [AboutDialogTest.kt](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/androidTest/java/com/example/discountcards/ui/list/AboutDialogTest.kt)
- `aboutDialog_showsAppNameAndVersion`
- `aboutDialog_copyNameButton_copiesToClipboard`
- `aboutDialog_githubLink_isClickable`
- `aboutDialog_qrCode_isVisible`
- `aboutDialog_shareButton_sharesText`

#### [NEW] [SortSelectionDialogTest.kt](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/androidTest/java/com/example/discountcards/ui/list/SortSelectionDialogTest.kt)
- `sortDialog_showsThreeOptions`
- `sortDialog_selectAlphabetical_invokesCallback`
- `sortDialog_selectFrequency_invokesCallback`
- `sortDialog_selectCreationDate_invokesCallback`

#### [NEW] [CardListScreenSmokeTest.kt](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/androidTest/java/com/example/discountcards/ui/list/CardListScreenSmokeTest.kt)
Smoke-тест всего `CardListScreen` с **fake `CardListViewModel`** (реальный VM требует Room + DataStore, слишком тяжёлый для smoke):
- `cardListScreen_emptyState_shown_whenNoCards`
- `cardListScreen_showsCards_whenStateNotEmpty`
- `cardListScreen_searchQuery_filters`
- `cardListScreen_addButton_triggersCallback`
- `cardListScreen_fab_triggersAddCard`

> Fake `CardListViewModel` будет создаваться через `ViewModelProvider.Factory` с `ViewModelStoreOwner`-заглушкой. ViewModel — `@HiltViewModel`? Нет, `CardListViewModel` — обычный `: ViewModel()` (см. `ViewModelFactory.kt`), так что можно подменить через `ViewModelProvider.AndroidViewModelFactory` с `ApplicationProvider.getApplicationContext()`.

---

### 2. Вынос диалогов из `ImportExportDialogs.kt`

#### [DELETE] [ImportExportDialogs.kt](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/main/java/com/example/discountcards/ui/list/components/ImportExportDialogs.kt)
После выноса всех 8 Composable в 4 файла.

#### [NEW] [ExportSelectionDialog.kt](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/main/java/com/example/discountcards/ui/list/components/dialogs/ExportSelectionDialog.kt)
- `SelectionControlBar` (private, используется только в `ExportSelectionDialog`)
- `ExportSelectionDialog`
- `ExportDialog` (fallback — показ JSON-текста)

#### [NEW] [ImportDialogs.kt](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/main/java/com/example/discountcards/ui/list/components/dialogs/ImportDialogs.kt)
- `ImportSelectionDialog`
- `ImportDialog` (fallback — ручной ввод JSON/CSV)
- **НЕ включает** `ImportModeDialog` — удалён как мёртвый код (не вызывается нигде в проекте)

#### [NEW] [DeleteSelectionDialog.kt](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/main/java/com/example/discountcards/ui/list/components/dialogs/DeleteSelectionDialog.kt)
- `SelectionControlBar` (private — дублируем, чтобы не тащить в общий файл)
- `DeleteSelectionDialog`

#### [NEW] [ShareSelectionDialog.kt](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/main/java/com/example/discountcards/ui/list/components/dialogs/ShareSelectionDialog.kt)
- `SelectionControlBar` (private)
- `ShareSelectionDialog`

> [!NOTE]
> `SelectionControlBar` — маленький (3 кнопки) и используется в 4 диалогах. Дублируем в каждом файле как `private @Composable` — 25 строк × 4 = 100 строк дублирования, но зато каждый диалог **самодостаточен** и не тянет за собой общий файл. Альтернатива — вынести в `SelectionControlBar.kt`. Решение: **дублируем** (проще, без cross-file зависимости).

#### [MODIFY] [CardListScreen.kt](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/main/java/com/example/discountcards/ui/list/CardListScreen.kt)
Обновить импорты: убрать `SearchBarComponent` (заменить на `CardListTopBar`), убрать `ExportDialog`/`ImportDialog`/`DeleteSelectionDialog`/`ExportSelectionDialog`/`ImportSelectionDialog`/`ShareSelectionDialog` (теперь из `dialogs.*`), убрать `SearchBarComponent` → `CardListTopBar`.

---

### 3. Переименование и разборка `SearchBarComponent.kt`

#### [DELETE] [SearchBarComponent.kt](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/main/java/com/example/discountcards/ui/list/components/SearchBarComponent.kt)

#### [NEW] [CardListTopBar.kt](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/main/java/com/example/discountcards/ui/list/components/CardListTopBar.kt)
Структура:
```kotlin
@Composable
fun CardListTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onExportJsonClick: () -> Unit,
    onExportCsvClick: () -> Unit,
    onImportJsonClick: () -> Unit,
    onImportCsvClick: () -> Unit,
    onShareClick: () -> Unit,
    onAddCardClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onArchiveClick: () -> Unit,
    onAboutClick: () -> Unit,           // новый коллбэк (был inline)
    onThemeModeSelected: (ThemeMode) -> Unit,
    onSortOrderSelected: (SortOrder) -> Unit,
    onSettingsClick: () -> Unit,
    currentThemeMode: ThemeMode,
    currentSortOrder: SortOrder,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var fileSubmenuExpanded by remember { mutableStateOf(false) }

    Row(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SearchField(
            query = query,
            onQueryChange = onQueryChange,
            onClearQuery = onClearQuery,
            modifier = Modifier.weight(1f)
        )
        CardListOverflowMenu(
            expanded = menuExpanded,
            fileSubmenuExpanded = fileSubmenuExpanded,
            onToggleMenu = { menuExpanded = it },
            onToggleFileSubmenu = { fileSubmenuExpanded = it },
            onExportJson = { ...; onExportJsonClick() },
            ... (все 11 пунктов)
            onAbout = { ...; onAboutClick() }
        )
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(...)  // текущий код строка 100–135
}

@Composable
private fun CardListOverflowMenu(
    expanded: Boolean,
    fileSubmenuExpanded: Boolean,
    onToggleMenu: (Boolean) -> Unit,
    onToggleFileSubmenu: (Boolean) -> Unit,
    onExportJson: () -> Unit,
    onImportJson: () -> Unit,
    onExportCsv: () -> Unit,
    onImportCsv: () -> Unit,
    onShare: () -> Unit,
    onArchive: () -> Unit,
    onAddCard: () -> Unit,
    onDelete: () -> Unit,
    onThemeToggle: () -> Unit,
    isDarkThemeEffective: Boolean,
    onSort: () -> Unit,
    onSettings: () -> Unit,
    onAbout: () -> Unit
) { ... }  // текущий DropdownMenu, строки 137–290
}
```

**Ключевые изменения:**
- Убрали `onExportClick`/`onImportClick` (дефолтные алиасы, которые ни разу не вызываются — проверим при рефакторинге, удаляем).
- Убрали inline `AboutDialog` и inline `SortSelectionDialog` — теперь они вызываются снаружи (`CardListScreen`).
- Убрали `ThemeOptionRow` (он нужен только в `SortSelectionDialog`).
- Убрали `showSortDialog`/`showAboutDialog` state — они переезжают в `CardListScreen`.

#### [NEW] [SortSelectionDialog.kt](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/main/java/com/example/discountcards/ui/list/components/SortSelectionDialog.kt)
- `SortSelectionDialog`
- `ThemeOptionRow` (private)
- Тесты из `SortSelectionDialogTest.kt` покрывают

#### [NEW] [AboutDialog.kt](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/main/java/com/example/discountcards/ui/list/components/AboutDialog.kt)
- `AboutDialog(onDismiss: () -> Unit)` — **без** коллбэков (весь логик — clipboard, share, QR — самодостаточный)
- Тесты из `AboutDialogTest.kt` покрывают

#### [NEW] [FallbackDialogs.kt](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/main/java/com/example/discountcards/ui/list/components/FallbackDialogs.kt)
- `@Composable fun FallbackDialogs(
       exportedJson: String?,
       onExportDismiss: () -> Unit,
       showImportDialog: Boolean,
       onImportDismiss: () -> Unit,
       onImport: (String) -> Unit
   )`
- Инкапсулирует **оба** fallback-диалога, которые сейчас inline-рендерятся в `CardListScreen` (строки 303–322)
- В `CardListScreen` заменяем inline-рендер на вызов `FallbackDialogs(...)`

#### [MODIFY] [CardListScreen.kt](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/main/java/com/example/discountcards/ui/list/CardListScreen.kt)
- `SearchBarComponent` → `CardListTopBar`
- Импорты `com.example.discountcards.ui.list.components.ExportDialog` и др. → `com.example.discountcards.ui.list.components.dialogs.*`
- Inline-рендер `ExportDialog`/`ImportDialog` → `FallbackDialogs`
- `showSortDialog`/`showAboutDialog` state — добавляем в `CardListScreen` (были в `SearchBarComponent`)
- `CardListTopBar` получает `onAboutClick = { showAboutDialog = true }` и `onSortOrderSelected` — уже есть

#### [MODIFY] [ArchiveScreen.kt](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/main/java/com/example/discountcards/ui/archive/ArchiveScreen.kt)
- `SearchBarComponent` → `SearchField`
- Убираем все дефолтные коллбэки (они не нужны — ArchiveScreen использует только поиск)
- Импорты обновить

---

### 4. Обновление импортов и верификация

#### [MODIFY] [CardListScreen.kt](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/main/java/com/example/discountcards/ui/list/CardListScreen.kt)
(см. выше)

#### [MODIFY] [ArchiveScreen.kt](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/main/java/com/example/discountcards/ui/archive/ArchiveScreen.kt)
(см. выше)

#### Проверить другие места
- `ViewModelFactory.kt` — не трогать
- `CardDetailScreen.kt`, `AddEditCardScreen.kt`, `SettingsScreen.kt` — не трогать (не используют `SearchBarComponent`)

---

## Verification Plan

### Automated Tests

1. **До рефакторинга** (якорь):
   ```
   gradle_build("app:assembleDebug")
   gradle_build("app:connectedDebugAndroidTest")   # smoke-тесты на устройстве/эмуляторе
   ```
   - Все тесты из `CardListTopBarTest`, `AboutDialogTest`, `SortSelectionDialogTest`, `CardListScreenSmokeTest` **зелёные** на текущем коде.
   - Если устройство недоступно — `gradle_build("app:assembleDebug")` как минимум подтверждает компиляцию.

2. **После каждого шага рефакторинга** (5 шагов):
   ```
   gradle_build("app:assembleDebug")          # после шага 1 (dialogs)
   gradle_build("app:assembleDebug")          # после шага 2 (TopBar + Sort + About + Fallback)
   gradle_build("app:assembleDebug")          # после шага 3 (ArchiveScreen)
   gradle_build("app:connectedDebugAndroidTest")  # финальная проверка
   ```
   - Все тесты остаются зелёными.
   - Никакого изменения поведения — только перестановка кода.

3. **Финальная верификация:**
   ```
   gradle_build("app:connectedDebugAndroidTest")
   ```

### Manual Verification

> [!TIP]
> После завершения — ручная проверка:
> 1. Запустить app, проверить: поиск, меню (все 11 пунктов), сортировка (3 варианта), тема (toggle), О программе (QR, copy, share), экспорт/импорт (JSON+CSV), delete, share, archive, add card, settings.
> 2. Перейти в Архив — поиск работает, остальное меню отсутствует (как и было).
> 3. Тёмная/светлая тема — переключается корректно.
> 4. Экспорт → JSON → fallback-диалог с копированием в буфер.
> 5. Импорт → ручной ввод → `ImportDialog` fallback.
