package model;

import java.io.Serializable;

public class Musteri implements Serializable {
    private static final long serialVersionUID = 1L;

    // 1. Değişkenler private yapılarak dışarıdan doğrudan erişime kapatıldı
    private String tcNo;
    private String adSoyad;

    public Musteri(String tcNo, String adSoyad) {
        this.tcNo = tcNo;
        this.adSoyad = adSoyad;
    }

    // 2. Dışarıdan okuma yapabilmek için Getter metotları eklendi
    public String getTcNo() {
        return tcNo;
    }

    public String getAdSoyad() {
        return adSoyad;
    }

    // 3. (Opsiyonel) Dışarıdan veriyi değiştirmek gerekirse diye Setter metotları eklendi
    public void setTcNo(String tcNo) {
        // İleride buraya "TC No 11 hane olmalı" gibi güvenlik kontrolleri ekleyebilirsiniz.
        this.tcNo = tcNo;
    }

    public void setAdSoyad(String adSoyad) {
        this.adSoyad = adSoyad;
    }
}