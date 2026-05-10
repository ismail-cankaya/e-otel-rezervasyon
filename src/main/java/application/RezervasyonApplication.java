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
import service.OtelYonetimi;

/**
 * E-Otel Yönetim Sistemi Ana Arayüz (GUI) Sınıfı
 * * --- HATA YÖNETİMİ (ERROR HANDLING) ---
 * 1. Geçersiz Giriş: Kullanıcının girdiği TC Kimlik ve Ad-Soyad bilgileri boşluk testine tabi tutulmuştur.
 * 2. Format Kontrolü: TC Kimlik alanına harf girilmesi durumu try-catch (NumberFormatException) ile yakalanmış
 * ve 11 hane kuralı eklenmiştir.
 * 3. Eksik Veri: Oda veya tarih seçilmeden işlem yapılması engellenmiştir.
 * * --- ZAMAN VE ALAN KARMAŞIKLIĞI (COMPLEXITY) ---
 * - Arayüz Yükleme: Zaman O(1), Alan O(1) (Sabit sayıda bileşen yüklenir)
 * - Tablo Görünümü Oluşturma: Zaman O(N) (N = Listede gösterilecek kayıt sayısı)
 */
public class RezervasyonApplication extends Application {

    private OtelYonetimi sistem;

    @Override
    public void start(Stage primaryStage) {
        sistem = new OtelYonetimi();

        primaryStage.setTitle("E-Otel Yönetim Sistemi");

        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: #f8f9fa; -fx-font-family: 'Segoe UI', sans-serif;");

        // Sekmelerin oluşturulması ve arayüze eklenmesi (Zaman: O(1))
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

        Scene scene = new Scene(tabPane, 370, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Rezervasyon formunu oluşturur.
     * Karmaşıklık: Zaman O(1), Alan O(1)
     */
    private VBox createRezervasyonFormu() {
        Label lblBaslik = new Label("Otelimize Hoş Geldiniz");
        lblBaslik.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
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

        // Kaydet Butonu Tıklanma Olayı ve Hata Yönetimi (Error Handling)
        btnKaydet.setOnAction(e -> {
            String tcNo = txtTc.getText().trim();
            String adSoyad = txtAd.getText().trim();
            String secilenOda = cmbOdaNo.getValue();

            // Hata Yönetimi 1: Boşluk Kontrolü
            if (tcNo.isEmpty() || adSoyad.isEmpty()) {
                lblSonuc.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                lblSonuc.setText("Hata: TC Kimlik veya Ad Soyad boş bırakılamaz!");
                return;
            }

            // Hata Yönetimi 2: Format ve Veri Tipi Kontrolü (Try-Catch)
            try {
                Long.parseLong(tcNo); // Sayısal değer kontrolü (Harf girilirse catch'e düşer)
                if(tcNo.length() != 11) {
                    lblSonuc.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    lblSonuc.setText("Hata: TC Kimlik numarası 11 haneli olmalıdır!");
                    return;
                }
            } catch (NumberFormatException ex) {
                lblSonuc.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                lblSonuc.setText("Hata: TC Kimlik sadece rakamlardan oluşmalıdır!");
                return;
            }

            // Hata Yönetimi 3: Eksik Seçim Kontrolleri
            if (secilenOda == null) {
                lblSonuc.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                lblSonuc.setText("Hata: Lütfen listeden bir oda seçiniz.");
                return;
            }

            if (dpBasTarih.getValue() == null || dpBitTarih.getValue() == null) {
                lblSonuc.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                lblSonuc.setText("Hata: Lütfen giriş ve çıkış tarihlerini seçiniz.");
                return;
            }

            // Validasyonlar geçildiyse arka plana veriyi gönder
            String sonuc = sistem.musteriKayitVeRezervasyon(
                    tcNo, adSoyad, secilenOda,
                    dpBasTarih.getValue().toString(), dpBitTarih.getValue().toString()
            );

            lblSonuc.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            lblSonuc.setText(sonuc);

            // Formu Temizle (Zaman: O(1))
            txtTc.clear();
            txtAd.clear();
            cmbOdaNo.getSelectionModel().clearSelection();
            dpBasTarih.setValue(null);
            dpBitTarih.setValue(null);
        });

        btnIptal.setOnAction(e -> {
            txtTc.clear();
            txtAd.clear();
            cmbOdaNo.getSelectionModel().clearSelection();
            dpBasTarih.setValue(null);
            dpBitTarih.setValue(null);
            lblSonuc.setText("");
        });

        VBox vbox = new VBox(20, lblBaslik, grid, butonKutusu, lblSonuc);
        vbox.setPadding(new Insets(20));
        return vbox;
    }

    /**
     * Çıkış (Check-Out) formunu oluşturur.
     * Karmaşıklık: Zaman O(1), Alan O(1)
     */
    private VBox createCikisFormu() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 0, 20, 0));
        grid.setVgap(15);
        grid.setHgap(15);

