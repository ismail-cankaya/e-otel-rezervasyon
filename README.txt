================================================================================
         E-OTEL REZERVASYON SİSTEMİ - DETAYLI DOKÜMANTASYON
================================================================================

🏨 PROJE HAKKINDA
================================================================================
Adı: E-Otel Rezervasyon Sistemi
Amaç: Otel zincirlerinin rezervasyon, müşteri yönetimi, fiyatlandırma ve tarih
       çakışma kontrolünü yapan kapsamlı bir arka uç sistemi.
Dil: Java 24 (JDK 24)
Kurgusu: Maven (Bağımlılık Yönetimi)
Arayüzler: Scanner (CLI) + JavaFX (Desktop GUI)


📊 TEKNIK MİMARİ
================================================================================
Sistem, katmanlaşmış mimari (Layered Architecture) ile düzenlenmişti:

1. MODEL KATMANI (model/)
   ├─ Musteri.java       → TC, Ad/Soyad bilgileri
   ├─ Oda.java           → Oda No, Kapasite, Günlük Fiyat, Aktif Rezervasyonlar
   ├─ Rezervasyon.java   → Müşteri, Oda, Başlangıç/Bitiş Tarihi
   ├─ BeklemeListesi.java→ Bağlı Liste Veri Yapısı (FIFO Sistemi)
   └─ İç Veri Yapıları   → Bekleme Listesi Düğümleri

2. SERVICE KATMANI (service/)
   ├─ OtelYonetimi.java  → Şube Operasyonları (Rez., Çıkış, Sorgu, JSON I/O)
   └─ MerkeziSistem.java → 4 Şube Yönetimi ve Merkezi Rapor

 3. APPLICATION KATMANI (application/)
    ├─ RezervasyonConsole.java     → Scanner Tabanlı İnteraktif CLI Menü (Konsol)
    └─ RezervasyonApplication.java → JavaFX Desktop GUI Uygulaması

 4. UTILITY KATMANI (util/)
    ├─ AralikAgaci.java              → Interval Tree (Tarih Çakışmalarını Kontrol Eder)
    └─ KronolojikRezervasyonAgaci.java → Binary Search Tree (Arşiv Kronolojisiasısı)


🔌 BAĞIMLILIKLARI (pom.xml)
================================================================================
Maven Yapılandırması:
- Java Compiler: 24
- Gson: 2.10.1 (JSON Serialization/Deserialization)
- JavaFX: 17.0.6 (GUI Kütüphaneleri - Desktop Uygulaması)


📦 MODEL VERİ YAPISILARI (Detaylı)
================================================================================

Musteri (Müşteri) Sınıfı:
  Bilgiler:
    - tcNo: String (11 haneli, rakamsal)
    - adSoyad: String (Müşteri adı ve soyadı)
  Metotlar:
    - Getter/Setter (TC, Ad/Soyad)
  JSON Örneği:
    {
      "tcNo": "12345678901",
      "adSoyad": "Ahmet Yılmaz"
    }

Oda Sınıfı:
  Bilgiler:
    - odaNo: String (Oda numarası)
    - kapasite: int (1-4 arası kişi kapasitesi)
    - gunlukFiyat: int (Günlük konaklama ücreti TL cinsinden)
    - aktifRezervasyonlar: List<Rezervasyon> (Aktif rezervasyonlar)
    - agac: AralikAgaci (Tarih çakışmalarını kontrol için)
  Anahtar Metotlar:
    - musaitMi(baslangic, bitis): Tarih aralığında oda müsait mi?
    - getTarihtekiKisiSayisi(): O tarihler arasında kaç kişi konaklıyor?
    - rezervasyonEkle() / rezervasyonSil(): Sürü yönetimi
  Fiyatlandırma (Şubelere Göre):
    Bayburt:        1 Ki: 2000 TL | 2 Ki: 3000 TL | 3 Ki: 4000 TL | 4 Ki: 5000 TL
    Çorlu:          1 Ki: 1200 TL | 2 Ki: 1800 TL | 3 Ki: 2400 TL | 4 Ki: 3200 TL
    Los Angeles:    1 Ki: 1800 TL | 2 Ki: 2600 TL | 3 Ki: 3500 TL | 4 Ki: 4500 TL
    Las Vegas:      1 Ki: 1900 TL | 2 Ki: 2800 TL | 3 Ki: 3800 TL | 4 Ki: 4800 TL

