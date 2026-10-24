# Отчет по анализу кода и тестовому покрытию приложения "Discount Cards"

## 1. Общая оценка проекта
Проект **Discount Cards** представляет собой Android-приложение на **Kotlin / Jetpack Compose** с архитектурой **MVVM / Clean Architecture (Data -> Repository -> UI)**.
- **БД**: Room с миграциями (версия 4).
- **Хранение настроек**: DataStore Preferences + SharedPreferences.
- **Работа со штрихкодами**: ZXing + ML Kit Barcode Scanning.
- **Интерфейс**: Material 3, адаптивная поддержка тем и локализации.

В целом код написан аккуратно, структуры данных декомпозированы, основные функции экспорта/импорта и работы со штрихкодами реализованы корректно. Юнит-тесты выполняются успешно (33/33). Однако в ходе подробного анализа выявлены **потенциальные ошибки, утечки ресурсов, проблемы с управлением потоками, локализацией и недостаток тестового покрытия в ряде компонентов**.

---

## 2. Анализ потенциальных ошибок и рисков (Code Analysis)

### 🔴 Критические и важные риски (High / Medium Severity)

#### 1. Утечка ресурсов и потоков при сканировании штрихкодов (ML Kit + CameraX)
- **Файл**: [`CameraBarcodeScanner.kt`](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/main/java/com/example/discountcards/ui/addedit/components/CameraBarcodeScanner.kt)
- **Проблема**:
  ```kotlin
  val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
  val barcodeScanner = remember { BarcodeScanning.getClient() }
  ```
  `Executor` и `BarcodeScanner` создаются через `remember`, но при закрытии диалога сканера их ресурсы **никогда не закрываются** (`analysisExecutor.shutdown()` и `barcodeScanner.close()` отсутствуют).
- **Риск**: Нативный утечка ресурсов и памяти ML Kit, а также утечка однопоточного экзекутора при каждом открытии сканера камеры.
- **Решение**: Использовать `DisposableEffect` для вызова `analysisExecutor.shutdown()` и `barcodeScanner.close()` при размонтировании Composable.

#### 2. Утечка памяти ML Kit при выборе изображения из галереи
- **Файл**: [`AddEditCardViewModel.kt`](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/main/java/com/example/discountcards/ui/addedit/AddEditCardViewModel.kt)
- **Проблема**:
  ```kotlin
  val scanner = BarcodeScanning.getClient()
  scanner.process(inputImage)...
  ```
  При каждом распознавании фото из галереи создается новый экземпляр `BarcodeScanning.getClient()`, который не закрывается ни в `addOnSuccessListener`, ни в `addOnFailureListener`.
- **Решение**: Вызывать `scanner.close()` после завершения обработки изображения.

#### 3. Опасность потери данных при сбое миграции Room (`fallbackToDestructiveMigration`)
- **Файл**: [`AppDatabase.kt`](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/main/java/com/example/discountcards/data/local/AppDatabase.kt)
- **Проблема**:
  ```kotlin
  .fallbackToDestructiveMigration(dropAllTables = true)
  ```
  В случае отсутствия миграции между версиями базы данных Room без предупреждения полностью очистит базу данных пользователя и удалит все сохраненные дисконтные карты.
- **Решение**: Для приложения, хранящего пользовательские данные локально, рекомендуется убрать `fallbackToDestructiveMigration(dropAllTables = true)` или предусмотреть логику резервного копирования.

#### 4. Неэффективность и риск OutOfMemory в кэше штрихкодов (`LruCache`)
- **Файл**: [`BarcodeGenerator.kt`](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/main/java/com/example/discountcards/util/BarcodeGenerator.kt)
- **Проблема**:
  ```kotlin
  private val barcodeCache = LruCache<String, Bitmap>(100)
  ```
  Кэш ограничен количеством элементов (100 битмапов). Битмап размером 800x400 ARGB_8888 занимает ~1.28 МБ. 100 битмапов могут занять свыше 100 МБ Heap-памяти.
- **Решение**: Перевести `LruCache` на ограничение по размеру в килобайтах (`maxSize` в KB и переопределение `sizeOf`).

#### 5. Выполнение синхронного кода с базой данных на Main Thread во Widgеt Provider
- **Файл**: [`DiscountCardsWidgetProvider.kt`](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/main/java/com/example/discountcards/widget/DiscountCardsWidgetProvider.kt)
- **Проблема**:
  ```kotlin
  val card: DiscountCard? = runBlocking {
      app.database.discountCardDao().getCardByIdDirect(config.cardId)
  }
  ```
  Вызов `runBlocking` с обращением к Room прямо на главном потоке `BroadcastReceiver` может вызвать задержку интерфейса или ANR при высокой нагрузке на диск.
- **Решение**: Использовать `goAsync()` в `BroadcastReceiver` с исполнением корутины в `Dispatchers.IO`.

---

### 🟡 Средние и малые замечания (Logic & UI/UX Issues)

#### 6. Сброс состояния навигации при повороте экрана или смене языка
- **Файл**: [`MainActivity.kt`](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/main/java/com/example/discountcards/MainActivity.kt)
- **Проблема**: `backStack` инициализируется через `remember { mutableStateListOf(...) }`. При изменении конфигурации (поворот экрана, переключение языка через `recreate()`) стек навигации теряется и пользователь возвращается на главный экран.
- **Решение**: Использовать `rememberSaveable` со специальным `Saver` для стека `Destination`.

