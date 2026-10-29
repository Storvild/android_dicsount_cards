# Реорганизация SearchBarComponent — чеклист

## Step 1: Тестовая инфраструктура (якорь)
- [x] `ScreenshotTests.kt` — 7 screenshot-тестов (EmptyState, CardListItem ×4, CardListScreen ×2)
- [x] `SearchBarComponentTest.kt` — 15 smoke-тестов TopBar (переименовать в CardListTopBarTest после Step 3)
- [x] `AboutDialogTest.kt` — закомментирован (AboutDialog не существует до Step 3)
- [x] `SortSelectionDialogTest.kt` — 4 smoke-теста
- [x] `CardListScreenSmokeTest.kt` — закомментирован (нужен fake ViewModel)
- [x] `gradle_build("app:assembleDebug")` — зелёный
- [ ] `gradle_build("app:connectedDebugAndroidTest")` — зелёный (если устройство доступно)

## Step 2: Вынос диалогов из ImportExportDialogs.kt
- [ ] `dialogs/ExportSelectionDialog.kt` (SelectionControlBar + ExportSelectionDialog + ExportDialog)
- [ ] `dialogs/ImportDialogs.kt` (ImportSelectionDialog + ImportDialog, без ImportModeDialog)
- [ ] `dialogs/DeleteSelectionDialog.kt` (SelectionControlBar + DeleteSelectionDialog)
- [ ] `dialogs/ShareSelectionDialog.kt` (SelectionControlBar + ShareSelectionDialog)
- [ ] Удалить `ImportExportDialogs.kt`
- [ ] Обновить импорты в `CardListScreen.kt`
- [ ] `gradle_build("app:assembleDebug")` — зелёный

## Step 3: Рефакторинг SearchBarComponent.kt
- [ ] `CardListTopBar.kt` (CardListTopBar + SearchField + CardListOverflowMenu)
- [ ] `SortSelectionDialog.kt` (SortSelectionDialog + ThemeOptionRow)
- [ ] `AboutDialog.kt` (AboutDialog)
- [ ] `FallbackDialogs.kt` (FallbackDialogs)
- [ ] Удалить `SearchBarComponent.kt`
- [ ] Обновить `CardListScreen.kt` (импорты, SearchBarComponent→CardListTopBar, FallbackDialogs)
- [ ] Переименовать `SearchBarComponentTest.kt` → `CardListTopBarTest.kt`
- [ ] Раккомментировать `AboutDialogTest.kt`
- [ ] `gradle_build("app:assembleDebug")` — зелёный

## Step 4: ArchiveScreen
- [ ] `ArchiveScreen.kt` — SearchBarComponent→SearchField, убрать лишние коллбэки
- [ ] `gradle_build("app:assembleDebug")` — зелёный

## Step 5: Финальная верификация
- [ ] `gradle_build("app:connectedDebugAndroidTest")` — зелёный
- [ ] Ручная проверка (по чеклисту из плана)
