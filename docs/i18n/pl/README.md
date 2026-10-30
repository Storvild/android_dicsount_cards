# DiscountCards

**DiscountCards** to aplikacja Android do wygodnego przechowywania, skanowania i używania kart lojalnościowych oraz kart rabatowych. Aplikacja pozwala przechować wszystkie Twoje karty rabatowe w jednym miejscu, szybko skanować ich kasy za pomocą generatora kodów kreskowych i tworzyć kopie zapasowe danych.

> [!IMPORTANT]
> **Kluczowe zalety:**
> - ⚡️ **Natychmiastowy start** — aplikacja otwiera się ułamek sekundy i jest gotowa do pracy zaraz po starcie, bez oczekiwania na załadowanie.
> - 🧩 **Szybki dostęp do kodu kreskowego przez widget** — dodaj widget z wybranym kodem kreskowym na ekran główny i pokaż go kasjerowi, **nie otwierając samej aplikacji**.
> - 📤 **Eksport i import danych** — tworzenie kopii zapasowych wszystkich kart w formacie JSON lub CSV i przywracanie ich na dowolnym urządzeniu, a także dzielenie kart z innymi.
> - 🚫 **Bez reklam** — aplikacja jest w pełni samowystarczalna i nie zawiera żadnych reklam ani trackerów.

> [!TIP]
> **Pobierz aplikację:**
> - 📦 **Pobierz APK release'u:** [GitHub Releases](https://github.com/Storvild/android_discount_cards/releases/latest)

---

## 📸 Zrzuty ekranu

| Ekran główny | Ekran główny | Szczegóły karty | Dodawanie / Skaner |
| :---: | :---: |:---------------------------------------------------------------------------:| :---: |
| <img src="../../screenshots/01_main_with_barcodes.jpg" width="220" alt="Ekran główny z kodami kreskowymi"> | <img src="../../screenshots/02_main_compact.jpg" width="220" alt="Kompaktowy ekran główny"> | <img src="../../screenshots/03_card_detail.jpg" width="220" alt="Szczegóły karty"> | <img src="../../screenshots/04_add_edit_card.jpg" width="220" alt="Dodawanie karty"> |

| Ustawienia | Eksport kart | Import kart | Sortowanie kart |
| :---: | :---: | :---: |:----------------------------------------------------------------------------------------------------------:|
| <img src="../../screenshots/05_settings.jpg" width="220" alt="Ustawienia"> | <img src="../../screenshots/06_export_cards.jpg" width="220" alt="Eksport kart"> | <img src="../../screenshots/07_import_cards.jpg" width="220" alt="Import kart"> | <img src="../../screenshots/08_sort_cards_light_theme.jpg" width="220" alt="Sortowanie kart z jasnym motywem"> |

| Menu | Ustawienia 2 | Widget 1 | Widget 2 |
| :---: | :---: | :---: |:----------------------------------------------------------------------------------------------------------:|
| <img src="../../screenshots/09_menu.jpg" width="220" alt="Menu"> | <img src="../../screenshots/10_settings2.jpg" width="220" alt="Ustawienia 2"> | <img src="../../screenshots/11_widget1.jpg" width="220" alt="Widget 1"> | <img src="../../screenshots/12_widget2.jpg" width="220" alt="Widget 2"> |

---

## 📱 Główne funkcje

- 💳 **Zarządzanie kartami lojalnościowymi** — dodawanie, edycja i usuwanie kart indywidualnie lub masowo.
- 📷 **Skanowanie i wybieranie kodu kreskowego z pliku** — skanowanie kart fizycznych w czasie rzeczywistym z kamerą (CameraX + ML Kit) lub wybieranie obrazu kodu kreskowego z galerii/pliku.
- 🔢 **Obsługa wielu formatów kodów kreskowych** — generowanie kodów kreskowych za pomocą biblioteki ZXing:
  - `QR_CODE`
  - `CODE_128`
  - `EAN_13` / `EAN_8`
  - `UPC_A`
  - `CODE_39`
  - `PDF_417`
  - `AZTEC`
  - `PHONE` (rabat z numeru telefonu — wyświetlany jako tekst, nie generowany jako kod kreskowy)
- 🎨 **Personalizacja** — wybór koloru karty z gotowej palety lub dowolnego koloru przez paletę HSV i kod HEX (`#RRGGBB`), wybór ikony kategorii, dodawanie notatek i kodów CVC/CVV.
- ⭐️ **Ulubione i statystyki** — oznaczanie często używanych kart, automatyczne liczenie liczby wyświetleń.
- 🔍 **Inteligentne wyszukiwanie** — szybkie wyszukiwanie po nazwie sklepu, numerze karty lub notatkach.
- 🔀 **Sortowanie kart** — wygodne sortowanie listy po nazwie, dacie utworzenia lub częstotliwości użycia.
- ⚡️ **Kompaktowy tryb listy** — ustawienie „Pokazuj kody kreskowe w liście” (domyślnie włączone). Wyłączenie go wyłącza generowanie kodów kreskowych i numerów kart na ekranie głównym, zmniejsza wysokość elementów listy o połowę i znacząco przyspiesza ładowanie i przewijanie.
- 💡 **Kontrola jasności ekranu** — automatyczne ustawienie wybranego poziomu jasności przy otwarciu karty, a także możliwość szybkiego dostosowania bezpośrednio na ekranie karty za pomocą wbudowanego suwaka (włączane opcją „Pokaż jasność na karcie” w ustawieniach, domyślnie wyłączone).
- 📤 **Współdzielenie kart** — możliwość udostępnienia wybranych kart innym użytkownikom.
- 🌐 **Obsługa języków** — osobny wybór języka interfejsu w ustawieniach aplikacji (obsługa 11 języków).
- 🌗 **Motywy** — obsługa motywu jasnego, ciemnego i systemowego.
- 📦 **Kopia zapasowa (Eksport / Import)** — zapisywanie danych w formacie **JSON** lub **CSV**:
  - Tryby importu: **MERGE** (Scal) lub **OVERWRITE** (Nadpisz).
  - Elastyczne strategie obsługi duplikatów: *Zaktualizuj istniejące*, *Dodaj jako nowe*, *Pomiń*.
  - 📂 **Otwieranie plików kopii zapasowej („Otwórz za pomocą”)** — obsługa bezpośredniego otwierania lub wysyłania (`ACTION_VIEW`, `ACTION_SEND`) plików (JSON) z menedżerów plików z automatycznym uruchomieniem okna importu kart.