#### 7. Поведение яркости экрана по умолчанию в деталях карты
- **Файл**: [`CardDetailScreen.kt`](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/main/java/com/example/discountcards/ui/detail/CardDetailScreen.kt)
- **Проблема**: По умолчанию `cardBrightnessPercent = 100`. При каждом открытии детального экрана карты экран телефона автоматически переключается на 100% максимальную яркость, что может сслепить пользователя в темноте.
- **Решение**: Рекомендуется сделать базовым значением системную яркость (0 или -1) или позволить пользователю гибче управлять этим параметром.

#### 8. Необработанные жестко зашитые строки (Hardcoded Strings)
- **Файл**: [`DiscountCardsWidgetConfigureActivity.kt`](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/main/java/com/example/discountcards/widget/DiscountCardsWidgetConfigureActivity.kt)
- **Проблема**: Тексты `"Нет доступных карточек"`, `"Сортировка"`, `"Отображение и стиль"` зашиты строковыми литералами на русском языке вместо ресурсов `R.string.*`.
- **Решение**: Вынести их в `strings.xml` для всех поддерживаемых языков.

#### 9. Необновление поля `updatedAt` при увеличении счетчика использования карты
- **Файл**: [`DiscountCardDao.kt`](file:///E:/virtualenvs/discount_cards/DiscountCards/app/src/main/java/com/example/discountcards/data/local/dao/DiscountCardDao.kt)
- **Проблема**: Запрос `UPDATE discount_cards SET usageCount = usageCount + 1 WHERE id = :id` не обновляет метку времени `updatedAt`.

---

## 3. Проверка тестового покрытия (Test Coverage Audit)

### 📊 Текущий статус
- В проекте присутствуют **33 юнит-теста**, все они проходят успешно (`:app:testDebugUnitTest`).
- Покрыты основные сценарии в:
  - `CardRepositoryImplTest.kt` (импорт/экспорт, режимы MERGE/OVERWRITE, сортировка).
  - `AddEditCardViewModelTest.kt` (создание, редактирование, дубликаты, валидация).
  - `CardDetailViewModelTest.kt` (загрузка карты, удаление, обработка ошибок).
  - `CardListViewModelTest.kt` (поиск, фильтрация, выбор карт для экспорта).
  - `BackupManagerTest.kt` (сериализация, потоки).
  - `BarcodeGeneratorTest.kt` (кодирование штрихкодов, проверка контрольных сумм).

### ❌ Отсутствующие тесты (Missing Tests)

1. **`SettingsViewModelTest.kt`** — полностью отсутствует.
   - Не протестировано переключение темы, языков, сортировки, параметров отображения добавляемой кнопки и штрихкодов.
2. **`UserPreferencesRepositoryTest.kt`** — протестированы только дефолтные значения `UserPreferences`.
   - Не протестированы вызовы `getAppLanguageSynchronously()`, `getLocalizedContext()`, работа со `SharedPreferences` и `DataStore`.
3. **Сериализаторы (`Serializers.kt`)**:
   - Нет тестов для `ColorHexSerializer` (валидный hex, без `#`, невалидные строки, проверка альфа-канала).
   - Нет тестов для `DateIso8601Serializer` (формат ISO-8601, обработка парсинга сырого `Long` и обработка ошибок).
4. **Тесты виджетов (`WidgetPrefsTest.kt`)**:
   - Не протестировано сохранение, загрузка и удаление конфигурации виджетов в `SharedPreferences`.
5. **Тесты миграций БД Room (`AppDatabaseMigrationTest.kt`)**:
   - Отсутствуют интеграционные/юнит-тесты миграций `MIGRATION_1_2`, `MIGRATION_2_3`, `MIGRATION_3_4`.
6. **Инструментальные и UI-тесты (`androidTest`)**:
   - В `androidTest` находится только стандартный шаблонный файл `ExampleInstrumentedTest.kt`. Отсутствуют UI-тесты на Compose и навигацию.

---

## 4. Сводная таблица найденных проблем и приоритеты

| № | Компонент | Проблема | Приоритет |
|---|---|---|---|
| 1 | `CameraBarcodeScanner.kt` | Утечка `Executor` и `BarcodeScanner` при закрытии камеры | 🔴 Высокий |
| 2 | `AddEditCardViewModel.kt` | Утечка `BarcodeScanner` при выборе фото из галереи | 🔴 Высокий |
| 3 | `AppDatabase.kt` | Risk полной очистки БД при missing migration | 🔴 Высокий |
| 4 | `BarcodeGenerator.kt` | OOM риск из-за нелимитированного по памяти `LruCache` | 🟡 Средний |
| 5 | `DiscountCardsWidgetProvider.kt` | `runBlocking` обращение к БД на UI треде | 🟡 Средний |
| 6 | `MainActivity.kt` | Потеря стека навигации при повороте экрана / смене языка | 🟡 Средний |
| 7 | `DiscountCardsWidgetConfigureActivity.kt` | Захардкоженные русские строки без `strings.xml` | 🟢 Низкий |
| 8 | `SettingsViewModel` / `Serializers` | Отсутствие юнит-тестов | 🟡 Средний |
