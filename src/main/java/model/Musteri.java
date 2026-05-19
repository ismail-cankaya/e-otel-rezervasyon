package model;

import java.io.Serializable;

public class Musteri implements Serializable {
    private static final long serialVersionUID = 1L;

    private String tcNo;
    private String adSoyad;

    public Musteri(String tcNo, String adSoyad) {
        this.tcNo = tcNo;
        this.adSoyad = adSoyad;
    }

    public String getTcNo() {
        return tcNo;
    }

    public String getAdSoyad() {
        return adSoyad;
    }

    public void setTcNo(String tcNo) {
        this.tcNo = tcNo;
    }

    public void setAdSoyad(String adSoyad) {
        this.adSoyad = adSoyad;
    }
}