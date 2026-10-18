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


---  
You:  
Исправить ошибки в данном проекте. Если уже есть одна карточка, то при добавлении следующей, окно редактирования сначала открывается, а затем исчезает

Проблема с мгновенным закрытием экрана добавления карты успешно исправлена.

### Причина возникновения ошибки
В [MainActivity.kt](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/main/java/com/example/discountcards/MainActivity.kt) навигация построена на кастомном стеке `backStack` с типом элементов `Destination`. 

Для нового добавления карточки вызывался пункт `Destination.AddEditCard(cardId = null)` с постоянным ключом ViewModel `"add_edit_card_new"`. В силу того, что ViewModel сохранялась в `ViewModelStore` Activity:
1. После сохранения первой карточки во ViewModel флаг `isSaved` устанавливался в `true`.
2. Экран закрывался и возвращал пользователя к списку карт.
3. При нажатии на кнопку «Добавить карту» повторно, Compose запрашивал ViewModel по тому же ключу `"add_edit_card_new"`.
4. Из кэша возвращался старый экземпляр [AddEditCardViewModel](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/main/java/com/example/discountcards/ui/addedit/AddEditCardViewModel.kt) со значением `isSaved = true`.
5. Блок `LaunchedEffect(uiState.isSaved)` в [AddEditCardScreen.kt](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/main/java/com/example/discountcards/ui/addedit/AddEditCardScreen.kt) срабатывал мгновенно при первом же кадре рендеринга и сразу возвращал пользователя назад.

### Внесённые исправления

1. **Уникальные сессионные ключи навигации в [MainActivity.kt](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/main/java/com/example/discountcards/MainActivity.kt)**:
   - В `Destination.AddEditCard` и `Destination.CardDetail` добавлен уникальный идентификатор экрана (`key: String = UUID.randomUUID().toString()`).
   - Ключ передачи во `viewModel(key = ...)` теперь использует этот сессионный `key`, что гарантирует создание нового, чистого экземпляра [AddEditCardViewModel](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/main/java/com/example/discountcards/ui/addedit/AddEditCardViewModel.kt) при каждом открытии экрана добавления/редактирования.

2. **Сохранение даты создания карты**:
   - В [AddEditCardUiState.kt](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/main/java/com/example/discountcards/ui/addedit/AddEditCardUiState.kt) и [AddEditCardViewModel.kt](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/main/java/com/example/discountcards/ui/addedit/AddEditCardViewModel.kt) добавлена обработка `createdAt`. При редактировании карты дата её создания больше не сбрасывается.

3. **Оптимизация сканера камеры**:
   - В [CameraBarcodeScanner.kt](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/main/java/com/example/discountcards/ui/addedit/components/CameraBarcodeScanner.kt) добавлена очистка ресурсов `BarcodeScanner` и закрытие потока `analysisExecutor` при закрытии диалога сканирования.
   
---

# План оптимизации скорости запуска и отрисовки первого экрана с карточками

Оптимизация холодного старта приложения и времени до первого кадра (Time To Initial Display) с предотвращением скачков интерфейса и лагов при генерации штрихкодов.

## Proposed Changes

### 1. Кэширование штрихкодов
- **Цель:** Исключить повторную генерацию штрихкодов через ZXing при скролле и перерисовке.

#### [MODIFY] [BarcodeGenerator.kt](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/main/java/com/example/discountcards/util/BarcodeGenerator.kt)
- Добавить `LruCache<String, Bitmap>` для сохранения сгенерированных штрихкодов.
- Добавить метод `generateBarcodeCached(...)`.

#### [MODIFY] [CardListItem.kt](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/main/java/com/example/discountcards/ui/list/components/CardListItem.kt)
- Переключить вызов на `BarcodeGenerator.generateBarcodeCached(...)`.

---

### 2. Плавное начальное состояние и устранение скачка UI
- **Цель:** Убрать мерцание "Пустого экрана" в момент чтения данных из Room.

#### [MODIFY] [CardListUiState.kt](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/main/java/com/example/discountcards/ui/list/CardListUiState.kt)
- Установить значение по умолчанию `isLoading = true`.

#### [MODIFY] [CardListViewModel.kt](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/main/java/com/example/discountcards/ui/list/CardListViewModel.kt)
- Корректно выставлять начальное состояние загрузки при подписке на Flow.

#### [MODIFY] [CardListScreen.kt](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/main/java/com/example/discountcards/ui/list/CardListScreen.kt)
- Отображать состояние загрузки, если `isLoading == true` и список карточек пуст, исключая ложный показ `EmptyState`.
- Добавить `contentType = { "discount_card" }` для элементов `LazyVerticalGrid`.

---

### 3. Фоновый разогрев БД Room
- **Цель:** Перенести первое обращение к SQLite в фоновый поток при старте приложения.

#### [MODIFY] [DiscountCardsApp.kt](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/main/java/com/example/discountcards/DiscountCardsApp.kt)
- В `onCreate()` инициировать асинхронный фоновый запрос чтения к `AppDatabase` на `Dispatchers.IO`.

---

### 4. Оптимизация сборки R8 в Gradle
- **Цель:** Уменьшить размер DEX и ускорить загрузку классов при старте.

