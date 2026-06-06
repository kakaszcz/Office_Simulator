package game.view;

import game.core.Simulation;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class MainLayout {

    private final Scene scene;
    private final Label globalBudgetLabel;
    private final StatisticsDashboard dashboard;
    private final HRDashboard hrDashboard;
    private final SimulationStatsPanel statsPanel;

    public MainLayout(Simulation simulation, GameView gameView) {
        // --- GÓRNY PASEK Z BUDŻETEM ---
        globalBudgetLabel = new Label("BUDŻET: " + String.format("%.2f", simulation.getBudget()) + " $");
        globalBudgetLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2ce62c; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 4, 0, 1, 1);");

        HBox topBar = new HBox(globalBudgetLabel);
        topBar.setAlignment(Pos.CENTER);
        topBar.setPadding(new Insets(5, 10, 5, 10));
        topBar.setStyle("-fx-background-color: linear-gradient(to bottom, #4a4a4a, #222222); -fx-border-color: #111; -fx-border-width: 0 0 2 0;");

        //  LEWA STRONA (PLANSZA GRY Z PRZESUWANIEM)
        StackPane gameRoot = new StackPane();
        Group group = new Group(gameView.getCanvas());
        gameRoot.getChildren().add(group);

        Scale scale = new Scale(0.45, 0.45);
        scale.setPivotX(0);
        scale.setPivotY(0);
        gameView.getCanvas().getTransforms().add(scale);

        // PRAWA STRONA (PANEL LOGÓW I WYKRESÓW)
        VBox logsPanel = new VBox();
        logsPanel.setSpacing(10);
        logsPanel.setPadding(new Insets(15));

        Label lblSys = new Label("SYSTEM I FINANSE:");
        TextArea sysArea = new TextArea();
        sysArea.setEditable(false);
        VBox.setVgrow(sysArea, Priority.ALWAYS);

        Label lblWork = new Label("AKCJE PRACOWNIKÓW:");
        TextArea workerArea = new TextArea();
        workerArea.setEditable(false);
        VBox.setVgrow(workerArea, Priority.ALWAYS);

        Label lblAlert = new Label("PROBLEMY I BŁĘDY:");
        lblAlert.setTextFill(Color.RED);
        TextArea alertArea = new TextArea();
        alertArea.setEditable(false);
        alertArea.setStyle("-fx-text-fill: red;");
        VBox.setVgrow(alertArea, Priority.ALWAYS);

        logsPanel.getChildren().addAll(lblSys, sysArea, lblWork, workerArea, lblAlert, alertArea);

        setupConsoleCapture(sysArea, workerArea, alertArea);

        // ZAKŁADKI (Inicjalizacja zawartości)
        TabPane rightPanel = new TabPane();

        // 1. Dziennik Zdarzeń
        Tab logTab = new Tab("Dziennik Zdarzeń");
        logTab.setClosable(false);
        logTab.setContent(logsPanel);

        // 2. Analityka (Wykresy)
        Tab statsTab = new Tab("Analityka (Wykresy)");
        statsTab.setClosable(false);
        dashboard = new StatisticsDashboard();
        ScrollPane scrollPane = new ScrollPane(dashboard.getLayout());
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        statsTab.setContent(scrollPane);

        // 3. Kadry (Pracownicy)
        hrDashboard = new HRDashboard();
        Tab hrTab = new Tab("Kadry (Pracownicy)");
        hrTab.setClosable(false);
        hrTab.setContent(hrDashboard.getLayout());

        // Statystyki Globalne
        statsPanel = new SimulationStatsPanel();
        Tab globalStatsTab = new Tab("Statystyki Globalne");
        globalStatsTab.setClosable(false);
        ScrollPane statsScrollPane = new ScrollPane(statsPanel);
        statsScrollPane.setFitToWidth(true);
        statsScrollPane.setStyle("-fx-background-color: transparent;");
        globalStatsTab.setContent(statsScrollPane);

        // --- ZMIANA KOLEJNOŚCI: Wrzucamy globalStatsTab jako pierwszy element listy ---
        rightPanel.getTabs().addAll(globalStatsTab, logTab, statsTab, hrTab);


        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(gameRoot, rightPanel);
        splitPane.setDividerPositions(0.65);
        VBox.setVgrow(splitPane, Priority.ALWAYS);

        VBox rootLayout = new VBox();
        rootLayout.getChildren().addAll(topBar, splitPane);

        this.scene = new Scene(rootLayout);
    }

    private void setupConsoleCapture(TextArea sysArea, TextArea workerArea, TextArea alertArea) {
        OutputStream out = new OutputStream() {
            private ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            @Override
            public void write(int b) {
                if (b == '\r') return;
                if (b == '\n') {
                    String line = new String(buffer.toByteArray(), StandardCharsets.UTF_8);
                    buffer.reset();
                    Platform.runLater(() -> {
                        if (line.isEmpty() || line.trim().isEmpty()) return;

                        if (line.contains("TURA") || line.contains("$$$") || line.contains("HR") || line.contains("Rozdzielanie")) {
                            sysArea.appendText(line + "\n");
                        } else if (line.contains("!!!") || line.contains("płacze") || line.contains("FATAL") || line.contains("PORAŻKA") || line.contains("zawalił") || line.contains("błędów") || line.contains("BANKRUCTWO")) {
                            alertArea.appendText(line + "\n");
                        } else {
                            workerArea.appendText(line + "\n");
                        }
                    });
                } else {
                    buffer.write(b);
                }
            }
        };

        try {
            System.setOut(new PrintStream(out, true, "UTF-8"));
        } catch (Exception e) {
            System.out.println("Błąd z polskimi znakami");
        }
    }

    public Scene getScene() {
        return scene;
    }

    public void update(Simulation simulation) {
        double currentBudget = simulation.getBudget();

        if (currentBudget <= 0) {
            globalBudgetLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #ff3333; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 4, 0, 1, 1);");
            globalBudgetLabel.setText("BANKRUCTWO: " + String.format("%.2f", currentBudget) + " $");
        } else {
            globalBudgetLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2ce62c; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 4, 0, 1, 1);");
            globalBudgetLabel.setText("BUDŻET: " + String.format("%.2f", currentBudget) + " $");
        }

        dashboard.updateCharts(
                simulation.getStepCount(),
                currentBudget,
                simulation.getTotalFails(),
                simulation.getAverageEfficiency(),
                simulation.getCoffeesDrunk()
        );

        if (simulation.getHRManager() != null) {
            hrDashboard.update(simulation.getHRManager());
        }

        if (statsPanel != null) {
            statsPanel.update(simulation);
        }
    }

    public game.view.HRDashboard getHRDashboard() {
        return hrDashboard;
    }
}