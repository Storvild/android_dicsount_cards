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
