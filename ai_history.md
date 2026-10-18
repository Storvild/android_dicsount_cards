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
   

