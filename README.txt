═══════════════════════════════════════════════════════════════════════════════════
                      E-OTEL REZERVASYON SİSTEMİ
                        (Hotel Reservation System)
═══════════════════════════════════════════════════════════════════════════════════

📋 PROJENİN AÇIKLAMASI
─────────────────────────────────────────────────────────────────────────────────

E-Otel Rezervasyon Sistemi, modern bir otel yönetim platformudur. Müşterilerin otel
odalarını rezerve etmelerine, çıkış yapmalarına ve bekleme listesinde beklemelerine
olanak tanır. Sistem, Aralık Ağacı (Interval Tree) veri yapısı kullanarak tarih
çakışmalarını verimli bir şekilde kontrol eder.

🎯 ÖZELLİKLER
─────────────────────────────────────────────────────────────────────────────────

✅ ÇOKLU ŞUBEYİ DESTEK ✅
   • Bayburt Şubesi
   • Çorlu Şubesi
   • Los Angeles Şubesi
   • Las Vegas Şubesi

✅ RESERVASYONLARİ YÖNETME ✅
   • Yeni müşteri kaydı ve oda rezervasyonu
   • TC doğrulaması (11 haneli)
   • Tarih aralığı kontrolleri
   • Kapasite kontrolü
   • Tarih çakışması tespiti (Interval Tree ile)

✅ ÇIKIŞİ YÖNETME ✅
   • Odadan güvenli çıkış
   • TC numarası ile doğrulama
   • Verinin arşive kaydedilmesi

✅ BEKLEME LİSTESİ ✅
   • Tam kapasitede olan odalar için bekleme listesi
   • Müşteri sırası ve bilgileri
   • Gösterim ve yönetimi

✅ ARŞIV VE RAPOR ✅
   • Geçmiş rezervasyonlara erişim
   • Merkezi rapor (tüm şubeler özeti)
   • JSON tabanlı kalıcı veri depolama

🏗️ PROJE MİMARİSİ
─────────────────────────────────────────────────────────────────────────────────

SRC YAPISI:
───────────

application/
  ├── MainLauncher.java          → Program başlangıç noktası
  ├── RezervasyonApplication.java → Uygulama başlatıcı
  ├── RezervasyonConsole.java     → Eski console arayüzü
  └── ConsoleUI.java              → Yeni interaktif kullanıcı arayüzü ⭐

model/
  ├── Musteri.java                → Müşteri model sınıfı
  ├── Oda.java                    → Oda model sınıfı (Interval Tree içerir)
  ├── Rezervasyon.java            → Rezervasyon model sınıfı
  ├── BeklemeListesi.java         → Bekleme listesi yönetimi
  └── BeklemeDugumu.java          → Bekleme listesi düğümü

service/
  ├── MerkeziSistem.java          → Ana merkez sistemi (4 şubeyi yönetir)
  └── OtelYonetimi.java           → Şube yönetimi ve işlemleri

DATA FILES:
───────────
  ├── Bayburt.json                → Bayburt şubesi verileri
  ├── Çorlu.json                  → Çorlu şubesi verileri
  ├── Los Angeles.json            → Los Angeles şubesi verileri
  └── Las Vegas.json              → Las Vegas şubesi verileri

🚀 KURULUM VE ÇALIŞTIRMA
─────────────────────────────────────────────────────────────────────────────────

