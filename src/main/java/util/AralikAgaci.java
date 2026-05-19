package util;

import model.Rezervasyon;
import java.io.Serializable;
import java.time.LocalDate;

// --- INTERVAL TREE (ARALIK AĞACI) ---
public class AralikAgaci implements Serializable {

    private static class AralikAgaciDugumu implements Serializable {
        Rezervasyon rez;
        LocalDate maxBitis;
        AralikAgaciDugumu sol, sag;

        public AralikAgaciDugumu(Rezervasyon rez) {
            this.rez = rez;
            this.maxBitis = rez.getBitisTarihi();
        }
    }

    private AralikAgaciDugumu kok;

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