Rezervasyon Sınıfı:
  Bilgiler:
    - musteri: Musteri (Konaklamacak müşteri objesi)
    - odaNo: String (Rezervasyon yapılan oda)
    - baslangicTarihi: LocalDate (Giriş tarihi, YYYY-MM-DD)
    - bitisTarihi: LocalDate (Çıkış tarihi, YYYY-MM-DD)
  Özellikleri:
    - Kompoziyon ilişkisi: Müşteri ve Rezervasyon bağlıdır
    - toString(): Anlaşılır format output
  JSON Örneği:
    {
      "musteri": {"tcNo": "12345678901", "adSoyad": "Ahmet Yılmaz"},
      "odaNo": "5",
      "baslangicTarihi": "2026-05-20",
      "bitisTarihi": "2026-05-25"
    }

BeklemeListesi Sınıfı:
  Yapı:
    - FIFO (First In First Out) sistemine dayalı bağlı liste
    - Her düğüm bir Rezervasyon tutar
  Kullanım Alanları:
    - Oda dolu ise müşteri bekleme listesine eklenir
    - Oda boşalınca sıdaki müşteri otomatik yerleştirilme kontrolü yapılır
  Anahtar Metot:
    - kuyrugaEkle(musteri, odaNo, bas, bit): Bekleme listesine ekle
    - siradakiUygunTalebiAl(oda): Sıradaki uygun müşteriyi al
    - listeyiYazdir(): Konsola yazdır


🔧 SERVICE KATMANI (İş Mantığı)
================================================================================

OtelYonetimi (Şube Yönetimi) Sınıfı:

   Yapısındaki Veri Kaynakları:
     - musteriler: Map<String, Musteri> (TC anahtar)
     - odalar: Map<String, Oda> (Oda Numarası anahtar)
     - beklemeListesi: BeklemeListesi
     - tamamlananRezervasyonlar: KronolojikRezervasyonAgaci (Arşiv - BST yapısı)
     - gson: Gson (JSON I/O)

  DOSYA İŞLEMLERİ (JSON Kalıcılık):
    constructor(dosyaAdi):
      - "Bayburt.json", "Los Angeles.json", "Las Vegas.json", "Çorlu.json"
      - İlk çalışmada 16 oda otomatik oluşturur
      - JSON varsa yükledi, yoksa default odalar oluşturur

    save(): JSON dosyasına yazma
    upload(): JSON dosyasından okuma
    odaEkle(): 16 oda başlangıç yapısı
      - Odalar 1-4: 1 kişi kapasitesi
      - Odalar 5-8: 2 kişi kapasitesi
      - Odalar 9-12: 3 kişi kapasitesi
      - Odalar 13-16: 4 kişi kapasitesi

  REZERVASYON İŞLEMLERİ:
    uygunOdalariGetir(kisiSayisi, bas, bit):
      - Belirtilen kişi sayısına uygun odaları listeler
      - Her oda için durum gösterir (Boş / Müsait / Dolu)
      - Fiyat bilgisi ile birlikte sunuş

    musteriKayitVeRezervasyon(tc, ad, odaNo, basTarih, bitTarih):
      - Yeni müşteri kaydı veya mevcut güncelleme
      - Tarih yazım hatalarını kontrol (YYYY-MM-DD)
      - Geçmiş tarihe rezervasyon engellenir
      - Kapasite kontrolü (Müsait ise doğrudan, değilse Bekleme Listesi)
      - Fiyat hesaplama: gunSayisi × gunlukFiyat
      - JSON'a kaydeder
      Return: Başarı/Hata mesajı

     cikisYap(odaNo, tc):
       - TC ile müşteri doğrulaması yapılır
       - Oda boşaldıktan sonra tamamlananRezervasyonlar'a ekler
         (KronolojikRezervasyonAgaci.ekle() ile çıkış tarihi sırasıyla kaydedilir)
       - Bekleme listesindeki uygun seçeneği otomatik yerleştirir
       - JSON'a kaydeder

  SORGU VE RAPOR İŞLEMLERİ:
    beklemeListesiniGoster(): Bekleme listesini yazdırır
    gecmisRezervasyonlariGoster(): Arşiv rezervasyonlarını yazdırır
    aktifKonaklayanlariGoster(): Şu an otelde olan müşteri listesi
    tcIleMusteriSorgula(tc): TC ile müşteri sorgusu
    getTümZamanlarKayitliMusteri(): Toplam kayıtlı müşteri sayısı
    getAktifKonaklayanSayisi(): Şu an konaklayan sayısı
    getToplamHasilat(): Akıllı fatura algoritması (Çifte hesaplandırmayı önler)


