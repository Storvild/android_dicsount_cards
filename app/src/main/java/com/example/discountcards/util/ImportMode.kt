package com.example.discountcards.util

enum class ImportMode {
    MERGE,      // "Добавить к текущим"
    OVERWRITE   // "Перезаписать всё"
}

enum class DuplicateStrategy {
    UPDATE_EXISTING, // Обновить карту с таким же именем
    ADD_AS_NEW,      // Создать новую карту (дубликат)
    SKIP             // Пропустить импорт дубликата
}

