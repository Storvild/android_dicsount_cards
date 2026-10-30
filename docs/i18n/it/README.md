# DiscountCards

**DiscountCards** è un'app Android per archiviare, scansionare e utilizzare comodamente le tue tessere di fidelizzazione e di sconto. L'app ti consente di conservare tutte le tue tessere di sconto in un unico luogo, di scansionarle rapidamente alla cassa mediante un generatore di codici a barre e di creare backup dei tuoi dati.

> [!IMPORTANT]
> **Vantaggi principali:**
> - ⚡️ **Avvio istantaneo** — l'app si apre in una frazione di secondo ed è subito pronta all'uso, senza tempi di caricamento.
> - 🧩 **Accesso rapido al codice a barre tramite widget** — aggiungi un widget con il codice a barre scelto alla schermata home e mostralo all'operatore **senza aprire l'app**.
> - 📤 **Esportazione e importazione dei dati** — crea backup di tutte le tue tessere in formato JSON o CSV e ripristinali su qualsiasi dispositivo, oppure condividi le tessere con altri.
> - 🚫 **Nessuna pubblicità** — l'app è completamente autonoma e non contiene pubblicità né tracker.

> [!TIP]
> **Scarica l'app:**
> - 📦 **Scarica l'APK della release:** [GitHub Releases](https://github.com/Storvild/android_discount_cards/releases/latest)

---

## 📸 Screenshot

| Schermata principale | Schermata principale | Dettagli tessera | Aggiunta / Scanner |
| :---: | :---: |:---------------------------------------------------------------------------:| :---: |
| <img src="../../screenshots/main_with_barcodes.jpg" width="220" alt="Schermata principale con codici a barre"> | <img src="../../screenshots/main_compact.jpg" width="220" alt="Schermata principale compatta"> | <img src="../../screenshots/card_detail.jpg" width="220" alt="Dettagli tessera"> | <img src="../../screenshots/add_edit_card.jpg" width="220" alt="Aggiunta tessera"> |

| Impostazioni | Esporta tessere | Importa tessere | Ordinamento tessere |
| :---: | :---: | :---: |:----------------------------------------------------------------------------------------------------------:|
| <img src="../../screenshots/settings.jpg" width="220" alt="Impostazioni"> | <img src="../../screenshots/export_cards.jpg" width="220" alt="Esporta tessere"> | <img src="../../screenshots/import_cards.jpg" width="220" alt="Importa tessere"> | <img src="../../screenshots/sort_cards_light_theme.jpg" width="220" alt="Ordinamento tessere con tema chiaro"> |

---

## 📱 Funzionalità principali

- 💳 **Gestione delle tessere di fidelizzazione** — aggiunta, modifica e cancellazione individuale o in blocco delle tessere.
- 📷 **Scansione e selezione del codice a barre da un file** — scansione in tempo reale delle tessere fisiche tramite fotocamera (CameraX + ML Kit) o selezione di un'immagine di codice a barre dalla galleria/dal file.
- 🔢 **Supporto per molti formati di codice a barre** — generazione di codici a barre visivi con la libreria ZXing:
  - `QR_CODE`
  - `CODE_128`
  - `EAN_13` / `EAN_8`
  - `UPC_A`
  - `CODE_39`
  - `PDF_417`
  - `AZTEC`
  - `PHONE` (sconto per numero di telefono — visualizzato come testo, non generato come codice a barre)
- 🎨 **Personalizzazione** — scelta del colore della tessera da una palette pronta o di un colore libero tramite palette HSV e codice HEX (`#RRGGBB`), scelta di un'icona di categoria, aggiunta di note e codici CVC/CVV.
- ⭐️ **Preferiti e statistiche** — segnalazione delle tessere usate frequentemente, conteggio automatico del numero di visualizzazioni.
- 🔍 **Ricerca intelligente** — ricerca rapida per nome del negozio, numero della tessera o note.
- 🔀 **Ordinamento delle tessere** — ordinamento pratico dell'elenco per nome, data di creazione o frequenza d'uso.
- ⚡️ **Modalità elenco compatta** — impostazione «Mostra i codici a barre nell'elenco» (attiva di default). Disattivandola si disattiva la generazione di codici a barre e numeri di tessera nella schermata principale, riducendo a metà l'altezza degli elementi e accelerando sensibilmente il caricamento e lo scorrimento.
- 💡 **Controllo della luminosità dello schermo** — impostazione automatica del livello di luminosità scelto all'apertura di una tessera, oltre alla possibilità di una rapida regolazione direttamente sulla schermata della tessera tramite un cursore integrato (attivata dall'opzione «Mostra la luminosità nella tessera» nelle impostazioni, disattivata di default).
- 📤 **Condivisione delle tessere** — possibilità di condividere le tessere selezionate con altri utenti.
- 🌐 **Supporto lingue** — selezione separata della lingua dell'interfaccia nelle impostazioni dell'app (supportate 11 lingue).
- 🌗 **Temi** — supporto per tema chiaro, scuro e di sistema.
- 📦 **Backup (Esporta / Importa)** — salvataggio dei dati in formato **JSON** o **CSV**:
  - Modalità di importazione: **MERGE** (Unisci) o **OVERWRITE** (Sovrascrivi).
  - Strategie flessibili per i duplicati: *Aggiorna esistenti*, *Aggiungi come nuovi*, *Salta*.
  - 📂 **Apertura dei file di backup («Apri con»)** — supporto per l'apertura diretta o l'invio (`ACTION_VIEW`, `ACTION_SEND`) di file (JSON) dai gestori di file, con avvio automatico della finestra di dialogo di importazione delle tessere.