MerkeziSistem (Zincir Yönetimi) Sınıfı:

  Yapısı:
    - subeler: Map<String, OtelYonetimi> (Şube adı → OtelYonetimi)
    - 4 Şube başlangıç:
      * "Bayburt" → Bayburt.json
      * "Los Angeles" → Los Angeles.json
      * "Las Vegas" → Las Vegas.json
      * "Çorlu" → Çorlu.json

  Anahtar Metotlar:
    subeGetir(subeAdi):
      - Belirtilen şubenin OtelYonetimi nesnesini döndürür
      - Case-sensitive değil (trim işlemi)

    merkeziRaporOlustur():
      - Her şubenin bilgilerini rapor olarak hazırlayed
      - Toplam Müşteri Sayısı
      - Toplam Aktif Konaklayanlar
      - Toplam Hasılat
      - Zincir geneli özeti


🎨 APPLICATION KATMANI - KULLANICILAR ARAYÜZÜ
================================================================================

Proje İKİ Farklı Arayüzle Sunulur:

1️⃣ CLI ARAYÜZÜ (Terminal / Konsol) - RezervasyonConsole.java
========================================================================
Açıklama: Scanner kullanarak komut satırında çalışan metin tabanlı arayüz

Ana Menü İşleyişi:
  while(true) döngüsü içinde çalışır

  Seçenekler:
    0 - Merkezi Rapor Görüntüle
    1 - Bayburt Şubesi
    2 - Çorlu Şubesi
    3 - Los Angeles Şubesi
    4 - Las Vegas Şubesi
    5 - Çıkış

Şube Menüsü (Her şube için):
  1 - Yeni Rezervasyon Yap
  2 - Odadan Çıkış Yap
  3 - Bekleme Listesi Görüntüle
  4 - Arşivlenmiş Rezervasyonları Görüntüle
  5 - TC ile Müşteri Sorgusu
  6 - Aktif Konaklayanları Görüntüle
  7 - Şube Değiştir
  8 - Çıkış

UYGUN ODALAR ARAMA:
  Müşteri, kişi sayısı ve tarih aralığını girdikten sonra uygun odalar listelenir:
    Örn: "5 Numaralı Oda - Gecelik: 3000 TL | Durum: Boş"
  Müşteri müsait odalardan birini seçer

YENİ RESERVASYONveri Girişi:
  🆔 TC No: (11 hane, rakamsal - doğrulama yapılır)
  👤 Ad Soyad: (Metin)
  👥 Kişi Sayısı: (1-4 arası)
  🚪 Oda Numarası: (Listelenen müsait odalardan seç)
  📅 Başlangıç Tarihi: (YYYY-MM-DD formatı)
  📅 Bitiş Tarihi: (YYYY-MM-DD formatı, Başlangıçtan sonra olmalı)

  Doğrulamalar:
    ✓ TC 11 rakamsal hane
    ✓ Tarih geçmiş olamaz
    ✓ Bitiş > Başlangıç
    ✓ Oda kapasitesi ≥ Kişi Sayısı

  Başarı Mesajı:
    ✅ Başarılı: MÜŞTERI_ADI adına ODA_NO nolu odaya kayıt yapıldı.
    Gecelik: 3000 TL | Süre: 5 Gün | Toplam Fatura: 15000 TL

