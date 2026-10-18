# Project Plan

Приложение для хранения скидочных карточек (Discount Cards). 
Требования:
- Быстрая загрузка приложения (минимальный footprint, локальная БД Room, эффективная Compose UI)
- Главный экран: список карточек с названием магазинов, возможностью поиска и быстрыми действиями
- Экраны просмотра карточки: полноэкранное отображение QR-кода / штрихкода (поддержка QR_CODE, CODE_128, EAN_13, EAN_8, UPC_A, CODE_39, PDF_417, AZTEC и др.) с максимальной яркостью экрана при просмотре
- Добавление и редактирование карточки: название магазина, номер/содержимое кода, формат штрихкода, примечание, выбор цвета/иконки карточки, сканирование штрихкода с камеры или импорт из файла/галереи
- Импорт и экспорт в JSON: импорт с режимами (добавить к текущим / перезаписать всё) и экспорт в JSON файл
- Поддержка Тёмной и Светлой темы (Jetpack Compose Material3)
- Язык интерфейса: Русский

## Project Brief

# Project Brief: Discount Cards (Скидочные карточки)

## Features
1. **Управление картами и поиск**: Главный экран со списком сохраненных скидочных карточек, поиском по названию магазина и быстрыми действиями.
2. **Полноэкранный просмотр штрихкода**: Показ QR-кодов и штрихкодов (QR_CODE, CODE_128, EAN_13, EAN_8, UPC_A, CODE_39, PDF_417, AZTEC) с автоматической максимальной яркостью экрана.
3. **Добавление и редактирование с поддержкой галереи**: Создание и редактирование карт с указанием цвета, иконки и примечаний; сканирование кода с камеры или импорт/распознавание из сохраненного фото в галерее.
4. **Импорт и экспорт данных (JSON)**: Резервное копирование списка карт в файл JSON и восстановление с выбором режима (добавление к текущим или полная перезапись).
5. **Адаптивный UI и темизация**: Поддержка светлой/тёмной тем (Material 3), адаптивность макетов и локализация на русский язык.

## High-Level Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Navigation**: Jetpack Navigation 3
- **Adaptive Layouts**: Compose Material Adaptive (`androidx.compose.material3.adaptive`)
- **Persistence Layer**: Room Database
- **Barcode & Camera Processing**: ML Kit Barcode Scanning / ZXing (генерация, сканирование с камеры и импорт из галереи)
- **Async & Architecture**: Kotlin Coroutines, Flow, ViewModel / Architecture Components

## Implementation Steps
**Total Duration:** 33m 33s

### Task_1_DataLayerAndBarcodeEngine: Set up Room Database for discount cards (Entity, DAO, Repository), dependencies, and barcode/QR bitmap generation utility for QR_CODE, CODE_128, EAN_13, EAN_8, UPC_A, CODE_39, PDF_417, and AZTEC formats.
- **Status:** COMPLETED
- **Updates:** База данных Room (DiscountCard entity, DiscountCardDao, AppDatabase), CardRepositoryImpl и генератор штрихкодов BarcodeGenerator успешно реализованы и проверены юниты-тестами. Сборка проекта успешна.
- **Acceptance Criteria:**
  - Room database entity and DAO created for discount cards
  - Barcode generation utility created for supported formats
  - build pass
- **Duration:** 8m 17s

### Task_2_MainScreenAndBarcodeViewer: Implement Main Screen displaying card list with search filtering by store/name and full-screen barcode view with auto-screen-brightness maxing out on display.
- **Status:** COMPLETED
- **Updates:** Реализованы CardListScreen и CardDetailScreen/BarcodeViewerScreen с поиском, анимациями, авто-максимальной яркостью экрана при просмотре кода и диалогом удаления. Проверено сборкой и юниты-тестами.
- **Acceptance Criteria:**
  - Card list screen displays cards and filters by store name
  - Fullscreen barcode view renders barcode and sets max screen brightness
  - build pass
- **Duration:** 6m 31s

### Task_3_AddEditCardAndBarcodeScanner: Implement Add/Edit Card screen with CameraX ML Kit barcode scanner, gallery image picker barcode scanning, color/icon/note customization, and database persistence.
- **Status:** COMPLETED
- **Updates:** Реализован AddEditCardScreen, CameraX + ML Kit сканер с камеры, распознавание штрихкодов из галереи, выбор цветов и иконки категории, валидация и интеграция с Room БД. Написаны тесты, сборка успешна.
- **Acceptance Criteria:**
  - Add/Edit card screen allows manual entry, camera scan, and gallery scan
  - Cards can be saved and edited in Room database
  - build pass
- **Duration:** 4m 33s

### Task_4_BackupRestoreAndTheming: Implement JSON backup export and import (merge/overwrite modes), Material 3 Light/Dark theme support, Russian localization, and app resources/icon.
- **Status:** COMPLETED
- **Updates:** Реализованы BackupManager, SAF экспорт/импорт JSON (Merge/Overwrite), Material 3 Светлая и Тёмная темы с высоким контрастом и белым фоном под штрихкодом, русская локализация. Юнит-тесты и сборка прошли успешно.
- **Acceptance Criteria:**
  - JSON export and import backup functionality working
  - Material 3 Light and Dark themes implemented
  - Russian strings localized
  - build pass
- **Duration:** 6m 54s

### Task_5_RunAndVerify: Run and verify application stability, instruct critic_agent to verify no crashes occur, confirm alignment with user requirements, and report any critical UI issues.
- **Status:** COMPLETED
- **Updates:** Приложение проверено агент-критиком. Все функции (быстрый запуск, Room БД, генератор/сканер штрихкодов ZXing/ML Kit из камеры и галереи, авто-яркость экрана, SAF импорт/экспорт JSON в режимах Merge/Overwrite, Material 3 Light/Dark темы с белым контейнером штрихкода, русская локализация) работают идеально. Сбоев и крашей нет.
- **Acceptance Criteria:**
  - make sure all existing tests pass
  - build pass
  - app does not crash
  - app meets all user requirements
- **Duration:** 7m 18s

