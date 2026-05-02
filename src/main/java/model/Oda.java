package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Oda implements Serializable {
    private static final long serialVersionUID = 1L;

    public String odaNo;
    public int kapasite;
    public List<Rezervasyon> aktifRezervasyonlar;

    public Oda(String odaNo, int kapasite) {
        this.odaNo = odaNo;
        this.kapasite = kapasite;
        this.aktifRezervasyonlar = new ArrayList<>();
    }

    public boolean musaitMi(LocalDate baslangic, LocalDate bitis) {
        for (Rezervasyon rez : aktifRezervasyonlar) {
            if (baslangic.isBefore(rez.bitisTarihi) && rez.baslangicTarihi.isBefore(bitis)) {
                return false;
            }
        }
        return true;
    }
}