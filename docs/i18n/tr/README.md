# DiscountCards

**DiscountCards**, sadakat kartlarını ve indirim kartlarını rahatça saklamak, taramak ve kullanmak için tasarlanmış bir Android uygulamasıdır. Uygulama, tüm indirim kartlarınızı tek bir yerde saklamanıza, barkod oluşturucu sayesinde kasiyerde hızlıca taramanıza ve verilerinizi yedeklemenize olanak tanır.

> [!IMPORTANT]
> **Öne çıkan avantajlar:**
> - ⚡️ **Anında açılış** — uygulama bir saniyenin kesirinde açılır ve yüklenme bekleme süresi olmadan başlatıldıktan hemen sonra kullanıma hazırdır.
> - 🧩 **Widget üzerinden barkoda hızlı erişim** — seçilen barkodu içeren bir widget'ı ana ekrana ekleyin ve **uygulamayı kendiniz açmadan** kasiyere gösterin.
> - 📤 **Veri dışa ve içe aktarımı** — tüm kartların yedeklerini JSON veya CSV biçiminde oluşturun ve herhangi bir cihazda geri yükleyin veya kartları başkalarıyla paylaşın.
> - 🚫 **Reklamsız** — uygulama tamamen bağımsızdır ve hiçbir reklam veya takipçiler içermez.

> [!TIP]
> **Uygulamayı indirin:**
> - 📦 **Sürüm APK'sını indirin:** [GitHub Releases](https://github.com/Storvild/android_discount_cards/releases/latest)

---

## 📸 Ekran görüntüleri

| Ana ekran | Ana ekran | Kart detayları | Ekleme / Tarama |
| :---: | :---: |:---------------------------------------------------------------------------:| :---: |
| <img src="../../screenshots/main_with_barcodes.jpg" width="220" alt="Barkodlu ana ekran"> | <img src="../../screenshots/main_compact.jpg" width="220" alt="Kompakt ana ekran"> | <img src="../../screenshots/card_detail.jpg" width="220" alt="Kart detayları"> | <img src="../../screenshots/add_edit_card.jpg" width="220" alt="Kart ekleme"> |

| Ayarlar | Kartları dışa aktarma | Kartları içe aktarma | Kartları sıralama |
| :---: | :---: | :---: |:----------------------------------------------------------------------------------------------------------:|
| <img src="../../screenshots/settings.jpg" width="220" alt="Ayarlar"> | <img src="../../screenshots/export_cards.jpg" width="220" alt="Kartları dışa aktarma"> | <img src="../../screenshots/import_cards.jpg" width="220" alt="Kartları içe aktarma"> | <img src="../../screenshots/sort_cards_light_theme.jpg" width="220" alt="Açık tema ile kartları sıralama"> |

---

## 📱 Ana özellikler

- 💳 **Sadakat kartı yönetimi** — kart ekleme, düzenleme, tekil ve toplu silme.
- 📷 **Barkodu tarama ve dosyadan seçme** — kamera ile (CameraX + ML Kit) fiziksel kartların gerçek zamanlı taraması veya galeri/dosyadan barkodlu bir görsel seçme.
- 🔢 **Çoklu barkod biçimi desteği** — ZXing kitaplığıyla görsel barkod oluşturma:
  - `QR_CODE`
  - `CODE_128`
  - `EAN_13` / `EAN_8`
  - `UPC_A`
  - `CODE_39`
  - `PDF_417`
  - `AZTEC`
  - `PHONE` (telefon numarası üzerinden indirim — metin olarak görüntülenir, barkod olarak üretilmez)
- 🎨 **Kişiselleştirme** — hazır bir paletten kart rengi seçme veya HSV paleti ve HEX kodu (`#RRGGBB`) ile herhangi bir rengi seçme, kategori simgesi seçme, notlar ve CVC/CVV kodları ekleme.
- ⭐️ **Favoriler ve istatistikler** — sık kullanılan kartları işaretleme, görüntülenme sayısının otomatik takibi.
- 🔍 **Akıllı arama** — mağaza adı, kart numarası veya notlara göre hızlı arama.
- 🔀 **Kartları sıralama** — isim, oluşturma tarihi veya kullanım sıklığına göre kolay sıralama.
- ⚡️ **Kompakt liste modu** — «Listede barkodları göster» ayarı (varsayılan olarak açıktır). Kapatıldığında ana ekrandaki barkod ve kart numaralarının üretimi devre dışı bırakılır, liste öğesi yüksekliği yarıya iner ve yükleme ve kaydırma önemli ölçüde hızlanır.
- 💡 **Ekran parlaklığı yönetimi** — kart açıldığında seçilen parlaklık seviyesinin otomatik ayarlanması ve kart ekranındaki yerleşik kaydırıcı ile hızlı ayarlama (ayarlarda «Kartta parlaklığı göster» seçeneğiyle etkinleştirilir, varsayılan olarak kapalıdır).
- 📤 **Kart paylaşımı** — seçilen kartları diğer kullanıcılarla paylaşma imkânı.
- 🌐 **Dil desteği** — uygulama ayarlarında arayüz dilinin ayrı seçimi (11 dil desteği).
- 🌗 **Tasarım temaları** — açık, koyu ve sistem teması desteği.
- 📦 **Yedekleme (Dışa / İçe aktarma)** — verilerin **JSON** veya **CSV** biçiminde saklanması:
  - İçe aktarma modları: **MERGE (Birleştirme)** veya **OVERWRITE (Yazma)**.
  - Yinellemeler için esnek stratejiler: *Mevcutları güncelle*, *Yeni ekle*, *Atla*.
  - 📂 **Yedekleme dosyalarını açma («Şununla aç»)** — dosya yöneticilerinden (JSON) dosyaların doğrudan açılmasını veya gönderilmesini (`ACTION_VIEW`, `ACTION_SEND`) kart içe aktarma diyaloğunun otomatik başlatılmasıyla destekler.
