package service;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import model.BeklemeListesi;
import model.Musteri;
import model.Oda;
import model.Rezervasyon;

public class OtelYonetimi {
    // Verileri hafızada tutacak yapılar
    private Map<String, Musteri> musteriler = new HashMap<>();
    private Map<String, Oda> odalar = new HashMap<>();
    private BeklemeListesi beklemeListesi = new BeklemeListesi();
    private TreeMap<LocalDate, Rezervasyon> tamamlananRezervasyonlar = new TreeMap<>();

    // DÜZELTME: Dosya adı artık sabit değil, her şube için dışarıdan gelecek
    private final String DOSYA_ADI;
    private final Gson gson;

    // DÜZELTME: Constructor artık dosya adını parametre olarak alıyor
    public OtelYonetimi(String dosyaAdi) {
        this.DOSYA_ADI = dosyaAdi;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>) (src, typeOfSrc, context) -> new JsonPrimitive(src.toString()))
                .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, typeOfT, context) -> LocalDate.parse(json.getAsString()))
                .setPrettyPrinting()
                .create();
        upload();
    }

    private void save() {
        try (Writer writer = Files.newBufferedWriter(Paths.get(DOSYA_ADI))) {
            Map<String, Object> tumVeri = new HashMap<>();
            tumVeri.put("musteriler", musteriler);
            tumVeri.put("odalar", odalar);
            tumVeri.put("beklemeListesi", beklemeListesi);
            tumVeri.put("arsiv", tamamlananRezervasyonlar);
            gson.toJson(tumVeri, writer);
        } catch (IOException e) {
            System.out.println("Kayıt Hatası: " + e.getMessage());
        }
    }

    private void upload() {
        File dosya = new File(DOSYA_ADI);
        if (!dosya.exists()) {
            odaEkle();
            save();
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

        } catch (Exception e) {
            odaEkle();
        }
    }

    public String musteriKayitVeRezervasyon(String tc, String ad, String odaNo, String basTarih, String bitTarih) {
        try {
            LocalDate baslangic = LocalDate.parse(basTarih);
            LocalDate bitis = LocalDate.parse(bitTarih);

            musteriler.putIfAbsent(tc, new Musteri(tc, ad));
            Musteri musteri = musteriler.get(tc);

            Oda talepEdilenOda = odalar.get(odaNo);
            if (talepEdilenOda == null) {
                return "Hata: " + odaNo + " numaralı bir oda sistemde bulunmuyor!";
            }

            if (talepEdilenOda.musaitMi(baslangic, bitis)) {
                Rezervasyon yeniRezervasyon = new Rezervasyon(musteri, talepEdilenOda.odaNo, baslangic, bitis);

                // Eğer önceki adımlarda Aralık Ağacı (Interval Tree) için "rezervasyonEkle"
                // metodunu yazdıysan burayı talepEdilenOda.rezervasyonEkle(yeniRezervasyon); yapabilirsin.
                talepEdilenOda.aktifRezervasyonlar.add(yeniRezervasyon);

                save();
                return "Başarılı: " + odaNo + " numaralı odaya rezervasyon yapıldı.";
            } else {
                beklemeListesi.kuyrugaEkle(musteri);
                save();
                return "Uyarı: Oda dolu! Müşteri bekleme listesine eklendi.";
            }
        } catch (DateTimeParseException e) {
            return "Hata: Lütfen tarihleri YYYY-MM-DD formatında giriniz.";
        } catch (Exception e) {
            return "Hata: Beklenmeyen bir durum oluştu.";
        }
    }

    public String cikisYap(String odaNo, String tc) {
        Oda oda = odalar.get(odaNo);
        if (oda == null) return "Hata: '" + odaNo + "' numaralı oda bulunamadı!";

        Rezervasyon iptalEdilecek = null;
        for (Rezervasyon rez : oda.aktifRezervasyonlar) {
            if (rez.musteri.tcNo.equals(tc)) {
                iptalEdilecek = rez;
                break;
            }
        }

        if (iptalEdilecek != null) {
            oda.aktifRezervasyonlar.remove(iptalEdilecek); // Ağaç kullanıyorsan oda.rezervasyonSil(iptalEdilecek); yap
            tamamlananRezervasyonlar.put(iptalEdilecek.bitisTarihi, iptalEdilecek);
            save();
            return "Çıkış başarılı. Kayıt arşive (BST) aktarıldı.";
        } else {
            return "Hata: Bu odada bu TC ile kayıtlı aktif bir rezervasyon yok.";
        }
    }

    public String beklemeListesiniGoster() {
        if (beklemeListesi != null) {
            return beklemeListesi.listeyiYazdir();
        }
        return "Liste boş.";
    }

    public String gecmisRezervasyonlariGoster() {
        if (tamamlananRezervasyonlar == null || tamamlananRezervasyonlar.isEmpty()) {
            return "Arşivde hiç kayıt yok.";
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<LocalDate, Rezervasyon> entry : tamamlananRezervasyonlar.entrySet()) {
            sb.append("Çıkış Tarihi: ").append(entry.getKey()).append(" | Detay: ").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }

    private void odaEkle() {
        for (int i = 1; i <= 100; i++) {
            String odaNo = String.valueOf(i);
            int kapasite = (i % 5 == 0) ? 4 : (i % 2 == 0 ? 3 : 2);
            odalar.put(odaNo, new Oda(odaNo, kapasite));
        }
    }

    // --- MERKEZİ SİSTEM İÇİN EKLENEN YENİ METOTLAR ---

    public int getAktifMusteriSayisi() {
        return musteriler.size(); // Sisteme kayıtlı toplam müşteri sayısını verir
    }

    public int getTamamlananRezervasyonSayisi() {
        return tamamlananRezervasyonlar.size(); // Arşiv boyutunu verir
    }
}