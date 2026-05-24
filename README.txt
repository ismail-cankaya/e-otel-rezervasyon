# E-Otel Rezervasyon Yönetim Sistemi

Java ve JavaFX kullanılarak geliştirilmiş, çok şubeli (Çorlu, Bayburt, Los Angeles, Las Vegas) gelişmiş bir otel rezervasyon ve merkezi yönetim sistemidir. Temel veri yapıları (Ağaçlar, Bağlı Listeler) üzerine inşa edilen bu proje, şubeler arası veri akışını JSON üzerinden kalıcı hale getirerek modern ve kullanıcı dostu bir masaüstü arayüzü sunar.

--------------------------------------------------
* TEMEL ÖZELLİKLER (Masaüstü Arayüzü)
--------------------------------------------------
Sistem, kullanıcı deneyimini merkeze alan "RezervasyonApplication" üzerinden yönetilir:

- Dinamik Şube Yönetimi: Tek bir merkezi sistem ekranından şubeler arası anında geçiş yapabilme.
- Akıllı Arama ve Rezervasyon:
  * Kişi sayısı ve giriş-çıkış tarihlerine göre müsait odaları anında filtreleme.
  * Seçilen kişi sayısına göre dinamik olarak genişleyen Müşteri Kayıt Formu (TC Kimlik ve Ad-Soyad doğrulamalı).
  * Geçmiş tarihlere veya mantıksız aralıklara (çıkışın girişten önce olması) karşı anlık arayüz uyarıları.
- Otomatik Kuyruk (Bekleme Listesi) Sistemi: İstenilen tarihlerde oda doluysa, müşteri otomatik olarak bekleme listesine alınır. Odadan çıkış (Check-Out) yapıldığı anda, sistem kuyruğu kontrol eder ve sıradaki uygun müşteriyi otomatik olarak odaya yerleştirir.
- Kapsamlı Raporlama ve Arşiv Ekranı:
  * Aktif Konaklayanlar: Şu an içeride olan (In-House) müşterilerin listesi.
  * Bekleme Listesi: Odaların boşalmasını bekleyen müşterilerin anlık kuyruk durumu.
  * Arşiv (Geçmiş): Çıkış yapmış müşterilerin kronolojik arşivi.
  * Merkezi Zincir Raporu: Tüm şubelerin toplam kayıtlı müşteri, aktif konaklayan ve toplam kasa (hasılat) bilgilerinin tek ekranda finansal özeti.
  * TC ile Sorgulama: Belirli bir müşterinin aktif konaklama durumunu anında bulma.

Not: Sistemde bulunan "RezervasyonConsole" tam bir alternatif değil, arayüz tasarlanırken temel fonksiyonları test etmek amacıyla oluşturulmuş bir prototiptir. Sistemin asıl ve kararlı kullanım alanı JavaFX masaüstü arayüzüdür.

--------------------------------------------------
* ALGORİTMALAR VE VERİ YAPILARI
--------------------------------------------------
Bu proje, standart koleksiyon kütüphanelerinin ötesinde, spesifik problemlerin çözümü için özel veri yapıları kullanılarak optimize edilmiştir:

1. Aralık Ağacı (Interval Tree - AralikAgaci):
   Tarih çakışmalarını tespit etmek. Bir odanın belirli bir tarih aralığında kaç kişi tarafından rezerve edildiğini veya müsait olup olmadığını hızlıca hesaplar.

2. Kronolojik Rezervasyon Ağacı (BST - KronolojikRezervasyonAgaci):
   Çıkış yapan müşterilerin arşivlenmesi. İkili Arama Ağacı yapısıyla çıkış tarihlerine göre kronolojik olarak sıralı bir şekilde tutulur (In-Order Traversal ile tarihe göre sıralı veri çekilir).

3. Özel Bağlı Liste (Linked List - BeklemeListesi):
   Gelişmiş kuyruk (Queue) yönetimi. Sadece ilk gireni ilk çıkarmakla kalmaz; boşalan odanın numarasına ve tarih aralığına en uygun ilk sıradaki kişiyi aradan bulup çıkartacak şekilde optimize edilmiştir (siradakiUygunTalebiAl algoritması).

--------------------------------------------------
* MİMARİ VE SINIF İLİŞKİLERİ (UML ÖZETİ)
--------------------------------------------------
Proje, katmanlı mimari (N-Tier Architecture) prensiplerine uygun olarak paketlere ayrılmış ve modüler bir yapıda tasarlanmıştır.

1. application Katmanı (Sunum / UI)
- MainLauncher: JavaFX modül hatalarını önlemek için tasarlanmış giriş sınıfı. Uygulamayı başlatır.
- RezervasyonApplication: Sistemin ana masaüstü grafik arayüzüdür (GUI).
- RezervasyonConsole: Arayüz entegrasyonu öncesi kullanılan test prototipidir.

2. service Katmanı (İş Mantığı / Business Logic)
- MerkeziSistem: Otel zincirinin en üst yöneticisidir. İçerisinde şubeleri temsil eden bir liste tutar.
- OtelYonetimi: Tek bir şubenin beyni konumundadır. JSON dosya okuma/yazma, rezervasyon ve listeleme algoritmalarının koştuğu ana sınıftır.

3. model Katmanı (Veri Modelleri)
- Oda: Sistemin temel taşıdır. Aktif rezervasyonlarını ve çakışma kontrolleri için kendi AralikAgaci nesnesini tutar.
- Rezervasyon: Müşteri, oda ve tarih bilgilerini tutan model sınıfıdır.
- Musteri: TC Kimlik No ve Ad-Soyad bilgilerini tutar.
- BeklemeListesi: Baştan sona kendi düğüm (Node) yapısıyla kodlanmış bağlı listedir.

4. util Katmanı (Özel Veri Yapıları)
- AralikAgaci (Interval Tree): Tarih aralıklarını tutan gelişmiş arama yapısı.
- KronolojikRezervasyonAgaci (BST): Geçmiş kayıtları (arşiv) tarihe göre sıralı tutmak için kullanılır.

Temel UML İlişki Akışı:
RezervasyonApplication -> MerkeziSistem -> OtelYonetimi -> (Oda + BeklemeListesi + KronolojikRezervasyonAgaci) -> Rezervasyon -> Musteri

--------------------------------------------------
* KULLANILAN TEKNOLOJİLER
--------------------------------------------------
- Java: Core Java, Nesne Yönelimli Programlama (OOP)
- JavaFX: Zengin masaüstü kullanıcı arayüzü (GUI) tasarımı
- Gson (Google JSON): Şube verilerinin (.json) serileştirilmesi (Persistent Data)

--------------------------------------------------
* KURULUM VE ÇALIŞTIRMA
--------------------------------------------------
1. Projeyi yerel bilgisayarınıza klonlayın.
2. JavaFX SDK'nın ve Gson kütüphanesinin projenize dahil edildiğinden emin olun.
3. Uygulamayı başlatmak için "application.MainLauncher" sınıfını çalıştırın.
4. Sistem ilk çalıştığında şubeler için gerekli .json dosyalarını proje dizininde otomatik olarak oluşturacaktır.

--------------------------------------------------
* GELİŞTİRİCİLER
--------------------------------------------------
- Deniz Kahramanoğlu
- İsmail Çankaya
- Eren Karaağaç
- Emin Ortaca