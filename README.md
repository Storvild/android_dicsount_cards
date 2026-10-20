# DiscountCards

**DiscountCards** — это Android-приложение для хранения и использования карт лояльности. Позволяет вести список дисконтных карт, сканировать и генерировать штрих-коды, а также резервировать (экспортировать/импортировать) данные в формате JSON.

## Возможности

- **Список карт** — сетка карточек с сортировкой, поиском и избранными
- **Детали карты** — просмотр штрих-кода, заметок, номера карты и CVC
- **Сканирование штрих-кодов** — встроенная камера для быстрого сканирования (QR, EAN, Code 128 и др.)
- **Генерация штрих-кодов** — поддержка популярных форматов: `QR_CODE`, `CODE_128`, `EAN_13`, `EAN_8`, `UPC_A`, `CODE_39`, `PDF_417`, `AZTEC`, `PHONE`
- **Настройка цвета и иконки** — выбор цвета карточки и иконки категории
- **Избранное** — отметка любимых карт и сортировка
- **Счётчик использований** — количество показанных карт
- **Регулировка яркости экрана** — при просмотре штрих-кода (в тёмных местах)
- **Настройки темы** — светлая / тёмная / системная
- **Экспорт / Импорт** — резервное копирование в JSON, выбор режима дублирования (`MERGE`, `OVERWRITE`, `UPDATE_EXISTING`, `ADD_AS_NEW`, `SKIP`)
- **Поиск и сортировка** — по названию, номеру, дате создания и другим полям
- **Удаление** — отдельная карта или массовое удаление
- **Edge-to-edge** — адаптивный интерфейс с учётом системных панелей

## Технологический стек

| Компонент | Библиотека |
|---|---|
| UI | **Jetpack Compose** (Material 3) |
| Навигация | **Navigation 3** |
| БД | **Room** (SQLite) |
| Асинхронность | **Kotlin Coroutines + Flow** |
| Сеть | **Retrofit 3** + **OkHttp** + **Moshi** (де-/сериализация) |
| JSON | **kotlinx.serialization** |
| Изображения | **Coil** |
| Штрих-коды | **ML Kit Barcode Scanning**, **ZXing** |
| Камера | **CameraX** |
| Настройки | **DataStore Preferences** |
| Адаптивный UI | **Compose Adaptive** |
| Сборка | **Gradle + KSP** |
| Мин. SDK | **API 24** (Android 7.0) |
| Целевой SDK | **API 37** |

## Структура проекта

```
app/src/main/java/com/example/discountcards/
├── DiscountCardsApp.kt          # Application: DI-контейнер (БД, репозитории)
├── MainActivity.kt              # Точка входа, Navigation 3 (sealed interface Destination)
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt       # Room Database (version = 3, миграции)
│   │   ├── dao/DiscountCardDao.kt
│   │   └── entity/DiscountCard.kt
│   └── repository/
│       ├── CardRepository.kt        # Интерфейс
│       ├── CardRepositoryImpl.kt    # Реализация (Room + JSON backup)
│       └── UserPreferencesRepository.kt
├── ui/
│   ├── list/        # Список карт: экран, ViewModel, UiState, компоненты
│   ├── detail/      # Детали карты + показ штрих-кода
│   ├── addedit/     # Добавление/редактирование + сканер камеры
│   ├── settings/    # Настройки (тема, яркость, сортировка, экспорт/импорт)
│   └── theme/       # Цвета, типографика, DiscountCardsTheme
└── util/
    ├── BarcodeGenerator.kt   # Генерация штрих-кодов (ZXing) + кэш LRU
    ├── BackupManager.kt      # Сериализация/десериализация JSON
    └── ImportMode.kt         # MERGE / OVERWRITE и стратегии дублирования
```

## Архитектура

Приложение использует **MVVM + Clean Architecture**:

- **UI (Compose)** — экраны (`*Screen.kt`) подписываются на `UiState` из `ViewModel`
- **ViewModel** — `StateFlow<UiState>`, бизнес-логика UI, взаимодействие с репозиторием
- **Repository** — `CardRepository` (интерфейс) и `CardRepositoryImpl`; `UserPreferencesRepository` для настроек
- **Data Local** — Room (`AppDatabase`, `DiscountCardDao`, `DiscountCard`)
- **Util** — генерация штрих-кодов, бэкап JSON

Навигация реализована на **Navigation 3** через `sealed interface Destination` со стеком `mutableStateListOf<Destination>`; отменяющий жестикулирование обрабатывается через `BackHandler`.

## Сборка и запуск

```bash
# Отладочная сборка
./gradlew :app:assembleDebug

# Release (с R8-минификацией и shrinkResources)
./gradlew :app:assembleRelease

# Тесты
./gradlew :app:testDebugUnitTest
./gradlew :app:connectedAndroidTest
```

## Разрешения

| Разрешение | Назначение |
|---|---|
| `INTERNET` | Загрузка изображений (Coil) |
| `CAMERA` | Сканер штрих-кодов (необязательное — `uses-feature required="false"`) |

## Экспорт/Импорт

Резервное копирование — это JSON-файл со списком `DiscountCard`:

```json
[
  {
    "id": 1,
    "storeName": "ВкусВилл",
    "cardNumber": "1234567890",
    "barcodeFormat": "QR_CODE",
    "note": "",
    "cardColor": -15796827,
    "iconName": "",
    "isFavorite": false,
    "cvc": "",
    "usageCount": 0,
    "createdAt": 1730000000000,
    "updatedAt": 1730000000000
  }
]
```

При импорте доступны режимы:
- **MERGE** — добавить к текущим (по умолчанию)
- **OVERWRITE** — перезаписать всё

Стратегии обработки дубликатов:
- **UPDATE_EXISTING** — обновить карту с таким же именем
- **ADD_AS_NEW** — создать дубликат как новую карту
- **SKIP** — пропустить дубликат

## Лицензия

См. файл [LICENSE](file:///E:/virtualenvs/discount_cards/DiscountCards/LICENSE).
