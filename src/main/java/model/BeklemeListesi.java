package model;

import java.io.Serializable;

public class BeklemeListesi implements Serializable {
    private static final long serialVersionUID = 1L;

    private BeklemeDugumu bas; // Head
    private BeklemeDugumu son; // Tail

    public void kuyrugaEkle(Musteri musteri) {
        BeklemeDugumu yeniDugum = new BeklemeDugumu(musteri);
        if (bas == null) {
            bas = son = yeniDugum;
        } else {
            son.sonraki = yeniDugum;
            son = yeniDugum;
        }
    }

    // Konsola değil, arayüze (GUI) metin döndürmek için String yaptık.
    public String listeyiYazdir() {
        if (bas == null) {
            return "Bekleme listesi boş.";
        }
        StringBuilder sb = new StringBuilder();
        BeklemeDugumu gecici = bas;
        int sira = 1;
        while (gecici != null) {
            sb.append(sira).append(". Sırada bekleyen: ").append(gecici.musteri.adSoyad).append("\n");
            gecici = gecici.sonraki;
            sira++;
        }
        return sb.toString();
    }
}