- 🗃️ **Archiwum kart** — przenoszenie nieużywanych kart do osobnej sekcji:
  - Przycisk **„Przenieś do archiwum”** na ekranie szczegółów aktywnej karty i **„Wydobyj z archiwum”** na ekranie szczegółów karty z archiwum.
  - Pozycja **„Archiwum”** w menu rozwijanym ekranu głównego otwiera osobny ekran z wyszukiwaniem, listą i pustym stanem.
  - Karty z archiwum są ukryte z głównej listy, wyszukiwania, eksportu i współdzielenia (Share).
- 🧩 **Widgety na ekranie głównym** — rozszerzalny widget (od 1x1 do 5x5) z wyborem karty, kodem kreskowym/QR i nazwą sklepu, przyciskiem edycji parametrów i szybkim otwarciem karty po dotknięciu.
- 👆 **Menu kontekstowe przy długim przytrzymaniu** — szybki dostęp do akcji „Edytuj” i „Usuń” przy długim przytrzymaniu karty w liście.
- 📱 **Interfejs Edge-to-Edge** — nowoczesny, adaptacyjny interfejs uwzględniający paski systemowe, oparty na Material 3 i Jetpack Compose Adaptive.

---

## 🌐 Obsługiwane języki
- **Rosyjski**
- **Angielski (English)**
- **Francuski (Français)**
- **Niemiecki (Deutsch)**
- **Japoński (日本語)**
- **Hiszpański (Español)**
- **Portugalski (Português)**
- **Hindi (हिन्दी)**
- **Arabski (العربية)**
- **Turecki (Türkçe)**
- **Chiński uproszczony (简体中文)**

---

## 💾 Kopia zapasowa i format danych

Aplikacja obsługuje dwa formaty: **JSON** i **CSV**.

### JSON

```json
[
  {
    "id": 1,
    "storeName": "Pyaterochka",
    "cardNumber": "1234567890123",
    "barcodeFormat": "EAN_13",
    "note": "Karta rodzinna",
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

Tabela z nagłówkiem `Nazwa,Kod,TypKodu,CVC,Notatka`:

```csv
Nazwa,Kod,TypKodu,CVC,Notatka
Pyaterochka,1234567890123,EAN_13,123,Karta rodzinna
Magnit,9876543210,QR_CODE,,
```

### Tryby importu:
- **MERGE (Scal)** — dodawanie kart z pliku do istniejących w bazie.
- **OVERWRITE (Nadpisz)** — całkowite zastąpienie bieżących kart danymi z pliku.

### Strategie rozwiązywania konfliktów przy duplikatach kart:
- **UPDATE_EXISTING (Zaktualizuj istniejące)** — aktualizacja informacji o karcie.
- **ADD_AS_NEW (Dodaj jako nowe)** — utworzenie nowej karty-duplikatu.
- **SKIP (Pomiń)** — pominięcie duplikatu.

---

## 🔒 Prywatność i samowystarczalność

**DiscountCards** to aplikacja **całkowicie offline**. Z zasady **nie prosi** o uprawnienie dostępu do Internetu (`android.permission.INTERNET`).

**Co to daje użytkownikowi:**
- 🛡 **Absolutna prywatność:** wszystkie dane o Twoich kartach, historia użycia i kopie zapasowe są przechowywane lokalnie na Twoim urządzeniu. Nic nie jest wysyłane na obce serwery.
- 🚫 **Brak reklam:** bez Internetu fizycznie nie da się ładować i wyświetlać reklam ani trackerów.
- 🔋 **Oszczędność baterii:** brak ukrytych synchronizacji w tle ani zapytań sieciowych, które zużywają baterię i ruch mobilny.
- ✈️ **Niezależność od sieci:** karty są natychmiast ładowane i generują kody kreskowe wszędzie: nawet w betonowych skrzyniach supermarketów lub piwnicach bez zasięgu.

## 📋 Wymagane uprawnienia

Aplikacja wymaga tylko jednego dostępu, i to na żądanie:

| Uprawnienie | Cel |
|---|---|
| `android.permission.CAMERA` | Skanowanie kodów kreskowych z kart fizycznych (opcjonalne, proszone tylko w momencie skanowania, `uses-feature required="false"`). |

---

## 🛠 Dla programistów

Informacje techniczne o architekturze, stosie technologicznym, strukturze projektu, formacie przechowywania danych i poleceniach buildu przeniesiono do osobnego dokumentu:

👉 [**Dokumentacja dla programistów**](../../DEVELOPMENT.md)

---

## 📄 Licencja

Projekt jest dystrybuowany na warunkach licencji określonej w pliku [LICENSE](../../../LICENSE).
