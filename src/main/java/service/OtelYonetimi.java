package service;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import model.*;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    private final String DOSYA_ADI = "otel_verileri.json";
    private final Gson gson;

    public OtelYonetimi() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>) (src, typeOfSrc, context) -> new JsonPrimitive(src.toString()))
                .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, typeOfT, context) -> LocalDate.parse(json.getAsString()))
                .setPrettyPrinting()
                .create();

        verileriYukle();
    }

    private void verileriKaydet() {
        try (Writer writer = Files.newBufferedWriter(Paths.get(DOSYA_ADI))) {
            Map<String, Object> tumVeri = new HashMap<>();
            tumVeri.put("musteriler", musteriler);
            tumVeri.put("odalar", odalar);
            tumVeri.put("beklemeListesi", beklemeListesi);
            tumVeri.put("arsiv", tamamlananRezervasyonlar);
            gson.toJson(tumVeri, writer);
        } catch (IOException e) {
            System.out.println("JSON Kayıt Hatası: " + e.getMessage());
        }
    }

    private void verileriYukle() {
        File dosya = new File(DOSYA_ADI);
        if (!dosya.exists()) {
            System.out.println("Kayıtlı veri bulunamadı. 1-100 arası odalar oluşturuluyor...");
            varsayilanOdalariEkle();
            verileriKaydet();
            return;
        }

        try (Reader reader = Files.newBufferedReader(Paths.get(DOSYA_ADI))) {
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();

            Type musterilerType = new TypeToken<Map<String, Musteri>>(){}.getType();
            musteriler = gson.fromJson(jsonObject.get("musteriler"), musterilerType);

            Type odalarType = new TypeToken<Map<String, Oda>>(){}.getType();
            odalar = gson.fromJson(jsonObject.get("odalar"), odalarType);

            Type arsivType = new TypeToken<TreeMap<LocalDate, Rezervasyon>>(){}.getType();
            tamamlananRezervasyonlar = gson.fromJson(jsonObject.get("arsiv"), arsivType);

            if (musteriler == null) musteriler = new HashMap<>();
            if (odalar == null) odalar = new HashMap<>();
            if (tamamlananRezervasyonlar == null) tamamlananRezervasyonlar = new TreeMap<>();

            System.out.println("Sistem: Veriler dosyadan yüklendi. Oda sayısı: " + odalar.size());
        } catch (Exception e) {
            System.out.println("Veri yüklenirken hata oluştu, odalar yeniden oluşturuluyor.");
            varsayilanOdalariEkle();
        }
    }

    public void musteriKayitVeRezervasyon(String tc, String ad, String odaNo, String basTarih, String bitTarih) {
        try {
            LocalDate baslangic = LocalDate.parse(basTarih);
            LocalDate bitis = LocalDate.parse(bitTarih);

            musteriler.putIfAbsent(tc, new Musteri(tc, ad));
            Musteri musteri = musteriler.get(tc);

            Oda talepEdilenOda = odalar.get(odaNo);
            if (talepEdilenOda == null) {
                System.out.println("Hata: " + odaNo + " numaralı bir oda sistemde bulunmuyor!");
                return;
            }

            if (talepEdilenOda.musaitMi(baslangic, bitis)) {
                Rezervasyon yeniRezervasyon = new Rezervasyon(musteri, talepEdilenOda.odaNo, baslangic, bitis);
                talepEdilenOda.aktifRezervasyonlar.add(yeniRezervasyon);
                System.out.println("Başarılı: " + odaNo + " numaralı odaya rezervasyon yapıldı.");
                verileriKaydet();
            } else {
                System.out.println("Uyarı: Oda dolu! Müşteri bekleme listesine alınıyor.");
                beklemeListesi.kuyrugaEkle(musteri);
                verileriKaydet();
            }
        } catch (DateTimeParseException e) {
            System.out.println("Hata: Lütfen tarihleri YYYY-MM-DD formatında giriniz.");
        } catch (Exception e) {
            System.out.println("Hata: " + e.getMessage());
        }
    }

    public void cikisYap(String odaNo, String tc) {
        Oda oda = odalar.get(odaNo);
        if (oda == null) {
            System.out.println("Hata: '" + odaNo + "' numaralı oda bulunamadı!");
            return;
        }

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
            System.out.println("Çıkış başarılı. Kayıt arşive aktarıldı.");
            verileriKaydet();
        } else {
            System.out.println("Hata: Bu odada bu TC ile kayıtlı aktif bir rezervasyon yok.");
        }
    }

    // --- SİLİNEN METODLARI GERİ EKLEDİK ---

    public void beklemeListesiniGoster() {
        System.out.println("\n--- Bekleme Listesi ---");
        if (beklemeListesi != null) {
            beklemeListesi.listeyiYazdir();
        }
    }

    public void gecmisRezervasyonlariGoster() {
        System.out.println("\n--- Geçmiş Rezervasyonlar (Arşiv) ---");
        if (tamamlananRezervasyonlar == null || tamamlananRezervasyonlar.isEmpty()) {
            System.out.println("Arşivde hiç kayıt yok.");
            return;
        }
        for (Map.Entry<LocalDate, Rezervasyon> entry : tamamlananRezervasyonlar.entrySet()) {
            System.out.println("Çıkış Tarihi: " + entry.getKey() + " | Detay: " + entry.getValue());
        }
    }

    // --------------------------------------

    private void varsayilanOdalariEkle() {
        for (int i = 1; i <= 100; i++) {
            String odaNo = String.valueOf(i);
            int kapasite = (i % 5 == 0) ? 4 : (i % 2 == 0 ? 3 : 2);
            odalar.put(odaNo, new Oda(odaNo, kapasite));
        }
    }
}