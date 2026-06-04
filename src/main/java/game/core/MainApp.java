package game.core;

import game.model.Agent;
import game.view.GameView;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class MainApp extends Application {
    private Simulation simulation;
    private GameView gameView;

    @Override
    public void start(Stage primaryStage) {
        // 1. Inicjalizacja świata gry
        simulation = new Simulation(5, 3, 1000);
        gameView = new GameView(simulation.getGameBoard());

        // 2. Lewa strona - plansza gry
        StackPane gameRoot = new StackPane();
        Group group = new Group(gameView.getCanvas());
        gameRoot.getChildren().add(group);

        // Skalowanie planszy żeby zmieściła się w oknie
        Scale scale = new Scale(0.5, 0.5);
        scale.setPivotX(0);
        scale.setPivotY(0);
        gameView.getCanvas().getTransforms().add(scale);

        //Prawa str - Panele logi
        VBox logsPanel = new VBox();
        logsPanel.setSpacing(10);
        logsPanel.setPadding(new Insets(15));
        logsPanel.setPrefWidth(450);

        // okienka tekstowe logi


        //okienko na system i finanse
        Label lblSys = new Label("SYSTEM I FINANSE:");
        TextArea sysArea = new TextArea();
        sysArea.setEditable(false);
        sysArea.setPrefHeight(200);

        //okienko na akcje agentow
        Label lblWork = new Label("AKCJE PRACOWNIKÓW:");
        TextArea workerArea = new TextArea();
        workerArea.setEditable(false);
        workerArea.setPrefHeight(200);

        // czerwone okienko z bledami
        Label lblAlert = new Label("PROBLEMY I BŁĘDY:");
        lblAlert.setTextFill(Color.RED);
        TextArea alertArea = new TextArea();
        alertArea.setEditable(false);
        alertArea.setPrefHeight(200);
        alertArea.setStyle("-fx-text-fill: red;");

        //wszystko do prawego panelu
        logsPanel.getChildren().addAll(lblSys, sysArea, lblWork, workerArea, lblAlert, alertArea);


        // 4. Przechwytywanie wypisywania z konsoli do interfejsu (żeby nie pisać System.out w wielu miejscach)
        OutputStream out = new OutputStream() {
            private ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            @Override
            public void write(int b) {
                if (b == '\r') return; // Pomijamy znak powrotu z Windowsa

                if (b == '\n') {
                    // Mamy całą linijkę, zmieniamy na String z polskimi znakami
                    String line = new String(buffer.toByteArray(), StandardCharsets.UTF_8);
                    buffer.reset(); // Czysczenie bufor na następną linijkę

                    // JavaFX wymaga, żeby zmiany w grafice robić przez Platform.runLater
                    Platform.runLater(() -> {
                        if (line.isEmpty() || line.trim().isEmpty()) {
                            return; // Nie wypisujemy pustych linijek
                        }

                        // do segregacji tekstu do odpowiednich okienek
                        if (line.contains("TURA") || line.contains("$$$") || line.contains("HR") || line.contains("Rozdzielanie")) {
                            sysArea.appendText(line + "\n");
                        }
                        else if (line.contains("!!!") || line.contains("płacze") || line.contains("FATAL") || line.contains("PORAŻKA") || line.contains("zawalił") || line.contains("błędów") || line.contains("BANKRUCTWO")) {
                            alertArea.appendText(line + "\n");
                        }
                        else {
                            workerArea.appendText(line + "\n");
                        }
                    });
                } else {
                    buffer.write(b);
                }
            }
        };

        // nowy strumień jako domyślny zamiast zwykłej konsoli
        try {
            System.setOut(new PrintStream(out, true, "UTF-8"));
        } catch (Exception e) {
            System.out.println("Błąd z polskimi znakami");
        }

        //  Złożenie okna w całość (Lewa + Prawa)
        // --- NOWE: ZAKŁADKI (TABS) ---
        javafx.scene.control.TabPane rightPanel = new javafx.scene.control.TabPane();

        javafx.scene.control.Tab logTab = new javafx.scene.control.Tab("Dziennik Zdarzeń");
        logTab.setClosable(false);
        logTab.setContent(logsPanel); // Wrzucamy tu nasze 3 okienka z logami

        javafx.scene.control.Tab statsTab = new javafx.scene.control.Tab("Analityka (Wykresy)");
        statsTab.setClosable(false);
        game.view.StatisticsDashboard dashboard = new game.view.StatisticsDashboard();
        statsTab.setContent(dashboard.getLayout()); // Wrzucamy tu wykresy

        rightPanel.getTabs().addAll(logTab, statsTab);

        // 5. Złożenie okna w całość (Gra + Zakładki po prawej)
        HBox mainLayout = new HBox();
        mainLayout.getChildren().addAll(gameRoot, rightPanel); // <--- Zmiana z logsPanel na rightPanel!

        Scene scene = new Scene(mainLayout);
        primaryStage.setTitle("Symulacja Biura IT");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Pierwsze narysowanie mapy
        gameView.render(simulation);

        // 6. Pętla gry
        AnimationTimer timer = new AnimationTimer() {
            private long lastUpdate = 0;


            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 1_000_000_000) {
                    simulation.step();

                    // --- TO JEST TEN BRAKUJĄCY FRAGMENT OD WYKRESÓW ---
                    Platform.runLater(() -> {
                        dashboard.updateCharts(
                                simulation.getStepCount(),
                                simulation.getBudget(),
                                simulation.getTotalFails(),
                                simulation.getAverageEfficiency(),
                                simulation.getCoffeesDrunk()
                        );
                    });
                    // --------------------------------------------------

                    lastUpdate = now;
                }

                for (Agent agent : simulation.getAgents()) {
                    agent.updateVisual();
                }

                gameView.render(simulation);
            }
        };

        timer.start();

        timer.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}