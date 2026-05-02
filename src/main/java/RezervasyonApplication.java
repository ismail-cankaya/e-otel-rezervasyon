import service.OtelYonetimi; // Service paketindeki OtelYonetimi sınıfını çağırıyoruz
import java.util.Scanner;

public class RezervasyonApplication {
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