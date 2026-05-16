package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Oda implements Serializable {
    private static final long serialVersionUID = 1L;

    public String odaNo;
    public int kapasite;

    // --- YENİ EKLENEN: O anki doluluğu takip etmek için ---
    private int mevcutKisiSayisi;

    // Geçmişi ve TC'leri kolay listelemek için klasik listemiz
    public List<Rezervasyon> aktifRezervasyonlar;

    // HOCANIN İSTEDİĞİ: Çakışma kontrolünü çok hızlı yapacak Aralık Ağacı
    private AralikAgaci agac;

    public Oda(String odaNo, int kapasite) {
        this.odaNo = odaNo;
        this.kapasite = kapasite;
        this.mevcutKisiSayisi = 0; // Başlangıçta 0 kişi
        this.aktifRezervasyonlar = new ArrayList<>();
        this.agac = new AralikAgaci();
    }

    // --- YENİ EKLENEN: GETTER VE SETTER METOTLARI ---

    public int getKapasite() {
        return this.kapasite;
    }

    public int getMevcutKisiSayisi() {
        return this.mevcutKisiSayisi;
    }

    public void kisiEkle() {
        this.mevcutKisiSayisi++;
    }

    public void kisiCikar() {
        if (this.mevcutKisiSayisi > 0) {
            this.mevcutKisiSayisi--;
        }
    }
    // ------------------------------------------------

    // Müşteri geldiğinde hem listeye hem de Çakışma Ağacına (Interval Tree) eklenir
    public void rezervasyonEkle(Rezervasyon rez) {
        aktifRezervasyonlar.add(rez);
        agac.ekle(rez);
    }

    // Müşteri çıkış yaptığında listeden silinir, ağaç güncellenir
    public void rezervasyonSil(Rezervasyon rez) {
        aktifRezervasyonlar.remove(rez);
        agac = new AralikAgaci(); // Ağacı sıfırla
        for (Rezervasyon r : aktifRezervasyonlar) {
            agac.ekle(r); // Kalanları ağaca geri diz
        }
    }

    // ÇAKIŞMA KONTROLÜ: Artık for döngüsüyle değil, Aralık Ağacı ile (O(log N) hızında) yapılıyor!
    public boolean musaitMi(LocalDate baslangic, LocalDate bitis) {
        return !agac.cakismaVarMi(baslangic, bitis);
    }

    // =========================================================
    // --- INTERVAL TREE (ARALIK AĞACI) ALTYAPISI (İÇ SINIFLAR) ---
    // =========================================================

    private static class AralikAgaciDugumu implements Serializable {
        Rezervasyon rez;
        LocalDate maxBitis;
        AralikAgaciDugumu sol, sag;

        public AralikAgaciDugumu(Rezervasyon rez) {
            this.rez = rez;
            this.maxBitis = rez.bitisTarihi;
        }
    }

    private static class AralikAgaci implements Serializable {
        AralikAgaciDugumu kok;

        public void ekle(Rezervasyon yeniRez) {
            kok = ekleRec(kok, yeniRez);
        }

        private AralikAgaciDugumu ekleRec(AralikAgaciDugumu dugum, Rezervasyon yeniRez) {
            if (dugum == null) return new AralikAgaciDugumu(yeniRez);

            if (yeniRez.baslangicTarihi.isBefore(dugum.rez.baslangicTarihi)) {
                dugum.sol = ekleRec(dugum.sol, yeniRez);
            } else {
                dugum.sag = ekleRec(dugum.sag, yeniRez);
            }

            if (dugum.maxBitis.isBefore(yeniRez.bitisTarihi)) {
                dugum.maxBitis = yeniRez.bitisTarihi;
            }
            return dugum;
        }

        public boolean cakismaVarMi(LocalDate bas, LocalDate bit) {
            return cakismaKontrolRec(kok, bas, bit);
        }

        private boolean cakismaKontrolRec(AralikAgaciDugumu dugum, LocalDate bas, LocalDate bit) {
            if (dugum == null) return false; // Çakışma yok

            // Çakışma formülü: Biri bitmeden diğeri başlıyorsa çakışma vardır!
            if (bas.isBefore(dugum.rez.bitisTarihi) && bit.isAfter(dugum.rez.baslangicTarihi)) {
                return true;
            }

            if (dugum.sol != null && dugum.sol.maxBitis.isAfter(bas)) {
                return cakismaKontrolRec(dugum.sol, bas, bit);
            }

            return cakismaKontrolRec(dugum.sag, bas, bit);
        }
    }
}