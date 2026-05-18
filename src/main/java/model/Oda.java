package model;

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

    // Kurucu metot güncellendi (Fiyat parametresi eklendi)
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

    // YENİ EKLENEN METOT: O tarihlerde odada kaç kişi olduğunu döndürür
    public int getTarihtekiKisiSayisi(LocalDate baslangic, LocalDate bitis) {
        return agac.cakisanSayisiniBul(baslangic, bitis);
    }

    // --- INTERVAL TREE (ARALIK AĞACI) ---

    private static class AralikAgaciDugumu implements Serializable {
        Rezervasyon rez;
        LocalDate maxBitis;
        AralikAgaciDugumu sol, sag;

        public AralikAgaciDugumu(Rezervasyon rez) {
            this.rez = rez;
            this.maxBitis = rez.getBitisTarihi();
        }
    }

    private static class AralikAgaci implements Serializable {
        AralikAgaciDugumu kok;

        public void ekle(Rezervasyon yeniRez) {
            kok = ekleRec(kok, yeniRez);
        }

        private AralikAgaciDugumu ekleRec(AralikAgaciDugumu dugum, Rezervasyon yeniRez) {
            if (dugum == null) return new AralikAgaciDugumu(yeniRez);

            if (yeniRez.getBaslangicTarihi().isBefore(dugum.rez.getBaslangicTarihi())) {
                dugum.sol = ekleRec(dugum.sol, yeniRez);
            } else {
                dugum.sag = ekleRec(dugum.sag, yeniRez);
            }

            if (dugum.maxBitis.isBefore(yeniRez.getBitisTarihi())) {
                dugum.maxBitis = yeniRez.getBitisTarihi();
            }
            return dugum;
        }

        public int cakisanSayisiniBul(LocalDate bas, LocalDate bit) {
            return cakisanSayisiRec(kok, bas, bit);
        }

        private int cakisanSayisiRec(AralikAgaciDugumu dugum, LocalDate bas, LocalDate bit) {
            if (dugum == null) return 0;

            int cakismaSayisi = 0;

            if (bas.isBefore(dugum.rez.getBitisTarihi()) && bit.isAfter(dugum.rez.getBaslangicTarihi())) {
                cakismaSayisi++;
            }

            if (dugum.sol != null && dugum.sol.maxBitis.isAfter(bas)) {
                cakismaSayisi += cakisanSayisiRec(dugum.sol, bas, bit);
            }

            if (dugum.sag != null && bit.isAfter(dugum.rez.getBaslangicTarihi())) {
                cakismaSayisi += cakisanSayisiRec(dugum.sag, bas, bit);
            }

            return cakismaSayisi;
        }
    }
}