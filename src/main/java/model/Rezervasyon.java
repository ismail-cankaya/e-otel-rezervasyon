package model;

import java.io.Serializable;
import java.time.LocalDate;

public class Rezervasyon implements Serializable {
    private static final long serialVersionUID = 1L;

    public Musteri musteri;
    public String odaNo;
    public LocalDate baslangicTarihi;
    public LocalDate bitisTarihi;

    public Rezervasyon(Musteri musteri, String odaNo, LocalDate baslangicTarihi, LocalDate bitisTarihi) {
        this.musteri = musteri;
        this.odaNo = odaNo;
        this.baslangicTarihi = baslangicTarihi;
        this.bitisTarihi = bitisTarihi;
    }

    // Ekrana RAM adresi yerine düzgün metin yazdırmasını sağlayan sihirli metot:
    @Override
    public String toString() {
        return "Müşteri: " + musteri.adSoyad + " | Oda: " + odaNo + " | Tarih: " + baslangicTarihi + " -> " + bitisTarihi;
    }
}