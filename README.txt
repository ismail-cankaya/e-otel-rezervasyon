E-OTEL REZERVASYON SISTEMI

E-Otel Rezervasyon Sistemi, modern otel isletmelerinin musteri kayit, oda tahsisi ve kapasite yonetimi sureclerini optimize etmek amaciyla gelistirilmis kapsamli bir konsol uygulamasidir. Sistem, ozellikle yogun donemlerde karsilasilan tarih cakismalarini Aralik Agaci (Interval Tree) veri yapisi ile yuksek performansli bir sekilde cozerek kesintisiz bir rezervasyon deneyimi sunar.

TEMEL OZELLIKLER

* Coklu Sube Yonetimi: Bayburt, Corlu, Los Angeles ve Las Vegas subelerinin tek bir merkezden, ancak birbirinden bagimsiz veritabanlariyla yonetilmesi.
* Dinamik Rezervasyon ve Cakisma Kontrolu: 11 haneli TC Kimlik Numarasi dogrulamasi ile guvenli kayit. Interval Tree algoritmasi sayesinde, istenen tarih araliklarindaki musaitlik durumunun O(log n) karmasikliginda hizli tespiti.
* Akilli Bekleme Listesi (Waitlist): Kapasitesi dolan odalar icin Bagli Liste (Linked List) tabanli, FIFO (Ilk Giren Ilk Cikar) prensibiyle calisan otomatik sira yonetimi. Odayi bosaltan bir musteri oldugunda, siradaki musteri otomatik olarak isleme alinir.
* Cikis Islemleri ve Arsivleme: Musteri cikislarinda TC kimlik dogrulamasi ve gecmis rezervasyonlarin guvenli bir sekilde arsive aktarilmasi.
* Kalici Veri Depolama: Tum rezervasyon, bekleme listesi ve sube verilerinin Gson kutuphanesi kullanilarak JSON formatinda kalici olarak saklanmasi.

TEKNOLOJILER VE VERI YAPILARI

Bu proje, temel programlama prensiplerinin yani sira ileri duzey veri yapilarinin pratik uygulamalarini icermektedir:

* Dil: Java (JDK 8+)
* Bagimlilik Yonetimi: Maven (v3.6+)
* Veri Formati: JSON (Gson v2.10)
* Aralik Agaci (Interval Tree): Tarihsel verilerin cakisma analizlerini anlik ve verimli bir sekilde yapmak icin odalarin arka planinda calisir.
* Bagli Liste (Linked List): Bekleme listesindeki musterilerin bellek dostu ve sirali bir sekilde tutulmasi icin kullanilmistir.

PROJE MIMARISI

Proje, Surdurulebilirlik ve genisletilebilirlik ilkelerine uygun olarak Moduler (Katmanli) Mimari ile tasarlanmistir:

src/
-- application/
   -- MainLauncher.java (Programin baslangic noktasi)
   -- RezervasyonApplication.java (Uygulama baslatici ve yapilandirici)
   -- RezervasyonConsole.java (Etkilesimli, kullanici dostu konsol arayuzu)
-- model/
   -- Musteri.java (Musteri verilerini tutan entity)
   -- Oda.java (Oda modeli, Interval Tree entegrasyonu icerir)
   -- Rezervasyon.java (Rezervasyon detaylari)
   -- BeklemeListesi.java (Bekleyen musteriler icin Linked List yapisi)
-- service/
   -- MerkeziSistem.java (4 farkli subenin genel orkestrasyonu)
   -- OtelYonetimi.java (Sube bazli algoritmik is mantigi)

data/
-- Bayburt.json
-- Corlu.json
-- Los Angeles.json
-- Las Vegas.json

KURULUM VE CALISTIRMA

Projeyi kendi lokal ortaminizda test etmek icin asagidaki adimlari izleyebilirsiniz:

1. Depoyu Klonlayin:
git clone https://github.com/kullaniciadi/e-otel-rezervasyon.git
cd e-otel-rezervasyon

2. Gereksinimleri Yukleyin:
Sisteminizde Java 8+ ve Maven kurulu oldugundan emin olun. Proje kok dizininde pom.xml bulundugu icin IDE'niz (IntelliJ IDEA, Eclipse, vb.) gerekli Gson kutuphanesini otomatik olarak indirecektir.

3. Uygulamayi Baslatin:
IDE'niz uzerinden src/application/ConsoleUI.java dosyasini acin ve main metodunu calistirin.

KULLANIM REHBERI

Program basariyla baslatildiginda sizi etkilesimli bir ana menu karsilar:

1. Sube Secimi & Merkezi Rapor: Ilgili subeyi secerek veya tum subelerin ozet raporunu (toplam kayitlar, bekleme durumlari) goruntuleyerek baslayin.
2. Rezervasyon Islemleri: Yeni Rezervasyon Yap secenegi ile musteri bilgilerini (TC, Ad-Soyad, Oda No, Baslangic/Bitis Tarihi) girin. Sistem tarih cakismasi tespit ederse sizi uyaracak ve isterseniz musteriyi Bekleme Listesine alacaktir.
3. Cikis ve Arsiv: Odadan Cikis Yap secenegini kullanarak musterinin kaydini arsive tasiyin. Bu islem, bekleyen musteriler icin odayi otomatik olarak musait hale getirir.

HATA YONETIMI POLITIKASI

Sistem, kullanici deneyimini kesintiye ugratmamak adina saglam bir hata yonetimine sahiptir:
* Gecersiz (11 haneden farkli veya harf iceren) TC kimlik numaralari reddedilir.
* Gecmis tarihlere veya baslangic tarihinden onceye denk gelen bitis tarihleriyle rezervasyon yapilamaz.
* Gecersiz menu secimleri ve format hatalari (sayi yerine harf girilmesi) uygulamanin cokmesini engellemek icin Try-Catch bloklariyla guvenli bir sekilde yakalanir.
