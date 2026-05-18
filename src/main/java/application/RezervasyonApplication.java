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
import service.MerkeziSistem; // YENİ EKLENDİ

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

    // YENİ: Artık tek bir otel değil, merkezi sistemi çağırıyoruz
    private MerkeziSistem merkez;
    // YENİ: Hangi şubede işlem yapıldığını global olarak tutacak ComboBox
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
        Label lblBaslik = new Label("Rezervasyon İşlemleri");
        lblBaslik.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        lblBaslik.setMaxWidth(Double.MAX_VALUE);
        lblBaslik.setAlignment(Pos.CENTER);

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 0, 20, 0));
        grid.setVgap(15);
        grid.setHgap(15);

        TextField txtTc = new TextField();
        txtTc.setStyle("-fx-background-radius: 5; -fx-padding: 6;");
        txtTc.setPrefWidth(210);

        TextField txtAd = new TextField();
        txtAd.setStyle("-fx-background-radius: 5; -fx-padding: 6;");
        txtAd.setPrefWidth(210);

        ComboBox<String> cmbOdaNo = new ComboBox<>();
        cmbOdaNo.setPromptText("Oda Seçiniz");
        cmbOdaNo.setStyle("-fx-background-radius: 5;");
        cmbOdaNo.setPrefWidth(210);
        for (int i = 1; i <= 100; i++) {
            cmbOdaNo.getItems().add(String.valueOf(i));
        }

        DatePicker dpBasTarih = new DatePicker();
        dpBasTarih.setPromptText("Takvimden Seçin");
        dpBasTarih.setStyle("-fx-background-radius: 5;");
        dpBasTarih.setPrefWidth(210);

        DatePicker dpBitTarih = new DatePicker();
        dpBitTarih.setPromptText("Takvimden Seçin");
        dpBitTarih.setStyle("-fx-background-radius: 5;");
        dpBitTarih.setPrefWidth(210);

        grid.add(new Label("TC Kimlik No:"), 0, 0); grid.add(txtTc, 1, 0);
        grid.add(new Label("Ad Soyad:"), 0, 1); grid.add(txtAd, 1, 1);
        grid.add(new Label("Oda No:"), 0, 2); grid.add(cmbOdaNo, 1, 2);
        grid.add(new Label("Giriş Tarihi:"), 0, 3); grid.add(dpBasTarih, 1, 3);
        grid.add(new Label("Çıkış Tarihi:"), 0, 4); grid.add(dpBitTarih, 1, 4);

        Button btnKaydet = new Button("Rezervasyon Yap");
        btnKaydet.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 15;");

        Button btnIptal = new Button("İptal");
        btnIptal.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 15;");

        HBox butonKutusu = new HBox(10, btnKaydet, btnIptal);
        butonKutusu.setAlignment(Pos.CENTER_RIGHT);

        Label lblSonuc = new Label();
        lblSonuc.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");

        btnKaydet.setOnAction(e -> {
            String tcNo = txtTc.getText().trim();
            String adSoyad = txtAd.getText().trim();
            String secilenOda = cmbOdaNo.getValue();

            if (tcNo.isEmpty() || adSoyad.isEmpty()) {
                lblSonuc.setStyle("-fx-text-fill: #e74c3c;");
                lblSonuc.setText("Hata: TC Kimlik veya Ad Soyad boş bırakılamaz!");
                return;
            }

            try {
                Long.parseLong(tcNo);
                if(tcNo.length() != 11) {
                    lblSonuc.setStyle("-fx-text-fill: #e74c3c;");
                    lblSonuc.setText("Hata: TC Kimlik numarası 11 haneli olmalıdır!");
                    return;
                }
            } catch (NumberFormatException ex) {
                lblSonuc.setStyle("-fx-text-fill: #e74c3c;");
                lblSonuc.setText("Hata: TC Kimlik sadece rakamlardan oluşmalıdır!");
                return;
            }

            if (secilenOda == null || dpBasTarih.getValue() == null || dpBitTarih.getValue() == null) {
                lblSonuc.setStyle("-fx-text-fill: #e74c3c;");
                lblSonuc.setText("Hata: Lütfen oda ve tarih seçimlerini tamamlayınız.");
                return;
            }

            // DÜZELTME: İşlem aktif şube üzerinden yapılıyor!
            String aktifSube = cmbAktifSube.getValue();
            String sonuc = merkez.subeGetir(aktifSube).musteriKayitVeRezervasyon(
                    tcNo, adSoyad, secilenOda,
                    dpBasTarih.getValue().toString(), dpBitTarih.getValue().toString()
            );

            lblSonuc.setStyle("-fx-text-fill: #27ae60;");
            lblSonuc.setText(sonuc);

            txtTc.clear();
            txtAd.clear();
            cmbOdaNo.getSelectionModel().clearSelection();
            dpBasTarih.setValue(null);
            dpBitTarih.setValue(null);
        });

        btnIptal.setOnAction(e -> {
            txtTc.clear(); txtAd.clear(); cmbOdaNo.getSelectionModel().clearSelection();
            dpBasTarih.setValue(null); dpBitTarih.setValue(null); lblSonuc.setText("");
        });

        VBox vbox = new VBox(20, lblBaslik, grid, butonKutusu, lblSonuc);
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
        for (int i = 1; i <= 25; i++) {
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

        Button btnBeklemeListesi = new Button("Bekleme Listesi");
        btnBeklemeListesi.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 15;");
        btnBeklemeListesi.setMaxWidth(Double.MAX_VALUE);

        Button btnGecmis = new Button("Geçmişi Görüntüle");
        btnGecmis.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 15;");
        btnGecmis.setMaxWidth(Double.MAX_VALUE);

        // YENİ BUTON: Merkezi Rapor
        Button btnMerkezRapor = new Button("Merkezi Zincir Raporu");
        btnMerkezRapor.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 15;");
        btnMerkezRapor.setMaxWidth(Double.MAX_VALUE);

        HBox butonKutusu = new HBox(10, btnBeklemeListesi, btnGecmis);
        HBox.setHgrow(btnBeklemeListesi, Priority.ALWAYS);
        HBox.setHgrow(btnGecmis, Priority.ALWAYS);
        butonKutusu.setAlignment(Pos.CENTER);

        TextArea txtSonuc = new TextArea();
        txtSonuc.setEditable(false);
        txtSonuc.setStyle("-fx-background-radius: 5; -fx-border-radius: 5; -fx-font-family: 'Monospaced'; -fx-font-size: 13px;");
        txtSonuc.setPrefHeight(250);
        VBox.setVgrow(txtSonuc, Priority.ALWAYS);

        // Bekleme listesi ve Geçmiş, üstten seçili olan (aktif) şubeden çekilir
        btnBeklemeListesi.setOnAction(e -> {
            String aktifSube = cmbAktifSube.getValue();
            String hamVeri = merkez.subeGetir(aktifSube).beklemeListesiniGoster();
            txtSonuc.setText("--- " + aktifSube + " Şubesi Bekleme Listesi ---\n\n" + tabloGorunumuYap(hamVeri));
        });

        btnGecmis.setOnAction(e -> {
            String aktifSube = cmbAktifSube.getValue();
            String hamVeri = merkez.subeGetir(aktifSube).gecmisRezervasyonlariGoster();
            txtSonuc.setText("--- " + aktifSube + " Şubesi Arşivi ---\n\n" + tabloGorunumuYap(hamVeri));
        });

        // Merkez Raporu tüm sistemi tarar, tabloGorunumuYap() metoduna girmez (kendi tasarımı var)
        btnMerkezRapor.setOnAction(e -> {
            String rapor = merkez.merkeziRaporOlustur();
            txtSonuc.setText(rapor);
        });

        vbox.getChildren().addAll(butonKutusu, btnMerkezRapor, txtSonuc);
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