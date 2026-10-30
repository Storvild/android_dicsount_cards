# DiscountCards

**DiscountCards** est une application Android pour stocker, scanner et utiliser facilement vos cartes de fidélité et cartes de réduction. L'application vous permet de conserver toutes vos cartes de réduction à un seul endroit, de les scanner rapidement à la caisse grâce à un générateur de codes-barres et de créer des sauvegardes de vos données.

> [!IMPORTANT]
> **Avantages clés :**
> - ⚡️ **Démarrage instantané** — l'application s'ouvre en une fraction de seconde et est prête à l'emploi immédiatement après le lancement, sans attente de chargement.
> - 🧩 **Accès rapide au code-barres via un widget** — ajoutez un widget avec le code-barres sélectionné sur votre écran d'accueil et présentez-le au caissier **sans ouvrir l'application elle-même**.
> - 📤 **Exportation et importation des données** — créez des sauvegardes de toutes vos cartes au format JSON ou CSV et restaurez-les sur n'importe quel appareil, ou partagez vos cartes avec d'autres.
> - 🚫 **Sans publicité** — l'application est totalement autonome et ne contient aucune publicité ni traceur de suivi.

> [!TIP]
> **Télécharger l'application :**
> - 📦 **Télécharger l'APK de la version :** [GitHub Releases](https://github.com/Storvild/android_discount_cards/releases/latest)

---

## 📸 Captures d'écran

| Écran principal | Écran principal | Détails de la carte | Ajout / Scanner |
| :---: | :---: |:---------------------------------------------------------------------------:| :---: |
| <img src="../../screenshots/01_main_with_barcodes.jpg" width="220" alt="Écran principal avec codes-barres"> | <img src="../../screenshots/02_main_compact.jpg" width="220" alt="Écran principal compact"> | <img src="../../screenshots/03_card_detail.jpg" width="220" alt="Détails de la carte"> | <img src="../../screenshots/04_add_edit_card.jpg" width="220" alt="Ajout d'une carte"> |

| Paramètres | Exportation des cartes | Importation des cartes | Tri des cartes |
| :---: | :---: | :---: |:----------------------------------------------------------------------------------------------------------:|
| <img src="../../screenshots/05_settings.jpg" width="220" alt="Paramètres"> | <img src="../../screenshots/06_export_cards.jpg" width="220" alt="Exportation des cartes"> | <img src="../../screenshots/07_import_cards.jpg" width="220" alt="Importation des cartes"> | <img src="../../screenshots/08_sort_cards_light_theme.jpg" width="220" alt="Tri des cartes avec thème clair"> |

| Menu | Paramètres 2 | Widget 1 | Widget 2 |
| :---: | :---: | :---: |:----------------------------------------------------------------------------------------------------------:|
| <img src="../../screenshots/09_menu.jpg" width="220" alt="Menu"> | <img src="../../screenshots/10_settings2.jpg" width="220" alt="Paramètres 2"> | <img src="../../screenshots/11_widget1.jpg" width="220" alt="Widget 1"> | <img src="../../screenshots/12_widget2.jpg" width="220" alt="Widget 2"> |

---

## 📱 Principales fonctionnalités

- 💳 **Gestion des cartes de fidélité** — ajout, modification, suppression individuelle ou en masse des cartes.
- 📷 **Scan et sélection de code-barres depuis un fichier** — scan en temps réel des cartes physiques via l'appareil photo (CameraX + ML Kit) ou sélection d'une image de code-barres depuis la galerie/le fichier.
- 🔢 **Prise en charge de nombreux formats de codes-barres** — génération de codes-barres visuels à l'aide de la bibliothèque ZXing :
  - `QR_CODE`
  - `CODE_128`
  - `EAN_13` / `EAN_8`
  - `UPC_A`
  - `CODE_39`
  - `PDF_417`
  - `AZTEC`
  - `PHONE` (réduction par numéro de téléphone — affiché sous forme de texte, non généré comme code-barres)
- 🎨 **Personnalisation** — choix de la couleur de la carte depuis une palette prête à l'emploi ou d'une couleur libre via une palette HSV et un code HEX (`#RRGGBB`), choix d'une icône de catégorie, ajout de notes et de codes CVC/CVV.
- ⭐️ **Favoris et statistiques** — marquage des cartes utilisées fréquemment, comptage automatique du nombre de vues.
- 🔍 **Recherche intelligente** — recherche rapide par nom de magasin, numéro de carte ou notes.
- 🔀 **Tri des cartes** — tri pratique de la liste par nom, date de création ou fréquence d'utilisation.
> - ⚡️ **Mode liste compact** — réglage « Afficher les codes-barres dans la liste » (activé par défaut). Sa désactivation désactive la génération des codes-barres et des numéros de cartes sur l'écran principal, réduit la hauteur des éléments de la liste de moitié et accélère nettement le chargement et le défilement.
- 💡 **Contrôle de la luminosité de l'écran** — réglage automatique du niveau de luminosité sélectionné à l'ouverture d'une carte, ainsi que possibilité d'ajuster rapidement directement sur l'écran de la carte à l'aide d'un curseur intégré (activé par l'option « Afficher la luminosité sur la carte » dans les paramètres, désactivé par défaut).
- 📤 **Partage de cartes** — possibilité de partager les cartes sélectionnées avec d'autres utilisateurs.
- 🌐 **Prise en charge des langues** — choix séparé de la langue de l'interface dans les paramètres de l'application (11 langues prises en charge).
- 🌗 **Thèmes** — prise en charge des thèmes clair, sombre et système.
- 📦 **Sauvegarde (Exportation / Importation)** — stockage des données au format **JSON** ou **CSV** :
  - Modes d'importation : **MERGE** (Fusion) ou **OVERWRITE** (Écraser).
  - Stratégies flexibles de gestion des doublons : *Mettre à jour les existantes*, *Ajouter comme nouvelles*, *Ignorer*.
  - 📂 **Ouverture des fichiers de sauvegarde (« Ouvrir avec »)** — prise en charge de l'ouverture directe ou de l'envoi (`ACTION_VIEW`, `ACTION_SEND`) de fichiers (JSON) depuis les gestionnaires de fichiers avec lancement automatique de la boîte de dialogue d'importation des cartes.
