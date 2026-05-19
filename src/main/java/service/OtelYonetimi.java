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
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;

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
import util.KronolojikRezervasyonAgaci;

public class OtelYonetimi {

    private Map<String, Musteri> musteriler = new LinkedHashMap<>();
    private Map<String, Oda> odalar = new LinkedHashMap<>();
    private BeklemeListesi beklemeListesi = new BeklemeListesi();
    private transient KronolojikRezervasyonAgaci tamamlananRezervasyonlar = new KronolojikRezervasyonAgaci();

    private final String DOSYA_ADI;
    private final Gson gson;

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
            Map<String, Object> tumVeri = new LinkedHashMap<>();
            tumVeri.put("musteriler", musteriler);
            tumVeri.put("odalar", odalar);
            tumVeri.put("beklemeListesi", beklemeListesi);
            tumVeri.put("arsiv", tamamlananRezervasyonlar.toList());

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

            Type musterilerType = new TypeToken<LinkedHashMap<String, Musteri>>(){}.getType();
            musteriler = gson.fromJson(jsonObject.get("musteriler"), musterilerType);

            Type odalarType = new TypeToken<LinkedHashMap<String, Oda>>(){}.getType();
            odalar = gson.fromJson(jsonObject.get("odalar"), odalarType);

            Type arsivType = new TypeToken<ArrayList<Rezervasyon>>(){}.getType();
            List<Rezervasyon> geciciArsivListesi = gson.fromJson(jsonObject.get("arsiv"), arsivType);

            tamamlananRezervasyonlar = new KronolojikRezervasyonAgaci(); // Ağacı sıfırla
            if (geciciArsivListesi != null) {
                for (Rezervasyon rez : geciciArsivListesi) {
                    tamamlananRezervasyonlar.ekle(rez); // Ağaca ekle
                }
            }

            if (musteriler == null) musteriler = new LinkedHashMap<>();

