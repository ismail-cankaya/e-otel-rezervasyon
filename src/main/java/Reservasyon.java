import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

// --- 1. VERİ MODELLERİ (Modeller) ---

class Musteri {
    String tcNo;
    String adSoyad;

    public Musteri(String tcNo, String adSoyad) {
        this.tcNo = tcNo;
        this.adSoyad = adSoyad;
    }
}

class Oda {
    String odaNo;
    int kapasite;
    List<Rezervasyon> aktifRezervasyonlar; // Tarih çakışma kontrolü için tutulan aktif liste

    public Oda(String odaNo, int kapasite) {
        this.odaNo = odaNo;
        this.kapasite = kapasite;
        this.aktifRezervasyonlar = new ArrayList<>();
    }

    // Interval Tree mantığını simüle eden basit tarih çakışma kontrolü
    public boolean musaitMi(LocalDate baslangic, LocalDate bitis) {
        for (Rezervasyon rez : aktifRezervasyonlar) {
            // İki tarih aralığının çakışma formülü: (Baslangic1 < Bitis2) && (Baslangic2 < Bitis1)
            if (baslangic.isBefore(rez.bitisTarihi) && rez.baslangicTarihi.isBefore(bitis)) {
                return false; // Çakışma var, oda dolu
            }
        }
        return true; // Çakışma yok, oda boş
    }
}

class Rezervasyon {
    Musteri musteri;
    Oda oda;
    LocalDate baslangicTarihi;
    LocalDate bitisTarihi;

    public Rezervasyon(Musteri musteri, Oda oda, LocalDate baslangicTarihi, LocalDate bitisTarihi) {
        this.musteri = musteri;
        this.oda = oda;
        this.baslangicTarihi = baslangicTarihi;
        this.bitisTarihi = bitisTarihi;
    }

    @Override
    public String toString() {
        return "Müşteri: " + musteri.adSoyad + " | Oda: " + oda.odaNo + " | Tarih: " + baslangicTarihi + " -> " + bitisTarihi;
    }
}

// --- 2. SIFIRDAN YAZILAN VERİ YAPISI: BAĞLI LİSTE (Linked List) ---

class BeklemeDugumu {
    Musteri musteri;
    BeklemeDugumu sonraki;

    public BeklemeDugumu(Musteri musteri) {
        this.musteri = musteri;
        this.sonraki = null;
    }
}

class BeklemeListesi {
    private BeklemeDugumu bas; // Head
    private BeklemeDugumu son; // Tail

    public void kuyrugaEkle(Musteri musteri) {
        BeklemeDugumu yeniDugum = new BeklemeDugumu(musteri);
        if (bas == null) {
            bas = son = yeniDugum;
        } else {
            son.sonraki = yeniDugum;
            son = yeniDugum;
        }
        System.out.println("-> " + musteri.adSoyad + " bekleme listesine (kuyruğa) eklendi.");
    }

    public void listeyiYazdir() {
        if (bas == null) {
            System.out.println("Bekleme listesi boş.");
            return;
        }
        BeklemeDugumu gecici = bas;
        int sira = 1;
        while (gecici != null) {
            System.out.println(sira + ". Sırada bekleyen: " + gecici.musteri.adSoyad);
            gecici = gecici.sonraki;
            sira++;
        }
    }
}

// --- 3. ANA SİSTEM YÖNETİMİ ---

class OtelYonetimi {
    // HashMap ile hızlı erişim
    private Map<String, Musteri> musteriler = new HashMap<>();
    private Map<String, Oda> odalar = new HashMap<>();

    // SIFIRDAN: Bağlı Liste ile Bekleme kuyruğu
    private BeklemeListesi beklemeListesi = new BeklemeListesi();

    // BST (TreeMap) ile kronolojik arşiv
    private TreeMap<LocalDate, Rezervasyon> tamamlananRezervasyonlar = new TreeMap<>();

    public OtelYonetimi() {
        // Test verisi için sahte odalar ekleyelim
        odalar.put("101", new Oda("101", 2));
        odalar.put("102", new Oda("102", 3));
    }

