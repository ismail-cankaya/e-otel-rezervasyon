package application;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

import service.MerkeziSistem;
import service.OtelYonetimi;

public class RezervasyonConsole {
    private MerkeziSistem merkeziSistem;
    private Scanner scanner;

    public RezervasyonConsole() {
        this.merkeziSistem = new MerkeziSistem();
        this.scanner = new Scanner(System.in);
    }

    public void basla() {
        while (true) {
            menuAna();
        }
    }

    private void menuAna() {
        temizleEkran();
        ayrilim();
        System.out.println("       🏨 OTEL ZİNCİRİ YÖNETİM SİSTEMİ 🏨");
        ayrilim();
        System.out.println();
        System.out.println("0 - Merkezi Rapor Görüntüle");
        System.out.println("1 - Bayburt Şubesi");
        System.out.println("2 - Çorlu Şubesi");
        System.out.println("3 - Los Angeles Şubesi");
        System.out.println("4 - Las Vegas Şubesi");
        System.out.println("5 - Çıkış");
        System.out.println();
        kesikli();
        System.out.print("Seçim yapınız: ");

        try {
            String secim = scanner.nextLine().trim();

            switch (secim) {
                case "0":
                    merkeziRaporGoster();
                    break;
                case "1":
                    subeMenusu("Bayburt");
                    break;
                case "2":
                    subeMenusu("Çorlu");
                    break;
                case "3":
                    subeMenusu("Los Angeles");
                    break;
                case "4":
                    subeMenusu("Las Vegas");
                    break;
                case "5":
                    cikis();
                    break;
                default:
                    System.out.println("\n❌ Geçersiz seçim! Lütfen 0-5 arasında bir sayı girin.");
                    bekle();
            }
        } catch (Exception e) {
            System.out.println("\n❌ Hata: " + e.getMessage());
            bekle();
        }
    }

    private void merkeziRaporGoster() {
        temizleEkran();
        ayrilim();
        String rapor = merkeziSistem.merkeziRaporOlustur();
        System.out.println(rapor);
        ayrilim();
        bekle();
    }

    private void subeMenusu(String subeAdi) {
        OtelYonetimi sube = merkeziSistem.subeGetir(subeAdi);
        if (sube == null) {
            System.out.println("❌ Şube bulunamadı!");
            bekle();
            return;
        }

        while (true) {
            temizleEkran();
            ayrilim();
            System.out.println("📍 " + subeAdi.toUpperCase() + " ŞUBESI");
            ayrilim();
            System.out.println();
            System.out.println("1 - Yeni Rezervasyon Yap");
            System.out.println("2 - Odadan Çıkış Yap");
            System.out.println("3 - Bekleme Listesini Görüntüle");
            System.out.println("4 - Arşivlenmiş (Geçmiş) Rezervasyonları Görüntüle");
            System.out.println("5 - Şube Değiştir");
            System.out.println("6 - Çıkış");
            System.out.println();
            kesikli();
            System.out.print("Seçim yapınız: ");

            try {
                String secim = scanner.nextLine().trim();

                switch (secim) {
                    case "1":
                        yeniRezervasyonYap(sube);
                        break;
                    case "2":
                        odadanCikisYap(sube);
                        break;
                    case "3":
                        beklemeListesiniGoster(sube);
                        break;
                    case "4":
                        gecmisRezervasyonlariGoster(sube);
                        break;
                    case "5":
                        return;
                    case "6":
                        cikis();
                        break;
                    default:
                        System.out.println("\n❌ Geçersiz seçim! Lütfen 1-6 arasında bir sayı girin.");
                        bekle();
                }
            } catch (Exception e) {
                System.out.println("\n❌ Hata: " + e.getMessage());
                bekle();
            }
        }
    }

