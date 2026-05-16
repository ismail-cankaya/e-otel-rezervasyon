package service;

import java.util.HashMap;
import java.util.Map;

public class MerkeziSistem {
    // Şubeleri tutacağımız ana veri yapısı
    private Map<String, OtelYonetimi> subeler;

    public MerkeziSistem() {
        this.subeler = new HashMap<>();

        // Sisteme şubeleri kendi özel JSON dosyalarıyla ekliyoruz.
        subeler.put("Bayburt", new OtelYonetimi("Bayburt.json"));
        subeler.put("Los Angeles", new OtelYonetimi("Los Angeles.json"));
        subeler.put("Las Vegas", new OtelYonetimi("Las Vegas.json"));
        subeler.put("Çorlu", new OtelYonetimi("Çorlu.json"));
    }

    public OtelYonetimi subeGetir(String subeAdi) {
        return subeler.get(subeAdi);
    }

    // --- HATAYI ÇÖZEN KISIM BURASI ---
    // Artık 'void' değil, JavaFX arayüzünde göstermek için 'String' döndürüyor!
    public String merkeziRaporOlustur() {
        StringBuilder rapor = new StringBuilder();
        rapor.append("=========================================\n");
        rapor.append("      OTEL ZİNCİRİ MERKEZİ RAPORU        \n");
        rapor.append("=========================================\n\n");

        int toplamAktifMusteri = 0;
        int toplamGecmisRezervasyon = 0;

        for (Map.Entry<String, OtelYonetimi> entry : subeler.entrySet()) {
            String subeAdi = entry.getKey();
            OtelYonetimi sube = entry.getValue();

            int subeAktif = sube.getAktifMusteriSayisi();
            int subeGecmis = sube.getTamamlananRezervasyonSayisi();

            toplamAktifMusteri += subeAktif;
            toplamGecmisRezervasyon += subeGecmis;

            rapor.append("📍 Şube: ").append(subeAdi).append("\n");
            rapor.append("  -> Toplam Kayıtlı Müşteri: ").append(subeAktif).append("\n");
            rapor.append("  -> Arşivdeki (Geçmiş) İşlem: ").append(subeGecmis).append("\n");
            rapor.append("-----------------------------------------\n");
        }

        rapor.append("\n📊 GENEL TOPLAM KAYITLI MÜŞTERİ: ").append(toplamAktifMusteri).append("\n");
        rapor.append("📊 GENEL TOPLAM ARŞİV KAYDI: ").append(toplamGecmisRezervasyon).append("\n");
        rapor.append("=========================================\n");

        return rapor.toString();
    }
}