    public void musteriKayitVeRezervasyon(String tc, String ad, String odaNo, String basTarih, String bitTarih) {
        try {
            LocalDate baslangic = LocalDate.parse(basTarih);
            LocalDate bitis = LocalDate.parse(bitTarih);

            // 1. Müşteri Hashmap'te var mı? Yoksa oluştur (O(1))
            musteriler.putIfAbsent(tc, new Musteri(tc, ad));
            Musteri musteri = musteriler.get(tc);

            // 2. Oda Hashmap'ten çek (O(1))
            Oda talepEdilenOda = odalar.get(odaNo);
            if (talepEdilenOda == null) {
                System.out.println("Hata: " + odaNo + " numaralı bir oda sistemde bulunmuyor.");
                return;
            }

            // 3. Tarih çakışma kontrolü
            if (talepEdilenOda.musaitMi(baslangic, bitis)) {
                Rezervasyon yeniRezervasyon = new Rezervasyon(musteri, talepEdilenOda, baslangic, bitis);
                talepEdilenOda.aktifRezervasyonlar.add(yeniRezervasyon);
                System.out.println("Başarılı: Rezervasyon oluşturuldu! " + yeniRezervasyon);
            } else {
                System.out.println("Uyarı: " + odaNo + " numaralı oda bu tarihlerde DOLU!");
                // 4. Bağlı Listeye (Bekleme Kuyruğuna) ekle
                beklemeListesi.kuyrugaEkle(musteri);
            }

        } catch (DateTimeParseException e) {
            System.out.println("Hata: Lütfen tarihleri YYYY-MM-DD formatında giriniz.");
        }
    }

    public void cikisYap(String odaNo, String tc) {
        Oda oda = odalar.get(odaNo);
        if (oda != null) {
            Rezervasyon iptalEdilecek = null;
            for (Rezervasyon rez : oda.aktifRezervasyonlar) {
                if (rez.musteri.tcNo.equals(tc)) {
                    iptalEdilecek = rez;
                    break;
                }
            }

            if (iptalEdilecek != null) {
                oda.aktifRezervasyonlar.remove(iptalEdilecek);
                // 5. Tamamlanan rezervasyonları BST'ye (TreeMap) kronolojik olarak ekle
                tamamlananRezervasyonlar.put(iptalEdilecek.bitisTarihi, iptalEdilecek);
                System.out.println("Çıkış işlemi başarılı. Kayıt arşive (BST) aktarıldı.");
            } else {
                System.out.println("Hata: Bu odada bu müşteriye ait aktif kayıt bulunamadı.");
            }
        }
    }

    public void beklemeListesiniGoster() {
        System.out.println("\n--- Bekleme Listesi (Linked List) ---");
        beklemeListesi.listeyiYazdir();
    }

    public void gecmisRezervasyonlariGoster() {
        System.out.println("\n--- Geçmiş Rezervasyonlar (Kronolojik - BST/TreeMap) ---");
        if (tamamlananRezervasyonlar.isEmpty()) {
            System.out.println("Arşivde hiç kayıt yok.");
            return;
        }
        for (Map.Entry<LocalDate, Rezervasyon> entry : tamamlananRezervasyonlar.entrySet()) {
            System.out.println("Çıkış Tarihi: " + entry.getKey() + " | Detay: " + entry.getValue());
        }
    }
}

// --- 4. KONSOL ARAYÜZÜ (Main) ---

public class Reservasyon {
    public static void main(String[] args) {
        OtelYonetimi sistem = new OtelYonetimi();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n===== E-OTEL YÖNETİM SİSTEMİ =====");
            System.out.println("1 - Yeni Rezervasyon Yap");
            System.out.println("2 - Çıkış (Check-Out) Yap");
            System.out.println("3 - Bekleme Listesini Görüntüle");
            System.out.println("4 - Arşiv Kayıtlarını (BST) Görüntüle");
            System.out.println("5 - Çıkış");
            System.out.print("Seçiminiz: ");

            String secim = scanner.nextLine();

            switch (secim) {
                case "1":
                    System.out.print("TC Kimlik No: ");
                    String tc = scanner.nextLine();
                    System.out.print("Ad Soyad: ");
                    String ad = scanner.nextLine();
                    System.out.print("Oda No (101 veya 102 test odaları): ");
                    String odaNo = scanner.nextLine();
                    System.out.print("Giriş Tarihi (YYYY-MM-DD): ");
                    String basTarih = scanner.nextLine();
                    System.out.print("Çıkış Tarihi (YYYY-MM-DD): ");
                    String bitTarih = scanner.nextLine();
                    sistem.musteriKayitVeRezervasyon(tc, ad, odaNo, basTarih, bitTarih);
                    break;
                case "2":
                    System.out.print("Çıkış Yapılacak Oda No: ");
                    String cOda = scanner.nextLine();
                    System.out.print("Müşteri TC No: ");
                    String cTc = scanner.nextLine();
                    sistem.cikisYap(cOda, cTc);
                    break;
                case "3":
                    sistem.beklemeListesiniGoster();
                    break;
                case "4":
                    sistem.gecmisRezervasyonlariGoster();
                    break;
                case "5":
                    System.out.println("Sistemden çıkılıyor...");
                    scanner.close();
                    System.exit(0);
                default:
                    System.out.println("Geçersiz seçim. Lütfen tekrar deneyin.");
            }
        }
    }
}