    private void yeniRezervasyonYap(OtelYonetimi sube) {
        temizleEkran();
        ayrilim();
        System.out.println("      YENİ REZERVASYON OLUŞTUR");
        ayrilim();
        System.out.println();

        try {
            // TC Numarası Ald
            String tc = alinacakTC();
            if (tc == null) return;

            // Ad Soyad Al
            System.out.print("👤 Ad Soyad: ");
            String ad = scanner.nextLine().trim();
            if (ad.isEmpty()) {
                System.out.println("\n❌ Ad soyad boş olamaz!");
                bekle();
                return;
            }

            // Oda Numarası Al
            System.out.print("🚪 Oda Numarası (1-25): ");
            String odaNo = scanner.nextLine().trim();
            if (!odaNo.matches("^\\d+$") || Integer.parseInt(odaNo) < 1 || Integer.parseInt(odaNo) > 25) {
                System.out.println("\n❌ Oda numarası 1 ile 25 arasında olmalıdır!");
                bekle();
                return;
            }

            // Başlangıç Tarihi Al
            LocalDate basTarih = alinacakTarih("Başlangıç Tarihi (YYYY-MM-DD): ");
            if (basTarih == null) return;

            // Bitiş Tarihi Al
            LocalDate bitTarih = alinacakTarih("Bitiş Tarihi (YYYY-MM-DD): ");
            if (bitTarih == null) return;

            // Tarih kontrolü
            if (!bitTarih.isAfter(basTarih)) {
                System.out.println("\n❌ Bitiş tarihi, başlangıç tarihinden sonra olmalıdır!");
                bekle();
                return;
            }

            // Rezervasyon işlemini yap
            System.out.println();
            kesikli();
            String sonuc = sube.musteriKayitVeRezervasyon(tc, ad, odaNo, basTarih.toString(), bitTarih.toString());
            System.out.println(sonuc);
            ayrilim();
            bekle();

        } catch (Exception e) {
            System.out.println("\n❌ Hata: " + e.getMessage());
            bekle();
        }
    }

    private void odadanCikisYap(OtelYonetimi sube) {
        temizleEkran();
        ayrilim();
        System.out.println("      ODADAN ÇIKIS YAP");
        ayrilim();
        System.out.println();

        try {
            System.out.print("🚪 Oda Numarası (1-25): ");
            String odaNo = scanner.nextLine().trim();
            if (!odaNo.matches("^\\d+$") || Integer.parseInt(odaNo) < 1 || Integer.parseInt(odaNo) > 25) {
                System.out.println("\n❌ Oda numarası 1 ile 25 arasında olmalıdır!");
                bekle();
                return;
            }

            String tc = alinacakTC();
            if (tc == null) return;

            System.out.println();
            kesikli();
            String sonuc = sube.cikisYap(odaNo, tc);
            System.out.println(sonuc);
            ayrilim();
            bekle();

        } catch (Exception e) {
            System.out.println("\n❌ Hata: " + e.getMessage());
            bekle();
        }
    }

    private void beklemeListesiniGoster(OtelYonetimi sube) {
        temizleEkran();
        ayrilim();
        System.out.println("      BEKLEME LİSTESİ");
        ayrilim();
        System.out.println();
        String liste = sube.beklemeListesiniGoster();
        System.out.println(liste);
        ayrilim();
        bekle();
    }

    private void gecmisRezervasyonlariGoster(OtelYonetimi sube) {
        temizleEkran();
        ayrilim();
        System.out.println("      ARŞİVLENMİŞ (GEÇMİŞ) REZERVASYONLAR");
        ayrilim();
        System.out.println();
        String arsiv = sube.gecmisRezervasyonlariGoster();
        System.out.println(arsiv);
        ayrilim();
        bekle();
    }

    private String alinacakTC() {
        while (true) {
            System.out.print("🆔 TC Numarası (11 hane): ");
            String tc = scanner.nextLine().trim();

            if (tc.isEmpty()) {
                System.out.println("❌ TC numarası boş olamaz!");
                continue;
            }

            if (!tc.matches("^\\d{11}$")) {
                System.out.println("❌ TC numarası 11 haneli ve yalnızca rakamlardan oluşmalıdır!");
                continue;
            }

            return tc;
        }
    }

    private LocalDate alinacakTarih(String mesaj) {
        while (true) {
            System.out.print(mesaj);
            String tarihStr = scanner.nextLine().trim();

            try {
                LocalDate tarih = LocalDate.parse(tarihStr);
                if (tarih.isBefore(LocalDate.now())) {
                    System.out.println("❌ Geçmiş tarih seçemezsiniz!");
                    continue;
                }
                return tarih;
            } catch (DateTimeParseException e) {
                System.out.println("❌ Geçersiz tarih formatı! Lütfen YYYY-MM-DD formatında girin (örn: 2026-05-20)");
                continue;
            }
        }
    }

    private void cikis() {
        temizleEkran();
        ayrilim();
        System.out.println("        HOŞÇA KALIN 👋");
        ayrilim();
        System.out.println();
        scanner.close();
        System.exit(0);
    }

    private void temizleEkran() {
        for (int i = 0; i < 3; i++) {
            System.out.println();
        }
    }

    private void ayrilim() {
        System.out.println("=========================================");
    }

    private void kesikli() {
        System.out.println("-----------------------------------------");
    }

    private void bekle() {
        System.out.println();
        System.out.print("Devam etmek için ENTER tuşuna basınız...");
        scanner.nextLine();
    }

    public static void main(String[] args) {
        RezervasyonConsole ui = new RezervasyonConsole();
        ui.basla();
    }
}

