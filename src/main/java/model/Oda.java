package model;

import util.AralikAgaci; // YENİ: Ağacı oluşturduğumuz util paketinden çağırıyoruz
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Oda implements Serializable {
    private static final long serialVersionUID = 1L;

    private String odaNo;
    private int kapasite;
    private int gunlukFiyat; // YENİ: Odanın gecelik fiyatı
    private List<Rezervasyon> aktifRezervasyonlar;

    private transient AralikAgaci agac;

    // Kurucu metot
    public Oda(String odaNo, int kapasite, int gunlukFiyat) {
        this.odaNo = odaNo;
        this.kapasite = kapasite;
        this.gunlukFiyat = gunlukFiyat;
        this.aktifRezervasyonlar = new ArrayList<>();
        this.agac = new AralikAgaci();
    }

    // --- GETTER VE SETTER METOTLARI ---

    public String getOdaNo() {
        return odaNo;
    }

    public void setOdaNo(String odaNo) {
        this.odaNo = odaNo;
    }

    public int getKapasite() {
        return this.kapasite;
    }

    public void setKapasite(int kapasite) {
        this.kapasite = kapasite;
    }

    public int getGunlukFiyat() {
        return gunlukFiyat;
    }

    public void setGunlukFiyat(int gunlukFiyat) {
        this.gunlukFiyat = gunlukFiyat;
    }

    public List<Rezervasyon> getAktifRezervasyonlar() {
        return this.aktifRezervasyonlar;
    }

    // --- ODA VE REZERVASYON İŞLEMLERİ ---

    public void agaciYenidenOlustur() {
        this.agac = new AralikAgaci();
        if (this.aktifRezervasyonlar != null) {
            for (Rezervasyon r : this.aktifRezervasyonlar) {
                this.agac.ekle(r);
            }
        }
    }

    public void rezervasyonEkle(Rezervasyon rez) {
        aktifRezervasyonlar.add(rez);
        agac.ekle(rez);
    }

    public void rezervasyonSil(Rezervasyon rez) {
        aktifRezervasyonlar.remove(rez);
        agac = new AralikAgaci();
        for (Rezervasyon r : aktifRezervasyonlar) {
            agac.ekle(r);
        }
    }

    public boolean musaitMi(LocalDate baslangic, LocalDate bitis) {
        int oTarihtekiKisiSayisi = agac.cakisanSayisiniBul(baslangic, bitis);
        return oTarihtekiKisiSayisi < kapasite;
    }

    //  Belirli tarihlerde odada kaç kişi olduğunu döndürür.
    public int getTarihtekiKisiSayisi(LocalDate baslangic, LocalDate bitis) {
        return agac.cakisanSayisiniBul(baslangic, bitis);
    }
}