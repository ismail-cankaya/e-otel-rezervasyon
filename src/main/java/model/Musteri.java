package model;

import java.io.Serializable;

public class Musteri implements Serializable {
    private static final long serialVersionUID = 1L;

    //  Değişkenler private yapılarak dışarıdan doğrudan erişime kapatıldı
    private String tcNo;
    private String adSoyad;

    public Musteri(String tcNo, String adSoyad) {
        this.tcNo = tcNo;
        this.adSoyad = adSoyad;
    }

    //  Dışarıdan okuma yapabilmek için Getter metotları eklendi
    public String getTcNo() {
        return tcNo;
    }

    public String getAdSoyad() {
        return adSoyad;
    }

    // (Opsiyonel) Dışarıdan veriyi değiştirmek gerekirse diye Setter metotları eklendi
    public void setTcNo(String tcNo) {
        this.tcNo = tcNo;
    }

    public void setAdSoyad(String adSoyad) {
        this.adSoyad = adSoyad;
    }
}