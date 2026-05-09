package application;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import service.OtelYonetimi;

public class RezervasyonApplication extends Application {

    private OtelYonetimi sistem;

    @Override
    public void start(Stage primaryStage) {
        sistem = new OtelYonetimi();

        primaryStage.setTitle("E-Otel Yönetim Sistemi");

        TabPane tabPane = new TabPane();

        // 1. Sekme
        Tab tabRezervasyon = new Tab("Yeni Rezervasyon");
        tabRezervasyon.setClosable(false);
        tabRezervasyon.setContent(createRezervasyonFormu());

        // 2. Sekme
        Tab tabCikis = new Tab("Çıkış (Check-Out)");
        tabCikis.setClosable(false);
        tabCikis.setContent(createCikisFormu());

        // 3. Sekme
        Tab tabRaporlar = new Tab("Listeler ve Arşiv");
        tabRaporlar.setClosable(false);
        tabRaporlar.setContent(createRaporEkrani());

        tabPane.getTabs().addAll(tabRezervasyon, tabCikis, tabRaporlar);

        Scene scene = new Scene(tabPane, 500, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createRezervasyonFormu() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(10);
        grid.setHgap(10);

        TextField txtTc = new TextField();
        TextField txtAd = new TextField();
        TextField txtOdaNo = new TextField();
        TextField txtBasTarih = new TextField();
        txtBasTarih.setPromptText("YYYY-MM-DD");
        TextField txtBitTarih = new TextField();
        txtBitTarih.setPromptText("YYYY-MM-DD");

        grid.add(new Label("TC Kimlik No:"), 0, 0); grid.add(txtTc, 1, 0);
        grid.add(new Label("Ad Soyad:"), 0, 1); grid.add(txtAd, 1, 1);
        grid.add(new Label("Oda No:"), 0, 2); grid.add(txtOdaNo, 1, 2);
        grid.add(new Label("Giriş Tarihi:"), 0, 3); grid.add(txtBasTarih, 1, 3);
        grid.add(new Label("Çıkış Tarihi:"), 0, 4); grid.add(txtBitTarih, 1, 4);

        Button btnKaydet = new Button("Rezervasyon Yap");
        Label lblSonuc = new Label();
        lblSonuc.setStyle("-fx-text-fill: blue;");

        btnKaydet.setOnAction(e -> {
            String sonuc = sistem.musteriKayitVeRezervasyon(
                    txtTc.getText(), txtAd.getText(), txtOdaNo.getText(),
                    txtBasTarih.getText(), txtBitTarih.getText()
            );
            lblSonuc.setText(sonuc);
            txtTc.clear(); txtAd.clear(); txtOdaNo.clear(); txtBasTarih.clear(); txtBitTarih.clear();
        });

        VBox vbox = new VBox(20, grid, btnKaydet, lblSonuc);
        vbox.setPadding(new Insets(10));
        return vbox;
    }

    private VBox createCikisFormu() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(10);
        grid.setHgap(10);

        TextField txtOdaNo = new TextField();
        TextField txtTc = new TextField();

        grid.add(new Label("Çıkış Yapılacak Oda No:"), 0, 0); grid.add(txtOdaNo, 1, 0);
        grid.add(new Label("Müşteri TC No:"), 0, 1); grid.add(txtTc, 1, 1);

        Button btnCikis = new Button("Çıkış Yap");
        Label lblSonuc = new Label();
        lblSonuc.setStyle("-fx-text-fill: blue;");

        btnCikis.setOnAction(e -> {
            String sonuc = sistem.cikisYap(txtOdaNo.getText(), txtTc.getText());
            lblSonuc.setText(sonuc);
            txtOdaNo.clear(); txtTc.clear();
        });

        VBox vbox = new VBox(20, grid, btnCikis, lblSonuc);
        vbox.setPadding(new Insets(10));
        return vbox;
    }

    private VBox createRaporEkrani() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));

        Button btnBeklemeListesi = new Button("Bekleme Listesini Getir");
        Button btnGecmis = new Button("Geçmiş Arşivi (BST) Getir");
        
        TextArea txtSonuc = new TextArea();
        txtSonuc.setEditable(false);
        txtSonuc.setPrefHeight(250);

        btnBeklemeListesi.setOnAction(e -> {
            txtSonuc.setText(sistem.beklemeListesiniGoster());
        });

        btnGecmis.setOnAction(e -> {
            txtSonuc.setText(sistem.gecmisRezervasyonlariGoster());
        });

        vbox.getChildren().addAll(btnBeklemeListesi, btnGecmis, txtSonuc);
        return vbox;
    }

    public static void main(String[] args) {
        launch(args);
    }
}