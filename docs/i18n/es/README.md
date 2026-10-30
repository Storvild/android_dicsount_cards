# DiscountCards

**DiscountCards** es una aplicación Android para almacenar, escanear y usar de forma cómoda tus tarjetas de fidelización y de descuento. La aplicación te permite guardar todas tus tarjetas de descuento en un solo lugar, escanearlas rápidamente en la caja mediante un generador de códigos de barras y crear copias de seguridad de tus datos.

> [!IMPORTANT]
> **Ventajas clave:**
> - ⚡️ **Arranque instantáneo** — la aplicación se abre en una fracción de segundo y está lista para usarse inmediatamente después de arrancar, sin tiempo de espera.
> - 🧩 **Acceso rápido al código de barras mediante un widget** — añade un widget con el código de barras seleccionado a tu pantalla de inicio y preséntaselo al cajero **sin abrir la propia aplicación**.
> - 📤 **Exportación e importación de datos** — crea copias de seguridad de todas tus tarjetas en formato JSON o CSV y restáuralas en cualquier dispositivo, o comparte tus tarjetas con otros.
> - 🚫 **Sin anuncios** — la aplicación es totalmente autónoma y no contiene ningún anuncio ni rastreadores.

> [!TIP]
> **Descargar la aplicación:**
> - 📦 **Descargar el APK de la versión:** [GitHub Releases](https://github.com/Storvild/android_discount_cards/releases/latest)

---

## 📸 Capturas de pantalla

| Pantalla principal | Pantalla principal | Detalles de la tarjeta | Añadir / Escáner |
| :---: | :---: |:---------------------------------------------------------------------------:| :---: |
| <img src="../../screenshots/01_main_with_barcodes.jpg" width="220" alt="Pantalla principal con códigos de barras"> | <img src="../../screenshots/02_main_compact.jpg" width="220" alt="Pantalla principal compacta"> | <img src="../../screenshots/03_card_detail.jpg" width="220" alt="Detalles de la tarjeta"> | <img src="../../screenshots/04_add_edit_card.jpg" width="220" alt="Añadir tarjeta"> |

| Ajustes | Exportar tarjetas | Importar tarjetas | Ordenación de tarjetas |
| :---: | :---: | :---: |:----------------------------------------------------------------------------------------------------------:|
| <img src="../../screenshots/05_settings.jpg" width="220" alt="Ajustes"> | <img src="../../screenshots/06_export_cards.jpg" width="220" alt="Exportar tarjetas"> | <img src="../../screenshots/07_import_cards.jpg" width="220" alt="Importar tarjetas"> | <img src="../../screenshots/08_sort_cards_light_theme.jpg" width="220" alt="Ordenación de tarjetas con tema claro"> |

| Menú | Ajustes 2 | Widget 1 | Widget 2 |
| :---: | :---: | :---: |:----------------------------------------------------------------------------------------------------------:|
| <img src="../../screenshots/09_menu.jpg" width="220" alt="Menú"> | <img src="../../screenshots/10_settings2.jpg" width="220" alt="Ajustes 2"> | <img src="../../screenshots/11_widget1.jpg" width="220" alt="Widget 1"> | <img src="../../screenshots/12_widget2.jpg" width="220" alt="Widget 2"> |

---

## 📱 Principales características

- 💳 **Gestión de tarjetas de fidelización** — añadir, editar y eliminar tarjetas de forma individual o masiva.
- 📷 **Escaneo y selección de código de barras desde un archivo** — escaneo de tarjetas físicas en tiempo real con la cámara (CameraX + ML Kit) o selección de una imagen de código de barras desde la galería/archivo.
- 🔢 **Soporte de múltiples formatos de código de barras** — generación de códigos de barras visuales con la biblioteca ZXing:
  - `QR_CODE`
  - `CODE_128`
  - `EAN_13` / `EAN_8`
  - `UPC_A`
  - `CODE_39`
  - `PDF_417`
  - `AZTEC`
  - `PHONE` (descuento por número de teléfono — se muestra como texto, no se genera como código de barras)
- 🎨 **Personalización** — elección del color de la tarjeta desde una paleta lista o un color libre mediante paleta HSV y código HEX (`#RRGGBB`), selección de un icono de categoría, adición de notas y códigos CVC/CVV.
- ⭐️ **Favoritos y estadísticas** — marcar tarjetas usadas con frecuencia, recuento automático de visualizaciones.
- 🔍 **Búsqueda inteligente** — búsqueda rápida por nombre de tienda, número de tarjeta o notas.
- 🔀 **Ordenación de tarjetas** — ordenación cómoda de la lista por nombre, fecha de creación o frecuencia de uso.
- ⚡️ **Modo de lista compacta** — configuración «Mostrar códigos de barras en la lista» (activada por defecto). Al desactivarla se desactiva la generación de códigos de barras y números de tarjeta en la pantalla principal, se reduce a la mitad la altura de los elementos de la lista y se acelera notablemente la carga y el desplazamiento.
- 💡 **Control del brillo de la pantalla** — ajuste automático del nivel de brillo elegido al abrir una tarjeta, además de posibilidad de ajuste rápido directamente en la pantalla de la tarjeta mediante un deslizador integrado (se activa con la opción «Mostrar brillo en la tarjeta» en los ajustes, desactivada por defecto).
- 📤 **Compartir tarjetas** — posibilidad de compartir las tarjetas seleccionadas con otros usuarios.
- 🌐 **Soporte de idiomas** — selección separada del idioma de la interfaz en los ajustes de la aplicación (11 idiomas soportados).
- 🌗 **Temas** — soporte de tema claro, oscuro y del sistema.
- 📦 **Copia de seguridad (Exportar / Importar)** — almacenamiento de los datos en formato **JSON** o **CSV**:
  - Modos de importación: **MERGE** (Combinar) o **OVERWRITE** (Sobrescribir).
  - Estrategias flexibles para el manejo de duplicados: *Actualizar existentes*, *Añadir como nuevas*, *Omitir*.
  - 📂 **Apertura de archivos de copia de seguridad («Abrir con»)** — soporte para la apertura directa o envío (`ACTION_VIEW`, `ACTION_SEND`) de archivos (JSON) desde gestores de archivos, con arranque automático del diálogo de importación de tarjetas.
- 🗃️ **Archivo de tarjetas** — traslado de tarjetas no usadas a una sección separada:
  - Botón **«Mover al archivo»** en la pantalla de detalles de una tarjeta activa y **«Extraer del archivo»** en la pantalla de detalles de una tarjeta archivada.
  - Elemento **«Archivo»** en el menú desplegable de la pantalla principal que abre una pantalla separada con búsqueda, lista y estado vacío.
  - Las tarjetas archivadas se ocultan de la lista principal, de la búsqueda, de la exportación y del compartido (Share).
- 🧩 **Widgets en la pantalla de inicio** — widget ampliable (de 1x1 a 5x5) con selección de tarjeta, código de barras/QR y nombre de la tienda, botón para editar los parámetros y apertura rápida de la tarjeta al tocar.
- 👆 **Menú contextual mediante pulsación larga** — acceso rápido a las acciones «Editar» y «Eliminar» al mantener pulsada una tarjeta en la lista.
- 📱 **UI Edge-to-Edge** — interfaz moderna y adaptable que tiene en cuenta los paneles del sistema, basada en Material 3 y Jetpack Compose Adaptive.

---

## 🌐 Idiomas soportados
- **Ruso**
- **Inglés (English)**
- **Francés (Français)**
- **Alemán (Deutsch)**
- **Japonés (日本語)**
- **Español**
- **Portugués (Português)**
- **Hindi (हिन्दी)**
- **Árabe (العربية)**
- **Turco (Türkçe)**
- **Chino simplificado (简体中文)**

---

## 💾 Copia de seguridad y formato de datos

La aplicación soporta dos formatos: **JSON** y **CSV**.

### JSON

```json
[
  {
    "id": 1,
    "storeName": "Pyaterochka",
    "cardNumber": "1234567890123",
    "barcodeFormat": "EAN_13",
    "note": "Tarjeta familiar",
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

Tabla con la cabecera `Nombre,Codigo,TipodeCodigo,CVC,Nota`:

```csv
Nombre,Codigo,TipodeCodigo,CVC,Nota
Pyaterochka,1234567890123,EAN_13,123,Tarjeta familiar
Magnit,9876543210,QR_CODE,,
```

### Modos de importación:
- **MERGE (Combinar)** — añade las tarjetas del archivo a las ya existentes en la base.
- **OVERWRITE (Sobrescribir)** — sustituye por completo las tarjetas actuales por los datos del archivo.

### Estrategias de resolución de conflictos para tarjetas duplicadas:
- **UPDATE_EXISTING (Actualizar existentes)** — actualiza la información de la tarjeta.
- **ADD_AS_NEW (Añadir como nuevas)** — crea una tarjeta duplicada nueva.
- **SKIP (Omitir)** — omite el duplicado.

---

## 🔒 Privacidad y autonomía

**DiscountCards** es una aplicación **totalmente sin conexión**. En principio **no solicita** el permiso de acceso a Internet (`android.permission.INTERNET`).

**Qué aporta al usuario:**
- 🛡 **Privacidad absoluta:** todos los datos de tus tarjetas, el historial de uso y las copias de seguridad se almacenan localmente en tu dispositivo. Nada se envía a servidores de terceros.
- 🚫 **Sin anuncios:** sin Internet es físicamente imposible cargar y mostrar anuncios o rastreadores.
- 🔋 **Ahorro de batería:** no hay sincronizaciones ocultas en segundo plano ni solicitudes de red que consuman batería y datos móviles.
- ✈️ **Independencia de la red:** las tarjetas se cargan al instante y generan códigos de barras en cualquier lugar: incluso en las cajas de hormigón de los supermercados o en sótanos sin cobertura móvil.

## 📋 Permisos requeridos

La aplicación solo requiere un acceso, y además bajo petición:

| Permiso | Propósito |
|---|---|
| `android.permission.CAMERA` | Escaneo de códigos de barras de tarjetas físicas (opcional, se solicita solo en el momento del escaneo, `uses-feature required="false"`). |

---

## 🛠 Para desarrolladores

La información técnica sobre la arquitectura, el stack tecnológico, la estructura del proyecto, el formato de almacenamiento de datos y los comandos de compilación se ha trasladado a un documento separado:

👉 [**Documentación para desarrolladores**](../../DEVELOPMENT.md)

---

## 📄 Licencia

El proyecto se distribuye bajo las condiciones de la licencia especificada en el archivo [LICENSE](../../../LICENSE).