- 🗃️ **Archivio delle tessere** — spostamento delle tessere non utilizzate in una sezione separata:
  - Pulsante **«Sposta in archivio»** sulla schermata di dettagli di una tessera attiva e **«Estrai dall'archivio»** sulla schermata di dettagli di una tessera archiviata.
  - Voce **«Archivio»** nel menu a discesa della schermata principale che apre una schermata separata con ricerca, elenco e stato vuoto.
  - Le tessere archiviate sono nascoste dall'elenco principale, dalla ricerca, dall'esportazione e dalla condivisione (Share).
- 🧩 **Widget sulla schermata home** — widget ridimensionabile (da 1x1 a 5x5) con scelta della tessera, codice a barre/QR e nome del negozio, pulsante di modifica dei parametri e apertura rapida della tessera al tap.
- 👆 **Menu contestuale con pressione prolungata** — accesso rapido alle azioni «Modifica» e «Elimina» con pressione prolungata su una tessera nell'elenco.
- 📱 **UI Edge-to-Edge** — interfaccia moderna e adattiva che tiene conto dei pannelli di sistema, basata su Material 3 e Jetpack Compose Adaptive.

---

## 🌐 Lingue supportate
- **Russo**
- **Inglese (English)**
- **Francese (Français)**
- **Tedesco (Deutsch)**
- **Giapponese (日本語)**
- **Spagnolo (Español)**
- **Portoghese (Português)**
- **Hindi (हिन्दी)**
- **Arabo (العربية)**
- **Turco (Türkçe)**
- **Cinese semplificato (简体中文)**

---

## 💾 Backup e formato dati

L'app supporta due formati: **JSON** e **CSV**.

### JSON

```json
[
  {
    "id": 1,
    "storeName": "Pyaterochka",
    "cardNumber": "1234567890123",
    "barcodeFormat": "EAN_13",
    "note": "Tessera di famiglia",
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

Tabella con l'intestazione `Nome,Codice,TipodiCodice,CVC,Nota`:

```csv
Nome,Codice,TipodiCodice,CVC,Nota
Pyaterochka,1234567890123,EAN_13,123,Tessera di famiglia
Magnit,9876543210,QR_CODE,,
```

### Modalità di importazione:
- **MERGE (Unisci)** — aggiunge le tessere del file a quelle già presenti nel database.
- **OVERWRITE (Sovrascrivi)** — sostituisce completamente le tessere attuali con i dati del file.

### Strategie di risoluzione dei conflitti per le tessere duplicate:
- **UPDATE_EXISTING (Aggiorna esistenti)** — aggiorna le informazioni della tessera.
- **ADD_AS_NEW (Aggiungi come nuovi)** — crea una nuova tessera duplicata.
- **SKIP (Salta)** — salta il duplicato.

---

## 🔒 Privacy e autonomia

**DiscountCards** è un'app **completamente offline**. Non **richiede** il permesso di accesso a Internet (`android.permission.INTERNET`).

**Cosa offre all'utente:**
- 🛡 **Privacy assoluta:** tutti i dati delle tue tessere, la cronologia d'uso e i backup sono salvati localmente sul tuo dispositivo. Nulla viene inviato a server di terze parti.
- 🚫 **Nessuna pubblicità:** senza Internet è fisicamente impossibile caricare e mostrare pubblicità o tracker.
- 🔋 **Risparmio della batteria:** nessuna sincronizzazione nascosta in background né richieste di rete che consumano batteria e dati mobili.
- ✈️ **Indipendenza dalla rete:** le tessere vengono caricate istantaneamente e generano codici a barre ovunque: anche nelle casse in cemento degli ipermercati o nei seminterrati senza copertura.

## 📋 Permessi richiesti

L'app richiede un solo accesso, e anche quello su richiesta:

| Permesso | Finalità |
|---|---|
| `android.permission.CAMERA` | Scansione dei codici a barre delle tessere fisiche (facoltativo, richiesto solo al momento della scansione, `uses-feature required="false"`). |

---

## 🛠 Per gli sviluppatori

Le informazioni tecniche su architettura, stack tecnologico, struttura del progetto, formato di salvataggio dei dati e comandi di build sono state trasferite in un documento separato:

👉 [**Documentazione per gli sviluppatori**](../../DEVELOPMENT.md)

---

## 📄 Licenza

Il progetto è distribuito ai termini della licenza specificata nel file [LICENSE](../../../LICENSE).
