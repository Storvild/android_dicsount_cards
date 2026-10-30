# DiscountCards

**DiscountCards** is an Android app for conveniently storing, scanning and using loyalty and discount cards. The app lets you keep all your discount cards in one place, quickly scan them at the checkout using a barcode generator, and back up your data.

> [!IMPORTANT]
> **Key advantages:**
> - ⚡️ **Instant launch** — the app opens in a fraction of a second and is ready to use immediately after launch, without any loading wait.
> - 🧩 **Quick barcode access via a widget** — add a widget with the selected barcode to your home screen and show it to the cashier **without opening the app itself**.
> - 📤 **Data export and import** — back up all your cards in JSON or CSV format and restore them on any device, or share cards with others.
> - 🚫 **No ads** — the app is fully self-contained and contains no ads or tracking.

> [!TIP]
> **Download the app:**
> - 📦 **Download release APK:** [GitHub Releases](https://github.com/Storvild/android_discount_cards/releases/latest)

---

## 📸 Screenshots

| Main screen | Main screen | Card details | Add / Scanner |
| :---: | :---: |:---------------------------------------------------------------------------:| :---: |
| <img src="../../screenshots/01_main_with_barcodes.jpg" width="220" alt="Main screen with barcodes"> | <img src="../../screenshots/02_main_compact.jpg" width="220" alt="Compact main screen"> | <img src="../../screenshots/03_card_detail.jpg" width="220" alt="Card details"> | <img src="../../screenshots/04_add_edit_card.jpg" width="220" alt="Adding a card"> |

| Settings | Export cards | Import cards | Card sorting |
| :---: | :---: | :---: |:----------------------------------------------------------------------------------------------------------:|
| <img src="../../screenshots/05_settings.jpg" width="220" alt="Settings"> | <img src="../../screenshots/06_export_cards.jpg" width="220" alt="Exporting cards"> | <img src="../../screenshots/07_import_cards.jpg" width="220" alt="Importing cards"> | <img src="../../screenshots/08_sort_cards_light_theme.jpg" width="220" alt="Card sorting with light theme"> |

| Menu | Settings 2 | Widget 1 | Widget 2 |
| :---: | :---: | :---: |:----------------------------------------------------------------------------------------------------------:|
| <img src="../../screenshots/09_menu.jpg" width="220" alt="Menu"> | <img src="../../screenshots/10_settings2.jpg" width="220" alt="Settings 2"> | <img src="../../screenshots/11_widget1.jpg" width="220" alt="Widget 1"> | <img src="../../screenshots/12_widget2.jpg" width="220" alt="Widget 2"> |

---

## 📱 Main features

- 💳 **Loyalty card management** — add, edit, and delete cards individually or in bulk.
- 📷 **Barcode scanning and barcode selection from a file** — real-time scanning of physical cards with the camera (CameraX + ML Kit) or selecting a barcode image from the gallery/file.
- 🔢 **Support for multiple barcode formats** — generation of visual barcodes using the ZXing library:
  - `QR_CODE`
  - `CODE_128`
  - `EAN_13` / `EAN_8`
  - `UPC_A`
  - `CODE_39`
  - `PDF_417`
  - `AZTEC`
  - `PHONE` (discount by phone number — displayed as text, not generated as a barcode)
- 🎨 **Personalization** — choose a card color from a ready-made palette or a custom color via an HSV palette and a HEX code (`#RRGGBB`), pick a category icon, add notes and CVC/CVV codes.
- ⭐️ **Favorites and statistics** — mark frequently used cards and automatically track the number of views.
- 🔍 **Smart search** — quick search by store name, card number or notes.
- 🔀 **Card sorting** — convenient sorting of the list by name, creation date or frequency of use.
- ⚡️ **Compact list mode** — a “Show barcodes in the list” setting (enabled by default). Disabling it turns off barcode and card number generation on the main screen, halves the height of list items and significantly speeds up loading and scrolling.
- 💡 **Screen brightness control** — automatic setting of the selected brightness level when a card is opened, plus quick adjustment right on the card screen with a built-in slider (enabled by the “Show brightness on card” option in settings, disabled by default).
- 📤 **Card sharing** — the ability to share selected cards with other users.
- 🌐 **Language support** — separate selection of the interface language in the app settings (11 languages supported).
- 🌗 **Themes** — support for light, dark and system themes.
- 📦 **Backup (Export / Import)** — saving data in **JSON** or **CSV** format:
  - Import modes: **MERGE** or **OVERWRITE**.
  - Flexible duplicate-handling strategies: *Update existing*, *Add as new*, *Skip*.
  - 📂 **Opening backup files (“Open with”)** — support for direct opening or sending (`ACTION_VIEW`, `ACTION_SEND`) of (JSON) files from file managers with automatic launching of the card import dialog.
- 🗃️ **Card archive** — moving unused cards to a separate section:
  - A **“Move to archive”** button on the active card details screen and an **“Extract from archive”** button on the archived card details screen.
  - An **“Archive”** item in the main screen’s drop-down menu opens a separate screen with search, a list and an empty state.
  - Archived cards are hidden from the main list, search, export and sharing (Share).
- 🧩 **Home screen widgets** — a resizable widget (from 1x1 to 5x5) with card selection, barcode/QR and store name, a settings edit button and a quick card open on tap.
- 👆 **Long-press context menu** — quick access to “Edit” and “Delete” actions on a long press of a card in the list.
- 📱 **Edge-to-Edge UI** — a modern adaptive interface accounting for system panels, based on Material 3 and Jetpack Compose Adaptive.

---

## 🌐 Supported languages
- **Russian**
- **English**
- **French (Français)**
- **German (Deutsch)**
- **Japanese (日本語)**
- **Spanish (Español)**
- **Portuguese (Português)**
- **Hindi (हिन्दी)**
- **Arabic (العربية)**
- **Turkish (Türkçe)**
- **Simplified Chinese (简体中文)**

---

## 💾 Backup and data format

The app supports two formats: **JSON** and **CSV**.

### JSON

```json
[
  {
    "id": 1,
    "storeName": "Pyaterochka",
    "cardNumber": "1234567890123",
    "barcodeFormat": "EAN_13",
    "note": "Family card",
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

### CSV

A table with the header `Name,Code,CodeType,CVC,Note`:

```csv
Name,Code,CodeType,CVC,Note
Pyaterochka,1234567890123,EAN_13,123,Family card
Magnit,9876543210,QR_CODE,,
```

### Import modes:
- **MERGE** — adds cards from the file to those already present in the database.
- **OVERWRITE** — fully replaces the current cards with data from the file.

### Conflict resolution strategies for duplicate cards:
- **UPDATE_EXISTING** — updates information about the card.
- **ADD_AS_NEW** — creates a new duplicate card.
- **SKIP** — skips the duplicate.

---

## 🔒 Privacy and self-containment

**DiscountCards** is a fully **offline app**. It fundamentally **does not request** the internet access permission (`android.permission.INTERNET`).

**What this gives the user:**
- 🛡 **Absolute privacy:** All your card data, usage history and backups are stored locally on your device. Nothing is sent to third-party servers.
- 🚫 **No ads:** without the internet it is physically impossible to load and show ads or tracking.
- 🔋 **Battery saving:** there are no hidden background synchronizations or network requests that drain the battery and mobile data.
- ✈️ **Independence from the network:** cards are loaded instantly and generate barcodes anywhere: even in the concrete boxes of hypermarkets or basements without cell coverage.

## 📋 Required permissions

The app requires only one access, and even that on request:

| Permission | Purpose |
|---|---|
| `android.permission.CAMERA` | Scanning barcodes from physical cards (optional, requested only at the moment of scanning, `uses-feature required="false"`). |

---

## 🛠 For developers

Technical information about the architecture, the tech stack, project structure, the data storage format and build commands has been moved to a separate document:

👉 [**Developer documentation**](../../DEVELOPMENT.md)

---

## 📄 License

The project is distributed under the terms of the license specified in the [LICENSE](../../../LICENSE) file.
