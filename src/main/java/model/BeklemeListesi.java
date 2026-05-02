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
        System.out.println("-> " + musteri.adSoyad + " bekleme listesine (kuyruğa) eklendi.");
    }

    public void listeyiYazdir() {
        if (bas == null) {
            System.out.println("Bekleme listesi boş.");
            return;
        }
        BeklemeDugumu gecici = bas;
        int sira = 1;
        while (gecici != null) {
            System.out.println(sira + ". Sırada bekleyen: " + gecici.musteri.adSoyad);
            gecici = gecici.sonraki;
            sira++;
        }
    }
}