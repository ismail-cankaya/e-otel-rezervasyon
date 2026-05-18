package model;

import java.io.Serializable;

public class BeklemeListesi implements Serializable {
    private static final long serialVersionUID = 1L;

    // 1. DÜZELTME: Dugum sınıfı artık dışarıdan erişilemez bir İç Sınıf (Inner Class)
    private static class Dugum implements Serializable {
        private static final long serialVersionUID = 1L;

        // Dugum içindeki değişkenler de private olmalı
        private Musteri musteri;
        private Dugum ileri;

        public Dugum(Musteri musteri) {
            this.musteri = musteri;
            this.ileri = null;
        }

        // Getter ve Setter metotları
        public Musteri getMusteri() { return musteri; }
        public Dugum getIleri() { return ileri; }
        public void setIleri(Dugum ileri) { this.ileri = ileri; }
    }

    private Dugum bas;
    private Dugum son;

    // Kuyruğa (Sona) Ekleme İşlemi (FIFO mantığı)
    public void kuyrugaEkle(Musteri musteri) {
        Dugum yeniDugum = new Dugum(musteri);
        if (bas == null) {
            bas = son = yeniDugum;
        } else {
            son.setIleri(yeniDugum); // Dugum'un encapsulation'ı kullanıldı
            son = yeniDugum;
        }
    }

    // Arayüzde göstermek için String döndüren metot
    public String listeyiYazdir() {
        if (bas == null) {
            return "Bekleme listesi şu an boş.";
        }

        StringBuilder sb = new StringBuilder();
        Dugum gecici = bas;
        int sira = 1;

        while (gecici != null) {
            sb.append(sira).append(". Sırada | ")
                    .append("TC: ").append(gecici.getMusteri().getTcNo()).append(" | ")
                    .append("Ad Soyad: ").append(gecici.getMusteri().getAdSoyad())
                    .append("\n");

            gecici = gecici.getIleri();
            sira++;
        }

        return sb.toString();
    }
}