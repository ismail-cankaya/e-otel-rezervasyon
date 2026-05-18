package model;

import java.io.Serializable;
import java.time.LocalDate;

public class BeklemeListesi implements Serializable {
    private static final long serialVersionUID = 1L;

    // Bekleme talebini detaylandırdık
    private static class Dugum implements Serializable {
        private static final long serialVersionUID = 1L;
        private Musteri musteri;
        private String talepEdilenOdaNo;
        private LocalDate baslangic;
        private LocalDate bitis;
        private Dugum ileri;

        public Dugum(Musteri musteri, String talepEdilenOdaNo, LocalDate baslangic, LocalDate bitis) {
            this.musteri = musteri;
            this.talepEdilenOdaNo = talepEdilenOdaNo;
            this.baslangic = baslangic;
            this.bitis = bitis;
            this.ileri = null;
        }

        public Musteri getMusteri() { return musteri; }
        public String getTalepEdilenOdaNo() { return talepEdilenOdaNo; }
        public LocalDate getBaslangic() { return baslangic; }
        public LocalDate getBitis() { return bitis; }
        public Dugum getIleri() { return ileri; }
        public void setIleri(Dugum ileri) { this.ileri = ileri; }
    }

    private Dugum bas; // Listenin başı
    private Dugum son; // Listenin sonu

    // HATA DÜZELTİLDİ: Parametre isimleri 'baslangicTarihi' ve 'bitisTarihi' yapıldı
    public void kuyrugaEkle(Musteri musteri, String odaNo, LocalDate baslangicTarihi, LocalDate bitisTarihi) {
        Dugum yeniDugum = new Dugum(musteri, odaNo, baslangicTarihi, bitisTarihi);
        if (this.bas == null) {
            this.bas = this.son = yeniDugum;
        } else {
            this.son.setIleri(yeniDugum);
            this.son = yeniDugum;
        }
    }

    // Oda boşaldığında bekleme listesini tarar, o odayı o tarihlerde isteyen ilk kişiyi listeden çıkarıp döndürür
    public Rezervasyon siradakiUygunTalebiAl(Oda bosalanOda) {
        Dugum gecici = this.bas;
        Dugum onceki = null;

        while (gecici != null) {
            // Eğer bekleyen kişi bu odayı istiyorsa ve oda o tarihlerde artık müsaitse
            if (gecici.getTalepEdilenOdaNo().equals(bosalanOda.getOdaNo()) &&
                    bosalanOda.musaitMi(gecici.getBaslangic(), gecici.getBitis())) {

                // Onu listeden kopar
                if (onceki == null) {
                    this.bas = gecici.getIleri(); // Baştakiyse
                } else {
                    onceki.setIleri(gecici.getIleri()); // Aradaysa
                }
                if (gecici == this.son) this.son = onceki; // Sondakiyse

                // Talebi bir rezervasyona dönüştürüp yolla
                return new Rezervasyon(gecici.getMusteri(), bosalanOda.getOdaNo(), gecici.getBaslangic(), gecici.getBitis());
            }
            onceki = gecici;
            gecici = gecici.getIleri();
        }
        return null; // Uygun bekleyen yok
    }

    public String listeyiYazdir() {
        if (this.bas == null) return "Bekleme listesi şu an boş.";
        StringBuilder sb = new StringBuilder();
        Dugum gecici = this.bas;
        int sira = 1;
        while (gecici != null) {
            sb.append(sira).append(". Sırada | TC: ").append(gecici.getMusteri().getTcNo())
                    .append(" | Ad: ").append(gecici.getMusteri().getAdSoyad())
                    .append(" | Beklediği Oda: ").append(gecici.getTalepEdilenOdaNo())
                    .append(" (").append(gecici.getBaslangic()).append(" - ").append(gecici.getBitis()).append(")\n");
            gecici = gecici.getIleri();
            sira++;
        }
        return sb.toString();
    }
}