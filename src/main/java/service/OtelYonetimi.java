package service;

import model.*; // Model paketindeki tüm sınıfları buraya dahil ediyoruz

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class OtelYonetimi {
    private Map<String, Musteri> musteriler = new HashMap<>();
    private Map<String, Oda> odalar = new HashMap<>();
    private BeklemeListesi beklemeListesi = new BeklemeListesi();
    private TreeMap<LocalDate, Rezervasyon> tamamlananRezervasyonlar = new TreeMap<>();

    private final String DOSYA_ADI = "otel_verileri.dat";

    public OtelYonetimi() {
        verileriYukle();
    }

    public void musteriKayitVeRezervasyon(String tc, String ad, String odaNo, String basTarih, String bitTarih) {
        try {
            LocalDate baslangic = LocalDate.parse(basTarih);
            LocalDate bitis = LocalDate.parse(bitTarih);

            musteriler.putIfAbsent(tc, new Musteri(tc, ad));
            Musteri musteri = musteriler.get(tc);

            Oda talepEdilenOda = odalar.get(odaNo);
            if (talepEdilenOda == null) {
                System.out.println("Hata: " + odaNo + " numaralı bir oda sistemde bulunmuyor.");
                return;
            }

            if (talepEdilenOda.musaitMi(baslangic, bitis)) {
                Rezervasyon yeniRezervasyon = new Rezervasyon(musteri, talepEdilenOda, baslangic, bitis);
                talepEdilenOda.aktifRezervasyonlar.add(yeniRezervasyon);
                System.out.println("Başarılı: Rezervasyon oluşturuldu! " + yeniRezervasyon);
            } else {
                System.out.println("Uyarı: " + odaNo + " numaralı oda bu tarihlerde DOLU!");
                beklemeListesi.kuyrugaEkle(musteri);
            }

            verileriKaydet();

        } catch (DateTimeParseException e) {
            System.out.println("Hata: Lütfen tarihleri YYYY-MM-DD formatında giriniz.");
        }
    }

    public void cikisYap(String odaNo, String tc) {
        Oda oda = odalar.get(odaNo);
        if (oda != null) {
            Rezervasyon iptalEdilecek = null;
            for (Rezervasyon rez : oda.aktifRezervasyonlar) {
                if (rez.musteri.tcNo.equals(tc)) {
                    iptalEdilecek = rez;
                    break;
                }
            }

            if (iptalEdilecek != null) {
                oda.aktifRezervasyonlar.remove(iptalEdilecek);
                tamamlananRezervasyonlar.put(iptalEdilecek.bitisTarihi, iptalEdilecek);
                System.out.println("Çıkış işlemi başarılı. Kayıt arşive (BST) aktarıldı.");
                verileriKaydet();
            } else {
                System.out.println("Hata: Bu odada bu müşteriye ait aktif kayıt bulunamadı.");
            }
        }
    }

    public void beklemeListesiniGoster() {
        System.out.println("\n--- Bekleme Listesi (Linked List) ---");
        beklemeListesi.listeyiYazdir();
    }

    public void gecmisRezervasyonlariGoster() {
        System.out.println("\n--- Geçmiş Rezervasyonlar (Kronolojik - BST/TreeMap) ---");
        if (tamamlananRezervasyonlar.isEmpty()) {
            System.out.println("Arşivde hiç kayıt yok.");
            return;
        }
        for (Map.Entry<LocalDate, Rezervasyon> entry : tamamlananRezervasyonlar.entrySet()) {
            System.out.println("Çıkış Tarihi: " + entry.getKey() + " | Detay: " + entry.getValue());
        }
    }

    private void verileriKaydet() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DOSYA_ADI))) {
            oos.writeObject(musteriler);
            oos.writeObject(odalar);
            oos.writeObject(beklemeListesi);
            oos.writeObject(tamamlananRezervasyonlar);
        } catch (IOException e) {
            System.out.println("Veriler kaydedilirken bir hata oluştu: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void verileriYukle() {
        File dosya = new File(DOSYA_ADI);
        if (dosya.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DOSYA_ADI))) {
                musteriler = (Map<String, Musteri>) ois.readObject();
                odalar = (Map<String, Oda>) ois.readObject();
                beklemeListesi = (BeklemeListesi) ois.readObject();
                tamamlananRezervasyonlar = (TreeMap<LocalDate, Rezervasyon>) ois.readObject();
                System.out.println("Sistem verileri dosyadan başarıyla yüklendi.");
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Veriler yüklenirken bir hata oluştu. Yeni veri seti oluşturuluyor...");
                varsayilanOdalariEkle();
            }
        } else {
            System.out.println("Kayıtlı veri bulunamadı. Temel sistem başlatılıyor...");
            varsayilanOdalariEkle();
            verileriKaydet();
        }
    }

    private void varsayilanOdalariEkle() {
        odalar.put("101", new Oda("101", 2));
        odalar.put("102", new Oda("102", 3));
    }
}