#### [MODIFY] [app/build.gradle.kts](file:///E:/virtualenvs/discount_cards/DiscountCards/app/build.gradle.kts)
- Включить `isMinifyEnabled = true` и `isShrinkResources = true` для Release buildType.

---

## Verification Plan

### Automated Tests
- Запуск всех существующий юнит-тестов через `gradle_build`:
  `gradle_build("app:testDebugUnitTest")`
- Компиляция и сборка приложения:
  `gradle_build("app:assembleDebug")`

### Manual Verification
- Проверка успешной сборки и отсутствия регрессий в логике получения карточек и генерации штрихкодов.


Выполнены все этапы плана по ускорению запуска и исключению задержек на первом экране с карточками.

## Выполненные изменения

### 1. Кэширование штрихкодов в памяти
- **[BarcodeGenerator.kt](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/main/java/com/example/discountcards/util/BarcodeGenerator.kt)**: Добавлен `LruCache<String, Bitmap>` с методом `generateBarcodeCached`.
- **[CardListItem.kt](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/main/java/com/example/discountcards/ui/list/components/CardListItem.kt)**: Использование кэшированной версии генерации штрихкодов, что исключает генерацию ZXing матриц с нуля при скролле и перерисовке.

### 2. Устранение скачка макета (Layout Shift) при старте
- **[CardListUiState.kt](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/main/java/com/example/discountcards/ui/list/CardListUiState.kt)** & **[CardListViewModel.kt](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/main/java/com/example/discountcards/ui/list/CardListViewModel.kt)**: По умолчанию `isLoading = true` при подписке на StateFlow.
- **[CardListScreen.kt](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/main/java/com/example/discountcards/ui/list/CardListScreen.kt)**: Отображение лоадера при загрузке, чтобы исключить ложный мигающий показ `EmptyState` ("Карточек нет") до загрузки данных из Room.

### 3. Фоновый разогрев БД
- **[DiscountCardsApp.kt](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/main/java/com/example/discountcards/DiscountCardsApp.kt)**: В `onCreate()` запущен асинхронный фоновый запрос к БД на `Dispatchers.IO` для исключения задержек первого чтения SQLite на UI-потоке.

### 4. Оптимизация переиспользования списков
- **[CardListScreen.kt](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/main/java/com/example/discountcards/ui/list/CardListScreen.kt)**: Добавлен `contentType = { "discount_card" }` в `LazyVerticalGrid`.

### 5. Включение R8 / Сжатие кода
- **[app/build.gradle.kts](file:///E:/virtualenvs/discount_cards/DiscountCards/app/build.gradle.kts)** & **[app/proguard-rules.pro](file:///E:/virtualenvs/discount_cards/DiscountCards/app/proguard-rules.pro)**: Включены `isMinifyEnabled` и `isShrinkResources` для `release` сборки.

---

## Результаты проверки

- **Unit Tests**: Пройдено 23 из 23 тестов (`app:testDebugUnitTest`).
- **Сборка**: `app:assembleDebug` успешно скомпилирована без ошибок.
, absolutePath=C:\Users\Vadim_S\AppData\Local\Google\AndroidStudio2026.2.1\projects\discountcards.e9625bb0\.artifacts\84e48d7f-3c58-4449-9e58-14f2b18e209e\walkthrough.artifact.md)
Response:
  File was written
Agent:  
Call: set_plan_state(verified=true, state=COMPLETED)
Response:
  Plan state is now COMPLETED (verified).
Agent:  
Все запланированные оптимизации успешно выполнены и протестированы!

### Результаты работы:
1. **Кэширование штрихкодов в памяти ([BarcodeGenerator.kt](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/main/java/com/example/discountcards/util/BarcodeGenerator.kt), [CardListItem.kt](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/main/java/com/example/discountcards/ui/list/components/CardListItem.kt)):** внедрен `LruCache<String, Bitmap>`. Генерация с использованием ZXing выполняется ровно 1 раз для каждого штрихкода.
2. **Устранение скачка UI при открытии ([CardListUiState.kt](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/main/java/com/example/discountcards/ui/list/CardListUiState.kt), [CardListScreen.kt](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/main/java/com/example/discountcards/ui/list/CardListScreen.kt)):** устранено мерцание экрана `EmptyState` при запуске приложения.
3. **Фоновый разогрев БД ([DiscountCardsApp.kt](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/main/java/com/example/discountcards/DiscountCardsApp.kt)):** первичная инициализация Room перенесена на фоновый поток `Dispatchers.IO`.
4. **Оптимизация списка ([CardListScreen.kt](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/main/java/com/example/discountcards/ui/list/CardListScreen.kt)):** в `LazyVerticalGrid` добавлен параметр `contentType = { "discount_card" }`.
5. **Оптимизация R8 ([app/build.gradle.kts](file:///E:/virtualenvs/discount_cards/DiscountCards/app/build.gradle.kts)):** включены `isMinifyEnabled = true` и `isShrinkResources = true` для Release-сборок.

Все 23 юнит-теста прошли успешно (`app:testDebugUnitTest`), и проект собирается без ошибок. Подробности см. в [walkthrough](file:///C:/Users/Vadim_S/AppData/Local/Google/AndroidStudio2026.2.1/projects/discountcards.e9625bb0/.artifacts/84e48d7f-3c58-4449-9e58-14f2b18e209e/walkthrough.artifact.md).
