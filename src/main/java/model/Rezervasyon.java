package model;

import java.io.Serializable;
import java.time.LocalDate;

public class Rezervasyon implements Serializable {
    private static final long serialVersionUID = 1L;

    // 1. Değişkenler private yapıldı
    private Musteri musteri;
    private String odaNo;
    private LocalDate baslangicTarihi;
    private LocalDate bitisTarihi;

    public Rezervasyon(Musteri musteri, String odaNo, LocalDate baslangicTarihi, LocalDate bitisTarihi) {
        this.musteri = musteri;
        this.odaNo = odaNo;
        this.baslangicTarihi = baslangicTarihi;
        this.bitisTarihi = bitisTarihi;
    }

    // --- GETTER VE SETTER METOTLARI ---

    public Musteri getMusteri() {
        return musteri;
    }

    public void setMusteri(Musteri musteri) {
        this.musteri = musteri;
    }

    public String getOdaNo() {
        return odaNo;
    }

    public void setOdaNo(String odaNo) {
        this.odaNo = odaNo;
    }

    public LocalDate getBaslangicTarihi() {
        return baslangicTarihi;
    }

    public void setBaslangicTarihi(LocalDate baslangicTarihi) {
        this.baslangicTarihi = baslangicTarihi;
    }

    public LocalDate getBitisTarihi() {
        return bitisTarihi;
    }

    public void setBitisTarihi(LocalDate bitisTarihi) {
        this.bitisTarihi = bitisTarihi;
    }

    @Override
    public String toString() {
        // 2. ÖNEMLİ DÜZELTME: musteri.adSoyad yerine musteri.getAdSoyad() kullanıldı!
        return "Müşteri: " + musteri.getAdSoyad() + " | Oda: " + odaNo + " | Tarih: " + baslangicTarihi + " -> " + bitisTarihi;
    }
}