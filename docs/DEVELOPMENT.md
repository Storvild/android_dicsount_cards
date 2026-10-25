# Документация для разработчиков (DiscountCards)

Данный документ содержит техническую информацию, необходимую для разработки, сборки и поддержки приложения **DiscountCards**.

---

## 🛠 Технологический стек

| Компонент | Технологии / Библиотеки |
|---|---|
| **Язык программирования** | Kotlin |
| **UI Framework** | Jetpack Compose, Material 3, Compose Adaptive |
| **Навигация** | Navigation 3 (`sealed interface Destination`, stack state) |
| **База данных** | Room (SQLite) + KSP |
| **Асинхронность** | Kotlin Coroutines + Flow (`StateFlow`, `collectAsStateWithLifecycle`) |
| **Хранение настроек** | DataStore Preferences |
| **Камера и ML** | CameraX + ML Kit Barcode Scanning |
| **Генерация штрих-кодов** | ZXing Core (с LRU-кэшированием) |
| **Загрузка изображений** | Coil Compose |
| **Сеть и JSON** | Retrofit 3 + OkHttp + Moshi + kotlinx.serialization |
| **Инструменты сборки** | Gradle (Kotlin DSL), KSP |
| **Минимальный SDK** | API 24 (Android 7.0) |
| **Целевой SDK** | API 37 |

---

## 🏛 Архитектура

Приложение построено в соответствии с рекомендациями Google по архитектуре Android-приложений (**MVVM + Clean / Layered Architecture**):

1. **UI Layer (Jetpack Compose)**:
   - Экраны (`*Screen.kt`) представлены декларативными Composable-функциями.
   - Состояние интерфейса описывается `UiState` и отслеживается реактивно через `collectAsStateWithLifecycle()`.
2. **ViewModel Layer**:
   - Предоставляет `StateFlow<UiState>` для UI.
   - Обрабатывает действия пользователя и передаёт вызовы в слой данных.
3. **Data Layer (Repository Pattern)**:
   - `CardRepositoryImpl` предоставляет единую точку доступа к картам лояльности (Room DB) и функциям бэкапа.
   - `UserPreferencesRepository` управляет пользовательскими настройками через Android DataStore Preferences.

---

## 📂 Структура проекта

```
app/src/main/java/com/example/discountcards/
├── DiscountCardsApp.kt          # Application класс, DI-контейнер
├── MainActivity.kt              # Главная Activity, обработка Navigation 3 и тем
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt       # База данных Room (версия 4, миграции MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
│   │   ├── dao/DiscountCardDao.kt
│   │   └── entity/DiscountCard.kt
│   └── repository/
│       ├── CardRepository.kt        # Интерфейс репозитория карт
│       ├── CardRepositoryImpl.kt    # Реализация (Room + экспорт/импорт JSON)
│       └── UserPreferencesRepository.kt # Управление настройками DataStore
├── ui/
│   ├── list/        # Главный экран списка карт (поиск, сортировка, массовое удаление, экспорт/импорт)
│   ├── detail/      # Экран деталей карты (генерация и крупный показ штрих-кода, яркость, архив/извлечение)
│   ├── addedit/     # Экран добавления/редактирования карты (выбор цвета, иконки, встроенный сканер)
│   ├── archive/     # Экран архива (поиск, список архивных карт, извлечение, добавление)
│   ├── settings/    # Настройки приложения (тема, язык, яркость, показ кнопки «Добавить», показ штрих-кодов в списке)
│   └── theme/       # Цветовые схемы Material 3, типографика, DiscountCardsTheme
└── util/
    ├── BarcodeGenerator.kt   # Генерация битмапов штрих-кодов (ZXing) с LRU-кэшем
    ├── BackupManager.kt      # Сериализация и десериализация карт в JSON
    └── ImportMode.kt         # Стратегии импорта данных
```

---

## 💾 Формат данных JSON

При экспорте данные карт сохраняются в текстовый JSON-файл:

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

---

## 🚀 Сборка и запуск

### Предварительные требования
- Android Studio Ladybug (2024.2.1) или новее
- JDK 11 / 17
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
