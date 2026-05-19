package util;

import model.Rezervasyon;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class KronolojikRezervasyonAgaci implements Serializable {
    private static final long serialVersionUID = 1L;

    private static class AgacDugumu implements Serializable {
        LocalDate tarih;
        List<Rezervasyon> rezervasyonlar;
        AgacDugumu sol, sag;

        public AgacDugumu(LocalDate tarih) {
            this.tarih = tarih;
            this.rezervasyonlar = new ArrayList<>();
        }
    }

    private AgacDugumu kok;

    public void ekle(Rezervasyon rez) {
        kok = ekleRec(kok, rez);
    }

    private AgacDugumu ekleRec(AgacDugumu dugum, Rezervasyon rez) {
        LocalDate hedefTarih = rez.getBitisTarihi();

        if (dugum == null) {
            AgacDugumu yeniDugum = new AgacDugumu(hedefTarih);
            yeniDugum.rezervasyonlar.add(rez);
            return yeniDugum;
        }

        if (hedefTarih.isBefore(dugum.tarih)) {
            dugum.sol = ekleRec(dugum.sol, rez); // Küçük tarihler sola
        } else if (hedefTarih.isAfter(dugum.tarih)) {
            dugum.sag = ekleRec(dugum.sag, rez); // Büyük tarihler sağa
        } else {

            dugum.rezervasyonlar.add(rez);
        }
        return dugum;
    }

    public List<Rezervasyon> toList() {
        List<Rezervasyon> kronolojikListe = new ArrayList<>();
        inOrderGez(kok, kronolojikListe);
        return kronolojikListe;
    }

    private void inOrderGez(AgacDugumu dugum, List<Rezervasyon> liste) {
        if (dugum != null) {
            inOrderGez(dugum.sol, liste);           // Önce en eski (sol) taraf
            liste.addAll(dugum.rezervasyonlar);     // Sonra kökteki rezervasyonlar
            inOrderGez(dugum.sag, liste);           // Sonra yeni (sağ) taraf
        }
    }

    public void temizle() {
        this.kok = null;
    }
}