- 🗃️ **Archives des cartes** — transfert des cartes non utilisées vers une section séparée :
  - Bouton **« Transférer dans les archives »** sur l'écran de détails d'une carte active et bouton **« Extraire des archives »** sur l'écran de détails d'une carte archivée.
  - Élément **« Archives »** dans le menu déroulant de l'écran principal ouvrant un écran séparé avec recherche, liste et état vide.
  - Les cartes archivées sont masquées de la liste principale, de la recherche, de l'exportation et du partage (Share).
- 🧩 **Widgets sur l'écran d'accueil** — widget redimensionnable (de 1x1 à 5x5) avec choix de la carte, code-barres/QR et nom du magasin, bouton de modification des paramètres et ouverture rapide de la carte au toucher.
- 👆 **Menu contextuel par appui long** — accès rapide aux actions « Modifier » et « Supprimer » lors d'un appui long sur une carte dans la liste.
- 📱 **Interface Edge-to-Edge** — interface moderne et adaptative tenant compte des panneaux système, basée sur Material 3 et Jetpack Compose Adaptive.

---

## 🌐 Langues prises en charge
- **Russe**
- **Anglais (English)**
- **Français**
- **Allemand (Deutsch)**
- **Japonais (日本語)**
- **Espagnol (Español)**
- **Portugais (Português)**
- **Hindi (हिन्दी)**
- **Arabe (العربية)**
- **Turc (Türkçe)**
- **Chinois simplifié (简体中文)**

---

## 💾 Sauvegarde et format des données

L'application prend en charge deux formats : **JSON** et **CSV**.

### JSON

```json
[
  {
    "id": 1,
    "storeName": "Pyaterochka",
    "cardNumber": "1234567890123",
    "barcodeFormat": "EAN_13",
    "note": "Carte de la famille",
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

Tableau avec l'en-tête `Nom,Code,TypeDeCode,CVC,Note`:

```csv
Nom,Code,TypeDeCode,CVC,Note
Pyaterochka,1234567890123,EAN_13,123,Carte de la famille
Magnit,9876543210,QR_CODE,,
```

### Modes d'importation :
- **MERGE (Fusion)** — ajout des cartes du fichier à celles déjà présentes dans la base.
- **OVERWRITE (Écraser)** — remplacement complet des cartes actuelles par les données du fichier.

### Stratégies de résolution des conflits en cas de cartes en double :
- **UPDATE_EXISTING (Mettre à jour les existantes)** — mise à jour des informations de la carte.
- **ADD_AS_NEW (Ajouter comme nouvelles)** — création d'une nouvelle carte en double.
- **SKIP (Ignorer)** — passage du doublon.

---

## 🔒 Confidentialité et autonomie

**DiscountCards** est une application **totalement hors ligne**. Elle ne **demande pas** d'autorisation d'accès à Internet (`android.permission.INTERNET`).

**Ce que cela apporte à l'utilisateur :**
- 🛡 **Confidentialité absolue :** toutes les données de vos cartes, l'historique d'utilisation et les sauvegardes sont stockées localement sur votre appareil. Rien n'est envoyé vers des serveurs tiers.
- 🚫 **Absence de publicité :** sans Internet, il est physiquement impossible de charger et d'afficher de la publicité ou des traceurs de suivi.
- 🔋 **Économie de batterie :** aucune synchronisation en arrière-plan cachée ni requêtes réseau qui consomment la batterie et le trafic mobile.
- ✈️ **Indépendance du réseau :** les cartes sont chargées instantanément et génèrent des codes-barres n'importe où : même dans les boîtes en béton des hypermarchés ou les sous-sols sans couverture mobile.

## 📋 Autorisations requises

L'application n'exige qu'un seul accès, et même celui-ci à la demande :

| Autorisation | But |
|---|---|
| `android.permission.CAMERA` | Scan des codes-barres de cartes physiques (facultatif, demandé uniquement au moment du scan, `uses-feature required="false"`). |

---

## 🛠 Pour les développeurs

Les informations techniques sur l'architecture, la pile technologique, la structure du projet, le format de stockage des données et les commandes de compilation ont été déplacées dans un document séparé :

👉 [**Documentation pour les développeurs**](../../DEVELOPMENT.md)

---

## 📄 Licence

Le projet est diffusé sous les conditions de la licence spécifiées dans le fichier [LICENSE](../../../LICENSE).
