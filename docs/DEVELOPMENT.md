# Документация для разработчиков (DiscountCards)

Данный документ содержит техническую информацию, необходимую для разработки, сборки и поддержки приложения **DiscountCards**.

---

## 🛠 Технологический стек

| Компонент | Технологии / Библиотеки |
|---|---|
| **Язык программирования** | Kotlin |
| **UI Framework** | Jetpack Compose, Material 3, Compose Adaptive |
| **Навигация** | Navigation 3 (`sealed interface Destination` + `rememberSaveable` + `Saver`) |
| **База данных** | Room (SQLite) + KSP |
| **Асинхронность** | Kotlin Coroutines + Flow (`StateFlow`, `collectAsStateWithLifecycle`) |
| **Хранение настроек** | DataStore Preferences |
| **Камера и ML** | CameraX + ML Kit Barcode Scanning |
| **Генерация штрих-кодов** | ZXing Core (с LRU-кэшированием) |
| **Сериализация** | kotlinx.serialization (JSON) |
| **Инструменты сборки** | Gradle (Kotlin DSL), KSP |
| **Минимальный SDK** | API 24 (Android 7.0) |
| **Целевой SDK** | API 37 |

---

## 🏛 Архитектура

Приложение построено в соответствии с рекомендациями Google по архитектуре Android-приложений (**MVVM + Layered Architecture**):

1. **UI Layer (Jetpack Compose)**:
   - Экраны (`*Screen.kt`) представлены декларативными Composable-функциями.
   - Состояние интерфейса описывается `UiState`-классами и отслеживается реактивно через `collectAsStateWithLifecycle()`.
   - Навигация построена на `sealed interface Destination` + `SnapshotStateList` с `rememberSaveable` и кастомным `Saver`.
2. **ViewModel Layer**:
   - `CardListViewModel`, `CardDetailViewModel`, `AddEditCardViewModel`, `SettingsViewModel`.
   - Каждый ViewModel создаётся через собственный `ViewModelProvider.Factory` (см. `ui/ViewModelFactory.kt`).
   - Предоставляет `StateFlow<UiState>` для UI.
   - Обрабатывает действия пользователя и передаёт вызовы в слой данных.
3. **Data Layer (Repository Pattern)**:
   - `CardRepository` / `CardRepositoryImpl` — единая точка доступа к картам лояльности (Room DB) и функциям бэкапа (JSON/CSV).
   - `UserPreferencesRepository` — управление пользовательскими настройками (тема, сортировка, язык, яркость, видимость элементов) через Android DataStore Preferences.
4. **DI (ленивая инициализация)**:
   - `DiscountCardsApp` (`Application`) — корневой контейнер. Инициирует `AppDatabase`, `CardRepository`, `UserPreferencesRepository` через `by lazy`.
   - В `MainActivity` зависимости извлекаются из `applicationContext as DiscountCardsApp`.

---

## 📂 Структура проекта

