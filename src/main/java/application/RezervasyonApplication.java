package application;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import service.MerkeziSistem;

/**
 * E-Otel Yönetim Sistemi Ana Arayüz (GUI) Sınıfı
 * * --- HATA YÖNETİMİ (ERROR HANDLING) ---
 * 1. Geçersiz Giriş: Kullanıcının girdiği TC Kimlik ve Ad-Soyad bilgileri boşluk testine tabi tutulmuştur.
 * 2. Format Kontrolü: TC Kimlik alanına harf girilmesi durumu try-catch (NumberFormatException) ile yakalanmış
 * ve 11 hane kuralı eklenmiştir.
 * 3. Eksik Veri: Oda veya tarih seçilmeden işlem yapılması engellenmiştir.
 * * --- ÇOKLU ŞUBE MİMARİSİ (MULTI-BRANCH ARCHITECTURE) ---
 * - MerkeziSistem üzerinden O(1) karmaşıklıkla istenilen şubenin verilerine erişilir.
 */
public class RezervasyonApplication extends Application {

    private MerkeziSistem merkez;
    private ComboBox<String> cmbAktifSube;

    @Override
    public void start(Stage primaryStage) {
        merkez = new MerkeziSistem();

        primaryStage.setTitle("E-Otel Yönetim Sistemi - Merkezi Sistem");

        // --- EN ÜST KISIM: ŞUBE SEÇİCİ ---
        cmbAktifSube = new ComboBox<>();
        cmbAktifSube.getItems().addAll("Çorlu", "Bayburt", "Los Angeles", "Las Vegas");
        cmbAktifSube.setValue("Çorlu"); // Varsayılan Şube
        cmbAktifSube.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label lblSube = new Label("Aktif Şube Seçimi: ");
        lblSube.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        HBox topBox = new HBox(10, lblSube, cmbAktifSube);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(15));
        topBox.setStyle("-fx-background-color: #ecf0f1; -fx-border-color: #bdc3c7; -fx-border-width: 0 0 1 0;");

        // --- SEKMELER (TABS) ---
        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: #f8f9fa; -fx-font-family: 'Segoe UI', sans-serif;");

        Tab tabRezervasyon = new Tab("Yeni Rezervasyon");
        tabRezervasyon.setClosable(false);
        tabRezervasyon.setContent(createRezervasyonFormu());

        Tab tabCikis = new Tab("Çıkış (Check-Out)");
        tabCikis.setClosable(false);
        tabCikis.setContent(createCikisFormu());

        Tab tabRaporlar = new Tab("Listeler ve Arşiv");
        tabRaporlar.setClosable(false);
        tabRaporlar.setContent(createRaporEkrani());

        tabPane.getTabs().addAll(tabRezervasyon, tabCikis, tabRaporlar);

        // Ana düzen (Kök): Üstte şube seçici, altta sekmeler
        VBox root = new VBox(topBox, tabPane);
        VBox.setVgrow(tabPane, Priority.ALWAYS); // TabPane alanı doldursun