GEREKSINIMLER:
  • Java 8 veya daha yüksek
  • Maven 3.6 veya daha yüksek
  • Gson kütüphanesi (pom.xml'de tanımlanmış)

ADIMlar:

1. Proje Dosyasını İndir:
   Proje dosyasını bilgisayarınıza indirin veya klonlayın.

2. Proje Dizinine Gidin:
   cd C:\Users\deniz\IdeaProjects\e-otel-rezervasyon

3. IntelliJ IDEA'da Aç:
   • IntelliJ IDEA'yı açın
   • File > Open > proje klasörünü seçin

4. Programı Çalıştırın:
   IntelliJ IDEA'da ConsoleUI.java dosyasını sağ tıklayın
   → "Run ConsoleUI.main()" seçeneğini tıklayın

   Veya Alt+Shift+F10 tuşlarına basın

5. Console Arayüzü Hazır!
   Program çalışırsa, menü karşınızda görünecek.

📝 KULLANIM REHBERI
─────────────────────────────────────────────────────────────────────────────────

ANA MENÜ:
─────────
Açılış ekranında 5 seçenek vardır:

1. Merkezi Rapor Görüntüle
   → Tüm şubelerin genel özeti gösterilir
   → Toplam rezervasyon sayısı, bekleme listesi vb.

2-5. Şube Seçimi
   → Bayburt, Çorlu, Los Angeles, Las Vegas


ŞUBEYİ SEÇTİKTEN SONRA ALT MENÜ:
─────────────────────────────────

1. Yeni Rezervasyon Yap
   ─────────────────────
   Aşağıdaki bilgileri girin:

   TC: 11 haneli Türkiye Cumhuriyet numarası (sadece rakam)
       Örn: 12345678901

   Ad Soyad: Müşteri adı ve soyadı
       Örn: Ahmet Yılmaz

   Oda Numarası: 1-25 arasında
       Örn: 5

   Başlangıç Tarihi: YYYY-MM-DD formatında
       Örn: 2026-05-20

   Bitiş Tarihi: YYYY-MM-DD formatında (başlangıçtan sonra olmalı)
       Örn: 2026-05-25

   ✓ Başarılı kayıttan sonra rezervasyon onayı alırsınız.
   ✗ Hata durumunda (tarih çakışması, tam kapasite vb.) hata mesajı gösterilir.
     Bekleme listesine eklenilir.


2. Odadan Çıkış Yap
   ────────────────
   Oda Numarası: Çıkış yapılacak oda (1-25)
   TC: Müşterinin TC numarası (doğrulama için)

   ✓ Başarılı çıkıştan sonra oda boşalır, veriler arşive kaydedilir.
   ✗ Hata durumunda hata mesajı gösterilir.


3. Bekleme Listesini Görüntüle
   ──────────────────────────
   • Tam kapasiteli odalar için bekleyen müşterilerin listesini gösterir
   • TC, Ad, Başlangıç ve Bitiş tarihlerini gösterir
   • Sırasıyla gösterilir


4. Arşivlenmiş Rezervasyonları Görüntüle
   ───────────────────────────────────
   • Geçmiş rezervasyonlara (çıkış yapılmış müşteriler) erişim sağlar
   • TC, Ad Soyad, Oda Numarası, Başlangıç ve Bitiş tarihlerini gösterir


5. Şube Değiştir
   ──────────────
   • Ana menüye dönüş
   • Başka şube seçebilirsiniz


6. Çıkış
   ─────
   • Programı kapatır


🔧 VERİ YAPILARI
─────────────────────────────────────────────────────────────────────────────────

INTERVAL TREE (Aralık Ağacı):
──────────────────────────────
Her Oda nesnesi içinde bir Interval Tree tutulur. Bu yapı:
  • Tarih aralıklarını verimli bir şekilde temsil eder (O(log n) zamanda)
  • Çakışmaları kontrol eder
  • Kapasite dolduğunda bekleme listesi yönetir

BEKLEME LİSTESİ:
───────────────
Bağlı liste (Linked List) yapısı ile:
  • Müşterileri sırada tutar
  • FIFO (First In First Out) prensibine göre hizmet eder

JSON VERİ KAYDI:
────────────────
Gson kütüphanesi ile:
  • Rezervasyonlar dosyalara kaydedilir
  • Arşiv ve kalıcı veriler tutulur
  • Program kapatıldıktan sonra da veriler korunur


💡 ÖRNEK SENARYO
─────────────────────────────────────────────────────────────────────────────────

1. "Bayburt" şubesini seç
2. "Yeni Rezervasyon Yap" seçeneğini tıkla
3. Bilgileri gir:
   TC: 12345678901
   Ad: Mehmet Demir
   Oda: 3
   Başlangıç: 2026-05-20
   Bitiş: 2026-05-25

4. Başarılı mesajı göreceksin
5. "Bekleme Listesini Görüntüle" ile listeyi görebilirsin
6. "Odadan Çıkış Yap" ile aynı TC ile çıkış yapabilirsin


⚠️ HATA YÖNETIMI
─────────────────────────────────────────────────────────────────────────────────

Program aşağıdaki hataları yönetir:

✗ HATALAR:
  • Yanlış TC formatı (11 haneli olmalı)
  • Geçmiş tarihleri girme
  • Bitiş tarihi < Başlangıç tarihi
  • Oda numarası dışında (1-25)
  • Tarih çakışması
  • Tam kapasiteli oda (Bekleme listesine eklenir)
  • Sayı girmesi gereken yerlere harf girme
  • Geçersiz seçim


📦 BAĞIMLILIKLARI
─────────────────────────────────────────────────────────────────────────────────

POM.XML Dosyasında:

Java: 1.8
Gson: 2.10 (JSON işlemleri için)


🎓 TEKNOLOJİLER
─────────────────────────────────────────────────────────────────────────────────

✓ Programlama Dili: Java
✓ Derleme Aracı: Maven
✓ JSON Kütüphanesi: Gson
✓ Veri Yapıları: Aralık Ağacı (Interval Tree), Bağlı Liste (Linked List)
✓ Dosya İşlemleri: JSON dosya okuma/yazma
✓ Kullanıcı Arayüzü: Java Scanner (Console)


👨‍💻 GELIŞTIRME NOTLARI
─────────────────────────────────────────────────────────────────────────────────

• Sistem tamamen modülerdir. Backend (service katmanı) hiçbir şekilde
  değiştirilmeden, sadece arayüz (UI) eklenmiştir.

• Interval Tree kullanımı sayesinde sistem binlerce rezervasyonu da
  hızlı bir şekilde işleyebilir.

• Bekleme listesi sistematik bir şekilde yönetilir. Oda boşaldığında
  otomatik olarak kontrol edilir.

• Tüm veriler JSON formatında tutulur ve programa göre saklanır.


📞 DESTEK
─────────────────────────────────────────────────────────────────────────────────

Soru veya sorun yaşarsanız:
  1. Konsol çıktısındaki hata mesajını okuyun
  2. Dosya yollarının doğru olduğundan emin olun
  3. JSON dosyaları projelerin kök dizininde olmalı


═══════════════════════════════════════════════════════════════════════════════════
                     Son Güncelleme: 17-05-2026
═══════════════════════════════════════════════════════════════════════════════════