```
app/src/main/java/com/example/discountcards/
├── DiscountCardsApp.kt          # Application класс: ленивая инициализация БД, репозиториев
├── MainActivity.kt              # Главная Activity: Navigation 3, темы, локали, обработка Intent'ов (ACTION_VIEW/ACTION_SEND)
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt       # Room (версия 4, миграции MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
│   │   ├── dao/DiscountCardDao.kt
│   │   └── entity/DiscountCard.kt   # @Serializable + @Entity, кастомные сериализаторы
│   └── repository/
│       ├── CardRepository.kt        # Интерфейс репозитория карт
│       ├── CardRepositoryImpl.kt    # Реализация (Room + экспорт/импорт JSON/CSV)
│       └── UserPreferencesRepository.kt # Настройки (DataStore + SharedPreferences для locale)
├── ui/
│   ├── ViewModelFactory.kt      # 4 × ViewModelProvider.Factory (CardList, CardDetail, AddEditCard, Settings)
│   ├── list/                    # Главный экран: поиск, сортировка, массовое удаление, экспорт/импорт, Share
│   ├── detail/                  # Экран деталей: генерация/показ штрих-кода, яркость, архив/извлечение
│   ├── addedit/                 # Экран добавления/редактирования: цвет, иконка, формат, встроенный сканер
│   ├── archive/                 # Экран архива: поиск, список, извлечение, добавление
│   ├── settings/                # Настройки: язык, яркость, тема, сортировка, тумблеры видимости
│   └── theme/                   # Цветовые схемы Material 3, типографика, DiscountCardsTheme
├── util/
│   ├── BarcodeGenerator.kt      # Генерация битмапов штрих-кодов (ZXing) с LRU-кэшем
│   ├── BackupManager.kt         # Сериализация/десериализация в JSON и CSV
│   ├── Serializers.kt           # ColorHexSerializer, DateIso8601Serializer (кастомные KSerializer)
│   └── ImportMode.kt            # Стратегии импорта: MERGE/OVERWRITE + DuplicateStrategy
└── widget/
    ├── DiscountCardsWidgetProvider.kt       # AppWidget: 1×1–5×5, штрихкод/QR + название магазина
    ├── DiscountCardsWidgetConfigureActivity.kt # Экран конфигурации виджета
    └── WidgetPrefs.kt            # Хранение ID карты в SharedPreferences виджета
```

---

## 💾 Формат данных

Приложение поддерживает два формата резервного копирования: **JSON** и **CSV**.

### JSON

При экспорте в JSON данные карт сохраняются в текстовый JSON-файл:

```json
[
  {
    "id": 1,
    "storeName": "Пятёрочка",
    "cardNumber": "1234567890123",
    "barcodeFormat": "EAN_13",
    "note": "Карта семьи",
    "cardColor": "#0EE8A5",
    "iconName": "shopping_cart",
    "isFavorite": true,
    "cvc": "123",
    "usageCount": 5,
    "createdAt": "2026-01-01T10:10:10",
    "updatedAt": "2026-01-01T10:10:10",
    "isArchived": false
  }
]
```

Особенности сериализации:
- `cardColor` — HEX-строка `#RRGGBB` (или `#AARRGGBB` при alpha ≠ 255) благодаря `ColorHexSerializer`.
- `createdAt` / `updatedAt` — ISO-8601 строки (`yyyy-MM-dd'T'HH:mm:ss`) благодаря `DateIso8601Serializer`.

### CSV

При экспорте в CSV создаётся таблица с заголовком `Название,Код,Тип кода,CVC,Примечание`.
При импорте определяется разделитель (`,` / `;` / `\t`) по преобладанию, а также распознаётся строка заголовка (поддерживаются русские и английские ключевые слова).

---

## 📥 Открытие JSON-файлов («Открыть с помощью»)

`MainActivity` регистрирует intent-фильтры для `ACTION_VIEW` и `ACTION_SEND` с MIME-типами `application/json`, `text/json`, `text/plain`, `*/*` и паттернами `*.json` / `*.JSON`. При открытии такого файла из файлового менеджера запускается `MainActivity` с `Uri` в `intent.data` (для `ACTION_SEND` — в `EXTRA_STREAM`), что автоматически открывает диалог импорта карт.

---

## 🚀 Сборка и запуск

### Предварительные требования
- Android Studio Ladybug (2024.2.1) или новее
- JDK 17 (рекомендуется; в CI используется Temurin 17)
- Android SDK 37 (Compile / Target SDK)

### Команды Gradle

```bash
# Сборка отладочного APK
./gradlew :app:assembleDebug

# Сборка релизного APK (с R8 оптимизацией и сжатием ресурсов)
./gradlew :app:assembleRelease

# Запуск юнит-тестов
./gradlew :app:testDebugUnitTest

# Запуск инструментальных тестов
./gradlew :app:connectedAndroidTest
```