        Scene scene = new Scene(root, 400, 500); // Pencere boyutunu biraz büyüttük
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createRezervasyonFormu() {

        Label lblBaslik = new Label("Yeni Rezervasyon & Oda Arama");
        lblBaslik.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        lblBaslik.setMaxWidth(Double.MAX_VALUE);
        lblBaslik.setAlignment(Pos.CENTER);

        // --- 1. AŞAMA: ARAMA KRİTERLERİ ---
        GridPane searchGrid = new GridPane();
        searchGrid.setVgap(10); searchGrid.setHgap(15);

        ComboBox<Integer> cmbKisiSayisi = new ComboBox<>();
        cmbKisiSayisi.getItems().addAll(1, 2, 3, 4);
        cmbKisiSayisi.setPromptText("Kişi Sayısı");

        DatePicker dpBas = new DatePicker(); dpBas.setPromptText("Giriş Tarihi");
        DatePicker dpBit = new DatePicker(); dpBit.setPromptText("Çıkış Tarihi");

        Button btnOdaBul = new Button("Uygun Odaları Getir");
        btnOdaBul.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");

        searchGrid.add(new Label("Kişi Sayısı:"), 0, 0); searchGrid.add(cmbKisiSayisi, 1, 0);
        searchGrid.add(new Label("Giriş Tarihi:"), 0, 1); searchGrid.add(dpBas, 1, 1);
        searchGrid.add(new Label("Çıkış Tarihi:"), 0, 2); searchGrid.add(dpBit, 1, 2);
        searchGrid.add(btnOdaBul, 1, 3);

        // --- 2. AŞAMA: SONUÇLAR VE DİNAMİK FORM ---
        ComboBox<String> cmbOdaNo = new ComboBox<>();
        cmbOdaNo.setPromptText("Müsait Odaları Göster");
        cmbOdaNo.setDisable(true);
        cmbOdaNo.setPrefWidth(210);

        VBox dinamikMusteriKutusu = new VBox(10);
        dinamikMusteriKutusu.setStyle("-fx-padding: 10; -fx-border-color: #bdc3c7; -fx-border-radius: 5;");

        Label lblSonuc = new Label();
        lblSonuc.setStyle("-fx-font-weight: bold;");
        lblSonuc.setWrapText(true); // Uzun hata veya başarı mesajları ekrandan taşmasın, alt satıra geçsin

        // ODA BUL BUTONUNA TIKLANINCA
        btnOdaBul.setOnAction(e -> {
            if (cmbKisiSayisi.getValue() == null || dpBas.getValue() == null || dpBit.getValue() == null) {
                lblSonuc.setText("Lütfen arama için kişi sayısı ve tarihleri eksiksiz girin.");
                lblSonuc.setStyle("-fx-text-fill: #e74c3c;");
                return;
            }
            if (!dpBit.getValue().isAfter(dpBas.getValue())) {
                lblSonuc.setText("Çıkış tarihi girişten sonra olmalıdır!");
                lblSonuc.setStyle("-fx-text-fill: #e74c3c;");
                return;
            }

            String aktifSube = cmbAktifSube.getValue();
            java.util.List<String> uygunOdalar = merkez.subeGetir(aktifSube).uygunOdalariGetir(
                    cmbKisiSayisi.getValue(), dpBas.getValue(), dpBit.getValue());

            cmbOdaNo.getItems().clear();
            if (uygunOdalar.isEmpty()) {
                lblSonuc.setText("Bu tarihlerde " + cmbKisiSayisi.getValue() + " kişilik uygun oda bulunamadı.");
                lblSonuc.setStyle("-fx-text-fill: #e74c3c;");
                cmbOdaNo.setDisable(true);
                dinamikMusteriKutusu.getChildren().clear();
            } else {
                cmbOdaNo.getItems().addAll(uygunOdalar);
                cmbOdaNo.setDisable(false);
                lblSonuc.setText(uygunOdalar.size() + " adet uygun oda bulundu.");
                lblSonuc.setStyle("-fx-text-fill: #27ae60;");

                dinamikMusteriKutusu.getChildren().clear();
                for (int i = 1; i <= cmbKisiSayisi.getValue(); i++) {
                    HBox kisiSatiri = new HBox(10);
                    kisiSatiri.setAlignment(Pos.CENTER_LEFT);
                    TextField txtTc = new TextField(); txtTc.setPromptText(i + ". Kişi TC No");
                    TextField txtAd = new TextField(); txtAd.setPromptText(i + ". Kişi Ad Soyad");
                    kisiSatiri.getChildren().addAll(new Label(i + ". Misafir: "), txtTc, txtAd);
                    dinamikMusteriKutusu.getChildren().add(kisiSatiri);
                }
            }
        });

        // --- 3. AŞAMA: AKSİYON BUTONLARI (YAN YANA EN ALTTA) ---
        Button btnKaydet = new Button("Tüm Kişileri Kaydet");
        btnKaydet.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 15;");

        Button btnTemizle = new Button("Çıkış / Temizle");
        btnTemizle.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 15;");

        // İki butonun arasını tamamen açacak esnek boşluk elemanı
        javafx.scene.layout.Region yay = new javafx.scene.layout.Region();
        HBox.setHgrow(yay, Priority.ALWAYS);

        // Yay elemanını iki butonun ortasına koyuyoruz
        HBox butonKutusu = new HBox(btnKaydet, yay, btnTemizle);
        butonKutusu.setAlignment(Pos.CENTER_LEFT);

        // TEMİZLE BUTONUNUN İŞLEVİ
        btnTemizle.setOnAction(e -> {
            cmbKisiSayisi.getSelectionModel().clearSelection();
            dpBas.setValue(null);
            dpBit.setValue(null);
            cmbOdaNo.getItems().clear();
            cmbOdaNo.setDisable(true);
            dinamikMusteriKutusu.getChildren().clear();
            lblSonuc.setText("");
        });

        // KAYDET BUTONUNUN İŞLEVİ
        btnKaydet.setOnAction(e -> {
            if (cmbOdaNo.getValue() == null) {
                lblSonuc.setText("Lütfen filtrelenen listeden bir oda seçiniz.");
                lblSonuc.setStyle("-fx-text-fill: #e74c3c;");
                return;
            }

            String secilenOda = cmbOdaNo.getValue();
            String aktifSube = cmbAktifSube.getValue();
            StringBuilder islemSonucu = new StringBuilder();

            for (javafx.scene.Node node : dinamikMusteriKutusu.getChildren()) {
                if (node instanceof HBox) {
                    HBox satir = (HBox) node;
                    TextField txtTc = (TextField) satir.getChildren().get(1);
                    TextField txtAd = (TextField) satir.getChildren().get(2);

                    String tc = txtTc.getText().trim();
                    String ad = txtAd.getText().trim();

                    if(tc.matches("\\d{11}") && !ad.isEmpty()) {
                        String sonuc = merkez.subeGetir(aktifSube).musteriKayitVeRezervasyon(
                                tc, ad, secilenOda, dpBas.getValue().toString(), dpBit.getValue().toString());
                        islemSonucu.append(sonuc).append("\n");
                    } else {
                        islemSonucu.append("Hata: Geçersiz TC (Sadece 11 hane RAKAM olmalı) veya boş isim!\n");
                    }
                }
            }
            lblSonuc.setStyle("-fx-text-fill: #2c3e50;");
            lblSonuc.setText(islemSonucu.toString());
            dinamikMusteriKutusu.getChildren().clear();
            cmbOdaNo.getItems().clear(); cmbOdaNo.setDisable(true);
        });

        // Tüm arayüz elemanlarını düzgün bir dikey sırada topluyoruz
        VBox vbox = new VBox(15, lblBaslik, searchGrid, cmbOdaNo, dinamikMusteriKutusu, butonKutusu, lblSonuc);
        vbox.setPadding(new Insets(20));
        return vbox;
    }

