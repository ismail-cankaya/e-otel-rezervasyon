package model;

import java.io.Serializable;
import java.time.LocalDate;

public class Rezervasyon implements Serializable {
    private static final long serialVersionUID = 1L;

    public Musteri musteri;
    public Oda oda;
    public LocalDate baslangicTarihi;
    public LocalDate bitisTarihi;

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