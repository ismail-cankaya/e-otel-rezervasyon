package model;

import java.io.Serializable;

// Bekleme listesindeki her bir kişiyi tutacak düğüm (Node) yapısı
class Dugum implements Serializable {
    private static final long serialVersionUID = 1L;
    Musteri musteri;
    Dugum ileri;

    public Dugum(Musteri musteri) {
        this.musteri = musteri;
        this.ileri = null;
    }
}

public class BeklemeListesi implements Serializable {
    private static final long serialVersionUID = 1L;
    private Dugum bas;
    private Dugum son;

    // Kuyruğa (Sona) Ekleme İşlemi (FIFO mantığı)
    public void kuyrugaEkle(Musteri musteri) {
        Dugum yeniDugum = new Dugum(musteri);
        if (bas == null) {
            bas = son = yeniDugum;
        } else {
            son.ileri = yeniDugum;
            son = yeniDugum;
        }
    }

    // --- HATAYI ÇÖZEN KISIM BURASI ---
    // Artık 'void' değil, arayüzde (JavaFX) göstermek için 'String' döndürüyor!
    public String listeyiYazdir() {
        if (bas == null) {
            return "Bekleme listesi şu an boş.";
        }

        StringBuilder sb = new StringBuilder();
        Dugum gecici = bas;
        int sira = 1;

        while (gecici != null) {
            sb.append(sira).append(". Sırada | ")
                    .append("TC: ").append(gecici.musteri.tcNo).append(" | ")
                    .append("Ad Soyad: ").append(gecici.musteri.adSoyad)
                    .append("\n");

            gecici = gecici.ileri;
            sira++;
        }

        return sb.toString();
    }
}