ODADAN ÇIKIŞI:
  🚪 Oda Numarası:
  🆔 TC No: (Doğrulama - şube bu TC kayıtlı mı?)

  Başarı:
    ✅ Çıkış başarılı. Kayıt arşive aktarıldı.
    (+ Bekleme listesine otomatik yerleştirme kontrolü)

RAPORLAR (CLI):
  📋 Merkezi Rapor: Zincir geneli statistik
  📋 Şube Raporu: İçerik olmadığında "Rapor Boş" mesajı
  📋 Bekleme Listesi: FIFO sırasına göre müşteriler
  📋 Arşiv: KronolojikRezervasyonAgaci ile tarih sırasında çıkış yapan müşteriler
            (In-order traversal sonucu kronolojik sırada gösterilir)

---

2️⃣ JAVAFX GUI ARAYÜZÜ (Masaüstü Uygulaması) - RezervasyonApplication.java
========================================================================
Açıklama: JavaFX ile geliştirilmiş grafiksel kullanıcı arayüzü uygulaması
Main Class (pom.xml'de): application.RezervasyonApplication

Başlangıç Komutu:
  🖥️ IntelliJ IDEA: RezervasyonApplication.java → Sağ tıkla → Run
  🖥️ Terminal: mvn javafx:run

Ana Pencere Özellikleri:
  ✅ Şube Seçim ComboBox (Çorlu / Bayburt / Los Angeles / Las Vegas)
  ✅ Tab-Tabanlı Menü Sistemi (3 Sekme)
  ✅ Pencere Başlığı: "E-Otel Yönetim Sistemi - Merkezi Sistem"

SEKME 1 - "Yeni Rezervasyon" Sekmesi:
  Bileşenler:
    - Kişi Sayısı: ComboBox (1, 2, 3, 4)
    - Giriş Tarihi: DatePicker
    - Çıkış Tarihi: DatePicker
    - "Uygun Odaları Getir" Butonu
    - Mevcut Oda Kombo: Arama sonuçlarıyla doldurulur

  Dinamik Misafir Girişi:
    - Seçilen kişi sayısı için otomatik input alanları üretilir
    - Her kişi satırı: [Sıra] Kişi: [TC TextField] [Ad Soyad TextField]

  Aksiyonlar:
    - "Tüm Kişileri Kaydet" Butonu (Yeşil)
    - "Çıkış / Temizle" Butonu (Kırmızı)

  Çıktılar:
    - Başarı/Hata Mesajları: Renkli Label
    - Başarı: Yeşil (#27ae60)
    - Hata: Kırmızı (#e74c3c)

SEKME 2 - "Çıkış (Check-Out)" Sekmesi:
  Bileşenler:
    - Çıkış Yapılacak Oda No: ComboBox (1-16)
    - Müşteri TC No: TextField
    - "Çıkış Yap" Butonu (Mavi)

  Çıktılar:
    - İşlem Sonuç Mesajı: Label'da gösterilir
    - Başarılı çıkıştan sonra formlar temizlenir

SEKME 3 - "Listeler ve Arşiv" Sekmesi:
  Raporlama Butonları:
    - Bekleme Listesi (Turuncu)
    - Arşiv (Geçmiş) (Mor)
    - İçeridekiler (Aktif) (Yeşil)
    - Merkezi Zincir Raporu (Koyu Gri)

  Müşteri Arama Alanı:
    - TC ile Arama: [TextField] [Müşteri Ara Butonu]

  Sonuçlar:
    - TextArea'da detaylı bilgiler gösterilir
    - Monospaced font: Tablosu görünümü
    - Font Boyutu: 13px
    - Kaydırılabilir alan

Tasarım Özellikleri:
  ✅ Renkli UI: Material Design Renkler
     - Turuncu (#f39c12): Uyarılar, arama
     - Yeşil (#27ae60): Başarı
     - Kırmızı (#e74c3c): Hata
     - Mavi (#3498db): İşlem
     - Mor (#8e44ad): Arşiv
     - Koyu (#2c3e50): Merkez
  ✅ Dinamik Form: Kişi sayısına göre otomatik input üretimi
  ✅ Doğrulama: Girdi validasyonu ile tabı geçişleri engelleme
  ✅ Responsive: VBox.setVgrow() ile otomatik boyutlandırma
  ✅ Komfortable: Padding, spacing ile boşluklar
  ✅ Okunabilirlik: Labeller, yardımcı metinler


⚙️ VERİ YAPISILARI ve ALGORİTMALER
================================================================================

Interval Tree (AralikAgaci.java):
   Amaç: Belirli bir tarih aralığında kaç kişi konaklıyor?
   Yapı: İkili Ağaç (Binary Tree) tabanlı
   İşlem:
     - Ekle (Rezervasyon): O(log n)
     - Sorgula (Tarih Aralığında Kişi Sayısı): O(log n + k) k:sonuç sayısı
   Fayda:
     - Tarih çakışması hızlı kontrolü
     - Kapasite kontrolü verimli yapılır

Kronolojik Rezervasyon Ağacı (KronolojikRezervasyonAgaci.java):
   Amaç: Tamamlanan (Çıkış yapılmış) rezervasyonları kronolojik sırada tutmak
   Yapı: Binary Search Tree (BST) - Tarih anahtarı ile sıralanmış

   Anahtar Bileşenler:
   - AgacDugumu (İç Sınıf - Serializable):
     * tarih: LocalDate (Çıkış tarihi - ağaç anahtarı)
     * rezervasyonlar: List<Rezervasyon> (Aynı tarihte çıkış yapan tüm müşteriler)
     * sol: AgacDugumu (Daha eski tarihler)
     * sag: AgacDugumu (Daha yeni tarihler)

   Anahtar Metotlar:
     ekle(Rezervasyon rez): O(log n)
       - Rezervasyonun bitiş tarihi (çıkış tarih) ağaca eklenir
       - Aynı tarihte birden çok çıkış olabilir (List olarak tutulur)
       - Tarihler otomatik sıralanır (Eski Sol, Yeni Sağ)

     toList(): O(n)
       - In-order traversal ile (Sol → Kök → Sağ) kronolojik listeyi döndürür
       - Tamamlanan rezervasyonları tarih sırasıyla rapor için hazırlar
       - Ay-yıl bazında fatura hesaplamada kullanılır

     temizle()
       - Tüm ağacı temizler (kök = null)
       - Yeni sezonda veri sıfırlama için kullanılır

   Faydalar:
     - O(log n) ekleme → Çıkış işlemi hızlı
     - O(n) listeleme → Raporlar kronolojik sırada
     - Ay bazında fatura hesaplaması optimize (Çifte hesaplandırma önlenir)
     - Arşiv yönetimi verimli yapılır

Kapasiteli Rezervasyon Kontrolü:
  For each Oda:
    If oda.kapasite == kisiSayisi:
      oTarihtekiKisiSayisi = agac.cakisanSayisiniBul(bas, bit)
      If oTarihtekiKisiSayisi < kapasitesi:
        "Oda Müsait"
      Else:
        "Bekleme Listesine Ekle"

 Akıllı Fatura Algoritması (getToplamHasilat):
   1. KronolojikRezervasyonAgaci.toList() çağrılır
      → tamamlananRezervasyonlar tarih sırasıyla (In-order) alınır
   2. For each Çıkış Tarihi Grubu:
        For each tamamlananRezervasyonlar[i]:
          gün_sayısı = bitiş - başlangış
          if oda günü daha önceden faturlanmamış ise:
            toplam_hasilat += (gün_sayısı × oda.gunlukFiyat)
          mark oda günü faturalı
   3. Sonuç: Çifte faturaldırma engellenir

   KronolojikRezervasyonAgaci'nin Avantajı:
     - Tarih sırasıyla veriler hazırlanır (O(n) traversal)
     - Ay-yıl bazında raporlar hızlı oluşturulur
     - Fatura tutarı doğru hesaplanır

FIFO Bekleme Listesi:
  - Düğüm → Düğüm (Linked List)
  - İlk eklenen ilk çıkar
  - Sanal kapasitesi sınırsız


📝 JSON VERİ YAPISI ÖRNEĞİ
================================================================================

Bayburt.json:
{
  "musteriler": {
    "12345678901": {
      "tcNo": "12345678901",
      "adSoyad": "Ahmet Yılmaz"
    }
  },
  "odalar": {
    "1": {
      "odaNo": "1",
      "kapasite": 1,
      "gunlukFiyat": 2000,
      "aktifRezervasyonlar": [
        {
          "musteri": {"tcNo": "12345678901", "adSoyad": "Ahmet Yılmaz"},
          "odaNo": "1",
          "baslangicTarihi": "2026-05-20",
          "bitisTarihi": "2026-05-22"
        }
      ]
    }
  },
  "beklemeListesi": {
    "kafa": null
  },
   "tamamlananRezervasyonlar": [
     {
       "musteri": {"tcNo": "98765432101", "adSoyad": "Ayşe Kaya"},
       "odaNo": "2",
       "baslangicTarihi": "2026-05-10",
       "bitisTarihi": "2026-05-15"
     },
     {
       "musteri": {"tcNo": "11111111111", "adSoyad": "Mehmet Demir"},
       "odaNo": "5",
       "baslangicTarihi": "2026-05-12",
       "bitisTarihi": "2026-05-18"
     }
   ]
 }

 NOT: tamamlananRezervasyonlar listesi aşağıdaki sırada tutulur:
      - Program çalışma sırasında: KronolojikRezervasyonAgaci (BST) ile O(log n) hızında
      - JSON dosyasında: In-order traversal sonucu kronolojik sırada kaydedilir
      - Raporlarda: toList() çağrısıyla tarih sırasıyla gösterilir


🚀 KURULUM VE ÇALIŞTIRILMA
================================================================================

GEREKSINIMLER:
  ✓ Java 24 (JDK 24) - pom.xml'de maven.compiler.source/target
  ✓ Maven 3.6+ (Build aracı)
  ✓ JavaFX 17.0.6 (GUI kütüphaneleri - pom.xml'de tanımlı)
  ✓ IDE: IntelliJ IDEA, Eclipse, VS Code + Java Extension Pack
  ✓ Git (Versiyon kontrolü - isteğe bağlı)

ADIM 1 - Proje Açma:
  1. IntelliJ IDEA veya IDE'niz açın
  2. "Open Project" → e-otel-rezervasyon klasörünü seçin
  3. pom.xml Maven olarak yapılandırılmış olmalı

ADIM 2 - Bağımlılıkları İndirme:
  Maven otomatik indirir, manuel olarak:
  IDE'de sağ tıklama → "Run 'pom.xml'" veya Terminal:
    mvn clean install

ADIM 3 - Projeyi Çalıştırma:

  ⚙️ CLI ARAYÜZÜ (RezervasyonConsole):

    IntelliJ IDEA'da:
      1. RezervasyonConsole.java dosyasını aç
      2. Sağ tıkla → "Run 'RezervasyonConsole.main()'"
      Veya: Ctrl+Shift+F10 (Windows/Linux)

    Terminal Üzerinden:
      mvn clean compile
      mvn exec:java -Dexec.mainClass="application.RezervasyonConsole"

  🖥️ JAVAFX GUI ARAYÜZÜ (RezervasyonApplication):

    IntelliJ IDEA'da:
      1. RezervasyonApplication.java dosyasını aç
      2. Sağ tıkla → "Run 'RezervasyonApplication.main()'"
      Veya: Ctrl+Shift+F10 (Windows/Linux)

    Terminal Üzerinden:
      mvn clean compile javafx:run

ADIM 4 - Programı Kapatma:
  Ana menüden "5 - Çıkış" seçeneği ile veya Ctrl+C


📚 KULLANIM ÖRNEĞİ
================================================================================

Senaryo: Ahmet Yılmaz, Bayburt şubesinde 20-25 Mayıs arasında 2 kişi için
         oda ayırtmak istiyor.

Adımlar:

1. Program başlatılır:
   RezervasyonConsole konsolunda açılır

2. Ana menü:
   >>> Seçim yapınız: 1 (Bayburt Şubesi)

3. Şube menüsü:
   >>> Seçim yapınız: 1 (Yeni Rezervasyon Yap)

4. Kişi sayısı seçimi:
   📋 Kaç kişi konaklayacaksınız? 2

5. İçinde buıı ödalar gösterilir:
   5 Numaralı Oda - Gecelik: 3000 TL | Durum: Boş
   6 Numaralı Oda - Gecelik: 3000 TL | Durum: Boş
   ...

6. Müşteri No: 5 seçer, ardından:
   🆔 TC No: 12345678901
   👤 Ad Soyad: Ahmet Yılmaz
   📅 Başlangıç Tarihi (YYYY-MM-DD): 2026-05-20
   📅 Bitiş Tarihi (YYYY-MM-DD): 2026-05-25

7. Sistem Process:
   ✓ TC Doğrulaması: ✓ 11 hane
   ✓ Müşteri Kaydı: Yeni ise ekle
   ✓ Tarih Kontrolleri: ✓ Geçmiş değil, Bitiş > Başlangıç
   ✓ Kapasite Kontrolü: ✓ Oda2 kişi alabiliyor
   ✓ Fiyat Hesaplama: 5 Gün × 3000 TL = 15000 TL
   ✓ JSON Kayıt

8. Çıktı:
   ✅ Başarılı: Ahmet Yılmaz adına 5 nolu odaya kayıt yapıldı.
   Gecelik: 3000 TL | Süre: 5 Gün | Toplam Fatura: 15000 TL

9. Çıkış Yapmak:
   Şube menüsü → 2 (Odadan Çıkış Yap) → Oda No: 5 → TC: 12345678901
   ✅ Çıkış başarılı. Kayıt arşive aktarıldı.

10. Arşivi Kontrol Etmek:
    Şube menüsü → 4 (Arşiv) → Çıkış tarihi: 2026-05-25 ile kayıt


🔒 HATA YAKALAMA VE DOĞRULAMA
================================================================================

Sistem Seviye Hatalar (try-catch ile kontrol):
  ✓ Tarih Parsing Hatası: Input "aşşağıda" → "YYYY-MM-DD formatında giriniz"
  ✓ Oda Bulunamadı: Input "99" (yok) → "99 numaralı oda bulunmuyor"
  ✓ TC Müşteri Bulunamadı: → "Bu TC ile kayıtlı müşteri yok"
  ✓ Diy değerleri: "abc" sayısal input → Sayıca giriş istenir

Doğrulama Kontrolleri:
  ✓ TC Numarası: Exactly 11 rakamsal hane
  ✓ Tarih Formatı: YYYY-MM-DD (RegEx değil, LocalDate.parse)
  ✓ Oda No: 1-16 arası, varolan oda
  ✓ Tarih Mantığı: Başlangıç <= Bitiş, Geçmiş tarih yok
  ✓ Kapasite: kişi sayısı <= oda.kapasite


💡 GELİŞİM FİKİRLERİ (İleride Eklenebilir)
================================================================================
  □ Veritabanı Entegrasyonu (MySQL / MongoDB)
  □ Kullanıcı Yönetimi ve Oturum (Login/Logout)
  □ GUI (JavaFX Desktop Application)
  □ REST API (Spring Boot Microservices)
  □ İstatistiksel Analiz (Raporlar, Grafikler)
  □ E-mail Bildirim Sistemi
  □ Mobil Uygulama (Android/iOS)
  □ İngilizce/Diğer Dillere Lokalizasyon
  □ İskonto ve Promosyon Yönetimi
  □ Kredi Kartı Ödeme Entegrasyonu


📞 TEKNIK DESTEĞİ
================================================================================
Sorunlar:
  - Gson JSON Parse Hatası → JSON dosyası yazım hatası var mı kontrol et
  - Oda Bulunamadı → Dosyada ilgili JSON var mı kontrol et
  - NumberFormat Hatasında → Input türünü doğrula (Metin mi sayı mı)

Dosya Yükleme Hatası:
  - JSON Dosyaları projenin root klasöründe olmalı (pom.xml ile aynı)
  - Dosya adları tam olmalı: "Bayburt.json", "Los Angeles.json" vs.


================================================================================
                     © 2026 E-Otel Rezervasyon Sistemi
                          Sürüm: 2.0 FINAL
                    (CLI + JavaFX GUI + BST Arşiv Sistemi)
================================================================================
