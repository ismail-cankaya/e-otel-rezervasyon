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
        if (subeAdi == null) {
            return null;
        }
        return subeler.get(subeAdi.trim());
    }

    // --- HATAYI ÇÖZEN KISIM BURASI ---
    // Artık 'void' değil, JavaFX arayüzünde göstermek için 'String' döndürüyor!
    // ... (Dosyanın üst kısımları aynı) ...

    public String merkeziRaporOlustur() {
        StringBuilder rapor = new StringBuilder();
        rapor.append("=========================================\n");
        rapor.append("      OTEL ZİNCİRİ MERKEZİ RAPORU        \n");
        rapor.append("=========================================\n\n");

        int genelKayitli = 0;
        int genelAktif = 0;
        int genelArsiv = 0;

        for (Map.Entry<String, OtelYonetimi> entry : subeler.entrySet()) {
            String subeAdi = entry.getKey();
            OtelYonetimi sube = entry.getValue();

            int kayitli = sube.getTümZamanlarKayitliMusteri();
            int aktif = sube.getAktifKonaklayanSayisi();
            int arsiv = sube.getTamamlananRezervasyonSayisi();

            genelKayitli += kayitli;
            genelAktif += aktif;
            genelArsiv += arsiv;

            rapor.append("📍 Şube: ").append(subeAdi).append("\n");
            rapor.append("  -> Tüm Zamanlarda Kayıtlı Müşteri: ").append(kayitli).append("\n");
            rapor.append("  -> Aktif Konaklayan Müşteri: ").append(aktif).append("\n");
            rapor.append("  -> Arşivdeki (Geçmiş) İşlem Sayısı: ").append(arsiv).append("\n");
            rapor.append("-----------------------------------------\n");
        }

        rapor.append("\n📊 GENEL ZİNCİR ÖZETİ:\n");
        rapor.append("Toplam Sistemdeki Müşteri Hesabı: ").append(genelKayitli).append("\n");
        rapor.append("Şu An Aktif Kalan Toplam Müşteri: ").append(genelAktif).append("\n");
        rapor.append("Bugüne Kadar Tamamlanan Rezervasyon: ").append(genelArsiv).append("\n");
        rapor.append("=========================================\n");

        return rapor.toString();
    }
}