            if (odalar == null) {
                odalar = new LinkedHashMap<>();
            } else {
                for (Oda oda : odalar.values()) {
                    oda.agaciYenidenOlustur();
                }
            }

        } catch (Exception e) {
            odaEkle();
        }
    }

    private void odaEkle() {
        String subeAdi = DOSYA_ADI.replace(".json", "");

        int fiyat1Kisi = 2000; int fiyat2Kisi = 3000; int fiyat3Kisi = 4000; int fiyat4Kisi = 5000;

        if (subeAdi.equals("Çorlu")) {
            fiyat1Kisi = 1200; fiyat2Kisi = 1800; fiyat3Kisi = 2400; fiyat4Kisi = 3200;
        } else if (subeAdi.equals("Los Angeles")) {
            fiyat1Kisi = 1800; fiyat2Kisi = 2600; fiyat3Kisi = 3500; fiyat4Kisi = 4500;
        } else if (subeAdi.equals("Las Vegas")) {
            fiyat1Kisi = 1900; fiyat2Kisi = 2800; fiyat3Kisi = 3800; fiyat4Kisi = 4800;
        }

        for (int i = 1; i <= 16; i++) {
            String odaNo = String.valueOf(i);
            int kapasite; int odaFiyati;

            if (i <= 4) { kapasite = 1; odaFiyati = fiyat1Kisi; }
            else if (i <= 8) { kapasite = 2; odaFiyati = fiyat2Kisi; }
            else if (i <= 12) { kapasite = 3; odaFiyati = fiyat3Kisi; }
            else { kapasite = 4; odaFiyati = fiyat4Kisi; }

            odalar.put(odaNo, new Oda(odaNo, kapasite, odaFiyati));
        }
    }

    public List<String> uygunOdalariGetir(int kisiSayisi, LocalDate bas, LocalDate bit) {
        List<String> uygunlar = new ArrayList<>();

        for (Oda oda : odalar.values()) {
            if (oda.getKapasite() == kisiSayisi) {
                int odadakiKisi = oda.getTarihtekiKisiSayisi(bas, bit);
                String durumMesaji;

                if (odadakiKisi == 0) {
                    durumMesaji = " | Durum: Boş";
                } else if (odadakiKisi < oda.getKapasite()) {
                    durumMesaji = " | Durum: Odada " + odadakiKisi + " kişi kalıyor (Müsait)";
                } else {
                    durumMesaji = " | Durum: TAMAMEN DOLU (Bekleme Listesi)";
                }

                uygunlar.add(oda.getOdaNo() + " Numaralı Oda - Gecelik: " + oda.getGunlukFiyat() + " TL" + durumMesaji);
            }
        }
        return uygunlar;
    }

    public String musteriKayitVeRezervasyon(String tc, String ad, String odaNo, String basTarih, String bitTarih) {
        try {
            if (odaNo != null && odaNo.contains(" ")) {
                odaNo = odaNo.split(" ")[0];
            }

            LocalDate baslangic = LocalDate.parse(basTarih);
            LocalDate bitis = LocalDate.parse(bitTarih);

            if (baslangic.isBefore(LocalDate.now())) {
                return "Hata: Geçmiş bir tarihe (bugünden öncesine) rezervasyon yapılamaz!";
            }

            musteriler.putIfAbsent(tc, new Musteri(tc, ad));
            Musteri musteri = musteriler.get(tc);

            for (Oda oda : odalar.values()) {
                for (Rezervasyon rez : oda.getAktifRezervasyonlar()) {
                    if (rez.getMusteri().getTcNo().equals(tc)) {
                        return "Hata: İşlem reddedildi! " + ad + " şu anda zaten " + oda.getOdaNo() +
                                " numaralı odada konaklıyor. Aynı TC ile yeni odaya veya bekleme listesine kayıt yapılamaz.";
                    }
                }
            }

            Oda talepEdilenOda = odalar.get(odaNo);
            if (talepEdilenOda == null) {
                return "Hata: " + odaNo + " numaralı bir oda sistemde bulunmuyor!";
            }

            if (talepEdilenOda.musaitMi(baslangic, bitis)) {
                Rezervasyon yeniRezervasyon = new Rezervasyon(musteri, talepEdilenOda.getOdaNo(), baslangic, bitis);
                talepEdilenOda.rezervasyonEkle(yeniRezervasyon);

                long gunSayisi = ChronoUnit.DAYS.between(baslangic, bitis);
                if (gunSayisi <= 0) gunSayisi = 1;
                long toplamTutar = gunSayisi * talepEdilenOda.getGunlukFiyat();

                save();
                return "Başarılı: " + ad + " adına " + odaNo + " nolu odaya kayıt yapıldı.\n" +
                        "Gecelik: " + talepEdilenOda.getGunlukFiyat() + " TL | Süre: " + gunSayisi + " Gün | Toplam Fatura: " + toplamTutar + " TL";
            } else {
                beklemeListesi.kuyrugaEkle(musteri, odaNo, baslangic, bitis);
                save();
                return "Uyarı: Oda dolu! " + ad + " bekleme listesine eklendi.";
            }
        } catch (DateTimeParseException e) {
            return "Hata: Lütfen tarihleri YYYY-MM-DD formatında giriniz.";
        } catch (Exception e) {
            return "Hata: Beklenmeyen bir durum oluştu.";
        }
    }

    public String cikisYap(String odaNo, String tc) {
        if (odaNo != null && odaNo.contains(" ")) {
            odaNo = odaNo.split(" ")[0];
        }

        Oda oda = odalar.get(odaNo);
        if (oda == null) return "Hata: '" + odaNo + "' numaralı oda bulunamadı!";

        Rezervasyon iptalEdilecek = null;
        for (Rezervasyon rez : oda.getAktifRezervasyonlar()) {
            if (rez.getMusteri().getTcNo().equals(tc)) {
                iptalEdilecek = rez;
                break;
            }
        }

        if (iptalEdilecek != null) {
            oda.rezervasyonSil(iptalEdilecek);

            tamamlananRezervasyonlar.ekle(iptalEdilecek);

            Rezervasyon siradakiUygun = beklemeListesi.siradakiUygunTalebiAl(oda);
            String ekMesaj = "";
            if (siradakiUygun != null) {
                if (oda.musaitMi(siradakiUygun.getBaslangicTarihi(), siradakiUygun.getBitisTarihi())) {
                    oda.rezervasyonEkle(siradakiUygun);
                    ekMesaj = "\n  SİSTEM NOTU: Oda boşaldığı için bekleme listesindeki '" +
                            siradakiUygun.getMusteri().getAdSoyad() + "' otomatik olarak bu odaya yerleştirildi!";
                } else {
                    beklemeListesi.kuyrugaEkle(siradakiUygun.getMusteri(), oda.getOdaNo(),
                            siradakiUygun.getBaslangicTarihi(), siradakiUygun.getBitisTarihi());
                }
            }

            save();
            return "Çıkış başarılı. Kayıt arşive aktarıldı." + ekMesaj;
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
        List<Rezervasyon> kronolojikListe = tamamlananRezervasyonlar.toList(); // YENİ: Ağaçtan sıralı listeyi çek

        if (kronolojikListe == null || kronolojikListe.isEmpty()) {
            return "Arşivde hiç kayıt yok.";
        }
        StringBuilder sb = new StringBuilder();
        for (Rezervasyon rez : kronolojikListe) { // YENİ: Sıralı liste üzerinde dön
            sb.append("Çıkış Tarihi: ").append(rez.getBitisTarihi()).append(" | Detay: ").append(rez).append("\n");
        }
        return sb.toString();
    }

    public int getTümZamanlarKayitliMusteri() {
        return musteriler.size();
    }

    public int getAktifKonaklayanSayisi() {
        int toplam = 0;
        for (Oda oda : odalar.values()) {
            toplam += oda.getAktifRezervasyonlar().size();
        }
        return toplam;
    }

    public long getToplamHasilat() {
        long toplamKasa = 0;
        Map<String, Set<LocalDate>> faturalananGunler = new HashMap<>();

        for (Rezervasyon rez : tamamlananRezervasyonlar.toList()) {
            String odaNo = rez.getOdaNo();
            Oda oda = odalar.get(odaNo);

            if (oda != null) {
                faturalananGunler.putIfAbsent(odaNo, new HashSet<>());
                Set<LocalDate> odaninGunleri = faturalananGunler.get(odaNo);

                LocalDate bas = rez.getBaslangicTarihi();
                LocalDate bit = rez.getBitisTarihi();

                for (LocalDate tarih = bas; tarih.isBefore(bit); tarih = tarih.plusDays(1)) {
                    if (!odaninGunleri.contains(tarih)) {
                        odaninGunleri.add(tarih);
                        toplamKasa += oda.getGunlukFiyat();
                    }
                }

                if (bas.isEqual(bit) && !odaninGunleri.contains(bas)) {
                    odaninGunleri.add(bas);
                    toplamKasa += oda.getGunlukFiyat();
                }
            }
        }
        return toplamKasa;
    }

    public String aktifKonaklayanlariGoster() {
        StringBuilder sb = new StringBuilder();
        boolean musteriVar = false;

        for (Oda oda : odalar.values()) {
            for (Rezervasyon rez : oda.getAktifRezervasyonlar()) {
                musteriVar = true;
                Musteri m = rez.getMusteri();
                sb.append("Oda: ").append(oda.getOdaNo())
                        .append(" | TC: ").append(m.getTcNo())
                        .append(" | İsim: ").append(m.getAdSoyad())
                        .append(" | Tarih: ").append(rez.getBaslangicTarihi()).append(" -> ").append(rez.getBitisTarihi())
                        .append("\n");
            }
        }

        if (!musteriVar) {
            return "Şu anda otelde konaklayan aktif müşteri bulunmamaktadır.";
        }

        return sb.toString();
    }

    public String tcIleMusteriSorgula(String tc) {
        Musteri m = musteriler.get(tc);

        if (m == null) {
            return "Sonuç: Sistemde bu TC (" + tc + ") ile kayıtlı hiçbir müşteri yok.";
        }

        StringBuilder sb = new StringBuilder();
        boolean aktifOdasiVar = false;

        for (Oda oda : odalar.values()) {
            for (Rezervasyon rez : oda.getAktifRezervasyonlar()) {
                if (rez.getMusteri().getTcNo().equals(tc)) {
                    aktifOdasiVar = true;
                    sb.append("Müşteri Bulundu: ").append(m.getAdSoyad())
                            .append("\nDurum: ŞU AN OTELDE KONAKLIYOR")
                            .append("\nOda No: ").append(oda.getOdaNo())
                            .append("\nÇıkış Tarihi: ").append(rez.getBitisTarihi()).append("\n");
                }
            }
        }

        if (aktifOdasiVar) {
            return sb.toString();
        } else {
            return "Müşteri Bulundu: " + m.getAdSoyad() + "\nDurum: Sistemde kaydı var ancak şu an aktif bir konaklaması yok (Eski müşteri).";
        }
    }
}