    private VBox createCikisFormu() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 0, 20, 0));
        grid.setVgap(15);
        grid.setHgap(15);

        ComboBox<String> cmbOdaNo = new ComboBox<>();
        cmbOdaNo.setPromptText("Oda Seçiniz");
        cmbOdaNo.setStyle("-fx-background-radius: 5;");
        cmbOdaNo.setPrefWidth(210);
        for (int i = 1; i <= 16; i++) {

            cmbOdaNo.getItems().add(String.valueOf(i));
        }

        TextField txtTc = new TextField();
        txtTc.setStyle("-fx-background-radius: 5; -fx-padding: 6;");
        txtTc.setPrefWidth(210);

        grid.add(new Label("Çıkış Yapılacak Oda No:"), 0, 0); grid.add(cmbOdaNo, 1, 0);
        grid.add(new Label("Müşteri TC No:"), 0, 1); grid.add(txtTc, 1, 1);

        Button btnCikis = new Button("Çıkış Yap");
        btnCikis.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 15;");

        HBox butonKutusu = new HBox(btnCikis);
        butonKutusu.setAlignment(Pos.CENTER_RIGHT);

        Label lblSonuc = new Label();
        lblSonuc.setStyle("-fx-text-fill: #2980b9; -fx-font-weight: bold;");

        btnCikis.setOnAction(e -> {
            String secilenOda = cmbOdaNo.getValue();
            String tcNo = txtTc.getText().trim();

            if (tcNo.isEmpty() || secilenOda == null) {
                lblSonuc.setStyle("-fx-text-fill: #e74c3c;");
                lblSonuc.setText("Hata: TC ve Oda alanları boş olamaz.");
                return;
            }

            // DÜZELTME: Çıkış işlemi de aktif şubeye bildiriliyor
            String aktifSube = cmbAktifSube.getValue();
            String sonuc = merkez.subeGetir(aktifSube).cikisYap(secilenOda, tcNo);

            lblSonuc.setStyle("-fx-text-fill: #2980b9;");
            lblSonuc.setText(sonuc);

            cmbOdaNo.getSelectionModel().clearSelection();
            txtTc.clear();
        });

        VBox vbox = new VBox(20, grid, butonKutusu, lblSonuc);
        vbox.setPadding(new Insets(20));
        return vbox;
    }

    private VBox createRaporEkrani() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));

        // --- BUTON TANIMLAMALARI ---
        Button btnBeklemeListesi = new Button("Bekleme Listesi");
        btnBeklemeListesi.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        btnBeklemeListesi.setMaxWidth(Double.MAX_VALUE);

        Button btnGecmis = new Button("Arşiv (Geçmiş)");
        btnGecmis.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        btnGecmis.setMaxWidth(Double.MAX_VALUE);

        // Aktif konaklayanları gösteren buton
        Button btnAktifKalanlar = new Button("İçeridekiler (Aktif)");
        btnAktifKalanlar.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        btnAktifKalanlar.setMaxWidth(Double.MAX_VALUE);

        // Merkez rapor butonu
        Button btnMerkezRapor = new Button("Merkezi Zincir Raporu");
        btnMerkezRapor.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        btnMerkezRapor.setMaxWidth(Double.MAX_VALUE);

        // TC İLE SORGULAMA ALANI ---
        HBox tcSorguKutusu = new HBox(10);
        tcSorguKutusu.setAlignment(Pos.CENTER_LEFT);
        tcSorguKutusu.setStyle("-fx-padding: 10; -fx-border-color: #bdc3c7; -fx-border-radius: 5; -fx-background-color: #ecf0f1;");

        TextField txtTCSorgu = new TextField();
        txtTCSorgu.setPromptText("Sorgulanacak TC No");
        txtTCSorgu.setStyle("-fx-background-radius: 5;");

        Button btnTCSorgula = new Button("Müşteri Ara");
        btnTCSorgula.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");

        tcSorguKutusu.getChildren().addAll(new Label("TC ile Arama: "), txtTCSorgu, btnTCSorgula);
        HBox.setHgrow(txtTCSorgu, Priority.ALWAYS); // TextField alanı doldursun

        // --- BUTONLARI EKRANA DİZME (YATAY KUTULAR) ---
        HBox ustButonlar = new HBox(10, btnBeklemeListesi, btnGecmis, btnAktifKalanlar);
        HBox.setHgrow(btnBeklemeListesi, Priority.ALWAYS);
        HBox.setHgrow(btnGecmis, Priority.ALWAYS);
        HBox.setHgrow(btnAktifKalanlar, Priority.ALWAYS);
        ustButonlar.setAlignment(Pos.CENTER);

        // --- SONUÇ GÖSTERİM ALANI ---
        TextArea txtSonuc = new TextArea();
        txtSonuc.setEditable(false);
        txtSonuc.setStyle("-fx-background-radius: 5; -fx-border-radius: 5; -fx-font-family: 'Monospaced'; -fx-font-size: 13px;");
        txtSonuc.setPrefHeight(250);
        VBox.setVgrow(txtSonuc, Priority.ALWAYS);

        // --- BUTON OLAYLARI (EVENTS) ---

        btnBeklemeListesi.setOnAction(e -> {
            String aktifSube = cmbAktifSube.getValue();
            String hamVeri = merkez.subeGetir(aktifSube).beklemeListesiniGoster();
            txtSonuc.setText("--- " + aktifSube + " Şubesi Bekleme Listesi ---\n\n" + tabloGorunumuYap(hamVeri));
        });

        btnGecmis.setOnAction(e -> {
            String aktifSube = cmbAktifSube.getValue();
            String hamVeri = merkez.subeGetir(aktifSube).gecmisRezervasyonlariGoster();
            txtSonuc.setText("--- " + aktifSube + " Şubesi Arşivlenmiş Müşteri Kayıtları ---\n\n" + tabloGorunumuYap(hamVeri));
        });

        // Aktif Konaklayanları Listele
        btnAktifKalanlar.setOnAction(e -> {
            String aktifSube = cmbAktifSube.getValue();
            String veri = merkez.subeGetir(aktifSube).aktifKonaklayanlariGoster();
            txtSonuc.setText("--- " + aktifSube + " Şubesi Aktif Konaklayanlar (In-House) ---\n\n" + veri);
        });

        btnMerkezRapor.setOnAction(e -> {
            String rapor = merkez.merkeziRaporOlustur();
            txtSonuc.setText(rapor);
        });

        // TC ile Müşteri Sorgula
        btnTCSorgula.setOnAction(e -> {
            String tc = txtTCSorgu.getText().trim();
            if(tc.isEmpty()) {
                txtSonuc.setText("Lütfen sorgulamak için bir TC Kimlik Numarası girin.");
                return;
            }

            String aktifSube = cmbAktifSube.getValue();
            String veri = merkez.subeGetir(aktifSube).tcIleMusteriSorgula(tc);
            txtSonuc.setText("--- " + aktifSube + " Şubesi Müşteri Sorgu Sonucu ---\n\n" + veri);
        });

        // Hepsini Ana Dikey Kutuya (VBox) Ekle
        vbox.getChildren().addAll(ustButonlar, tcSorguKutusu, btnMerkezRapor, txtSonuc);
        return vbox;
    }

    private String tabloGorunumuYap(String veri) {
        if (veri == null || veri.trim().isEmpty()) {
            return "Görüntülenecek kayıt bulunamadı.";
        }

        StringBuilder tablo = new StringBuilder();
        String[] satirlar = veri.split("\n");

        for (String satir : satirlar) {
            if (satir.trim().isEmpty()) continue;

            String[] parcalar = satir.split("\\|");
            StringBuilder formatliSatir = new StringBuilder();

            for (int i = 0; i < parcalar.length; i++) {
                String parca = parcalar[i].trim().replace("Detay:", "").trim();
                formatliSatir.append(parca);

                if (i < parcalar.length - 1) {
                    formatliSatir.append("  │  ");
                }
            }

            tablo.append(" ").append(formatliSatir.toString()).append("\n");
        }

        return tablo.toString();
    }

    public static void main(String[] args) {
        launch(args);
    }
}