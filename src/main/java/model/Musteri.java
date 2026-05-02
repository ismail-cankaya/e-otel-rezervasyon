package model;

import java.io.Serializable;

public class Musteri implements Serializable {
    private static final long serialVersionUID = 1L;

    public String tcNo;
    public String adSoyad;

    public Musteri(String tcNo, String adSoyad) {
        this.tcNo = tcNo;
        this.adSoyad = adSoyad;
    }
}