        ComboBox<String> cmbOdaNo = new ComboBox<>();
        cmbOdaNo.setPromptText("Oda Seçiniz");
        cmbOdaNo.setStyle("-fx-background-radius: 5;");
        cmbOdaNo.setPrefWidth(210);
        for (int i = 1; i <= 100; i++) {
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

        // Çıkış Butonu Tıklanma Olayı ve Hata Yönetimi
        btnCikis.setOnAction(e -> {
            String secilenOda = cmbOdaNo.getValue();
            String tcNo = txtTc.getText().trim();

            if (tcNo.isEmpty()) {
                lblSonuc.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                lblSonuc.setText("Hata: TC Kimlik boş bırakılamaz!");
                return;
            }

            try {
                Long.parseLong(tcNo);
                if(tcNo.length() != 11) {
                    lblSonuc.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    lblSonuc.setText("Hata: TC Kimlik numarası 11 haneli olmalıdır!");
                    return;
                }
            } catch (NumberFormatException ex) {
                lblSonuc.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                lblSonuc.setText("Hata: TC Kimlik sadece rakamlardan oluşmalıdır!");
                return;
            }

            if (secilenOda == null) {
                lblSonuc.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                lblSonuc.setText("Hata: Lütfen listeden bir oda seçiniz.");
                return;
            }

            // Arka plandaki silme işlemi (Bulunamayan eleman kontrolü sistem.cikisYap içinde yapılır)
            String sonuc = sistem.cikisYap(secilenOda, tcNo);
            lblSonuc.setStyle("-fx-text-fill: #2980b9; -fx-font-weight: bold;");
            lblSonuc.setText(sonuc);

            cmbOdaNo.getSelectionModel().clearSelection();
            txtTc.clear();
        });

        VBox vbox = new VBox(20, grid, butonKutusu, lblSonuc);
        vbox.setPadding(new Insets(20));
        return vbox;
    }

    /**
     * Rapor ekranını oluşturur.
     * Karmaşıklık: Zaman O(1), Alan O(1)
     */
    private VBox createRaporEkrani() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));

        Button btnBeklemeListesi = new Button("Bekleme Listesi");
        btnBeklemeListesi.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 15;");
        btnBeklemeListesi.setMaxWidth(Double.MAX_VALUE);

        Button btnGecmis = new Button("Geçmişi Görüntüle");
        btnGecmis.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 15;");
        btnGecmis.setMaxWidth(Double.MAX_VALUE);

        HBox butonKutusu = new HBox(10, btnBeklemeListesi, btnGecmis);
        HBox.setHgrow(btnBeklemeListesi, Priority.ALWAYS);
        HBox.setHgrow(btnGecmis, Priority.ALWAYS);
        butonKutusu.setAlignment(Pos.CENTER);

        TextArea txtSonuc = new TextArea();
        txtSonuc.setEditable(false);

        txtSonuc.setStyle("-fx-background-radius: 5; -fx-border-radius: 5; -fx-font-family: 'Monospaced'; -fx-font-size: 13px;");
        txtSonuc.setPrefHeight(250);
        VBox.setVgrow(txtSonuc, Priority.ALWAYS);

        btnBeklemeListesi.setOnAction(e -> {
            String hamVeri = sistem.beklemeListesiniGoster();
            txtSonuc.setText(tabloGorunumuYap(hamVeri));
        });

        btnGecmis.setOnAction(e -> {
            String hamVeri = sistem.gecmisRezervasyonlariGoster();
            txtSonuc.setText(tabloGorunumuYap(hamVeri));
        });

        vbox.getChildren().addAll(butonKutusu, txtSonuc);
        return vbox;
    }

    /**
     * Veritabanı/Listelerden gelen string veriyi arayüzde düzenli göstermek için formatlar.
     * Zaman Karmaşıklığı: O(N) (N = Gönderilen metindeki satır sayısı)
     * Alan Karmaşıklığı: O(N) (N = Yeni oluşturulan tablo stringinin boyutu)
     */
    private String tabloGorunumuYap(String veri) {
        // Hata Yönetimi: Boş liste veya null kontrolü
        if (veri == null || veri.trim().isEmpty()) {
            return "Görüntülenecek kayıt bulunamadı.";
        }

        StringBuilder tablo = new StringBuilder();
        String[] satirlar = veri.split("\n"); // O(N) Zaman ve Alan

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