- 🗃️ **Kart arşivi** — kullanılmayan kartların ayrı bir bölüme taşınması:
  - Aktif kartın detay ekranında **«Arşive taşı»** ve arşiv kartının detay ekranında **«Arşivden çıkar」** düğmesi.
  - Ana ekranın açılır menüsündeki **«Arşiv»** öğesi, arama, liste ve boş durum içeren ayrı bir ekran açar.
  - Arşiv kartları ana listeden, aramadan, dışa aktarmadan ve paylaşımdan (Share) gizlenir.
- 🧩 **Ana ekran widget'ları** — kart seçimi, barkod/QR ve mağaza adı, ayarların düzenlenmesi düğmesi ve dokunarak kartın hızlı açılmasını içeren genişletilebilir widget (1x1'den 5x5'e).
- 👆 **Uzun basmada bağlam menüsü** — listede kartın uzun basılmasında «Düzenle» ve «Sil» işlemlerine hızlı erişim.
- 📱 **Edge-to-Edge UI** — Material 3 ve Jetpack Compose Adaptive tabanlı, sistem panelleri dikkate alan modern uyarlama arayüzü.

---

## 🌐 Desteklenen diller
- **Rusça (Русский)**
- **İngilizce (English)**
- **Fransızca (Français)**
- **Almanca (Deutsch)**
- **Japonca (日本語)**
- **İspanyolca (Español)**
- **Portekizce (Português)**
- **Hintçe (हिन्दी)**
- **Arapça (العربية)**
- **Türkçe (Türkçe)**
- **Basitleştirilmiş Çince (简体中文)**

---

## 💾 Yedekleme ve veri biçimi

Uygulama iki biçimi destekler: **JSON** ve **CSV**.

### JSON

```json
[
  {
    "id": 1,
    "storeName": "Şok Market",
    "cardNumber": "1234567890123",
    "barcodeFormat": "EAN_13",
    "note": "Aile kartı",
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

Başlık satırı `İsim,Kod,Kod tipi,CVC,Not` olan tablo:

```csv
İsim,Kod,Kod tipi,CVC,Not
Şok Market,1234567890123,EAN_13,123,Aile kartı
Migros,9876543210,QR_CODE,,
```

### İçe aktarma modları:
- **MERGE (Birleştirme)** — dosyadaki kartların veritabanındaki mevcut kartlara eklenmesi.
- **OVERWRITE (Yazma)** — mevcut kartların dosyadaki verilerle tamamen değiştirilmesi.

### Kartların çakışmasında çözüm stratejileri:
- **UPDATE_EXISTING (Mevcutları güncelle)** — kart bilgilerinin güncellenmesi.
- **ADD_AS_NEW (Yeni ekle)** — yeni bir yinelenen kartın oluşturulması.
- **SKIP (Atla)** — yinelenenin atlanması.

---

## 🔒 Gizlilik ve bağımsızlık

**DiscountCards** tamamen **çevrimdışı** bir uygulamadır. Temel olarak internet erişimi izni (`android.permission.INTERNET`) **istemez**.

**Bu kullanıcıya ne kazandırır:**
- 🛡 **Mutlak gizlilik:** Kartlarınızın tüm verileri, kullanım geçmişi ve yedeklemeler cihazınızda yerel olarak saklanır. Hiçbir şey dış sunuculara gönderilmez.
- 🚫 **Reklamsız:** İnternet olmadan reklam veya takipçilerin yüklenip gösterilmesi fiziksel olarak imkânsızdır.
- 🔋 **Pil tasarrufu:** Pil ve mobil veri tüketen gizli arka plan senkronizasyonları ve ağ istekleri yoktur.
- ✈️ **Ağdan bağımsızlık:** Kartlar her yerde anında yüklenir ve barkod üretir: süpermarketlerin beton kasalarında veya hücresel kapsama alanı olmayan bodrumlarda bile.

## 📋 Gerekli izinler (Permissions)

Uygulamanın yalnızca bir erişime ihtiyacı vardır ve bu da istek üzerine:

| İzin | Amaç |
|---|---|
| `android.permission.CAMERA` | Fiziksel kartlardan barkod taraması (isteğe bağlı, yalnızca tarama anında istenir, `uses-feature required="false"`). |

---

## 🛠 Geliştiriciler için

Mimari, teknoloji yığını, proje yapısı, veri saklama biçimi ve derleme komutları hakkındaki teknik bilgiler ayrı bir belgede birleştirilmiştir:

👉 [**Geliştirici belgeleri**](../../DEVELOPMENT.md)

---

## 📄 Lisans

Proje, [LICENSE](../../../LICENSE) dosyasında belirtilen lisans koşulları altında dağıtılmaktadır.
