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

/**
 * Klasa MainLayout odpowiada za główną kompozycję interfejsu użytkownika aplikacji.
 * Składa w jedną spójną przestrzeń planszę gry (warstwa wizualna) oraz panel boczny
 * zawierający dzienniki zdarzeń, wykresy analityczne oraz statystyki kadrowe (HR).
 * Implementuje również mechanizm przechwytywania standardowego wyjścia konsoli.
 */
public class MainLayout {

    /** Główna scena JavaFX przechowująca strukturę całego okna. */
    private final Scene scene;
    /** Etykieta wyświetlająca aktualny stan budżetu lub informację o bankructwie. */
    private final Label globalBudgetLabel;
    /** Panel zarządzający wykresami statystycznymi (Analityka). */
    private final StatisticsDashboard dashboard;
    /** Panel zarządzający informacjami o pracownikach (Kadry). */
    private final HRDashboard hrDashboard;
    /** Panel wyświetlający podsumowanie globalnych statystyk symulacji. */
    private final SimulationStatsPanel statsPanel;

    /**
     * Konstruktor głównego układu okna symulacji. Tworzy poszczególne panele,
     * konfiguruje ich rozmieszczenie oraz inicjalizuje podział okna (SplitPane).
     *
     * @param simulation Instancja silnika symulacji dostarczająca dane biznesowe.
     * @param gameView   Instancja silnika renderującego planszę biura.
     */
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

        // Zastosowanie transformacji skalującej dla płótna (Canvas) widoku gry
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

        // Aktywacja przechwytywania strumienia wyjściowego i mapowania do obszarów tekstowych
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

        rightPanel.getTabs().addAll(globalStatsTab, logTab, statsTab, hrTab);


        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(gameRoot, rightPanel);
        splitPane.setDividerPositions(0.65);
        VBox.setVgrow(splitPane, Priority.ALWAYS);

        VBox rootLayout = new VBox();
        rootLayout.getChildren().addAll(topBar, splitPane);

        this.scene = new Scene(rootLayout);
    }

    /**
     * Przekierowuje standardowy strumień System.out do niestandardowego wyjścia,
     * parsując tekst linia po linii i dystrybuując go do odpowiednich sekcji TextArea.
     * Prace na interfejsie są bezpiecznie delegowane do wątku JavaFX (Platform.runLater).
     *
     * @param sysArea    Obszar logów systemowych i finansowych.
     * @param workerArea Obszar logów aktywności pracowników.
     * @param alertArea  Obszar logów błędów, porażek i alertów krytycznych.
     */
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

                        // Klasyfikacja linii tekstu na podstawie słów kluczowych
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

    /**
     * Zwraca przygotowany obiekt sceny (Scene) zawierający kompletny układ UI.
     *
     * @return Główna scena aplikacji.
     */
    public Scene getScene() {
        return scene;
    }

    /**
     * Aktualizuje elementy widoku na podstawie bieżącego stanu symulacji.
     * Odświeża etykietę budżetu, wykresy analityczne oraz podległe panele statystyk i kadr.
     *
     * @param simulation Instancja silnika symulacji z aktualnymi danymi tury.
     */
    public void update(Simulation simulation) {
        double currentBudget = simulation.getBudget();

        // Weryfikacja stanu budżetu i dobór odpowiedniej stylizacji paska statusu
        if (currentBudget <= 0) {
            globalBudgetLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #ff3333; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 4, 0, 1, 1);");
            globalBudgetLabel.setText("BANKRUCTWO: " + String.format("%.2f", currentBudget) + " $");
        } else {
            globalBudgetLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2ce62c; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 4, 0, 1, 1);");
            globalBudgetLabel.setText("BUDŻET: " + String.format("%.2f", currentBudget) + " $");
        }

        // Przekazanie nowych serii danych do wykresów liniowych
        dashboard.updateCharts(
                simulation.getStepCount(),
                currentBudget,
                simulation.getTotalFails(),
                simulation.getAverageEfficiency(),
                simulation.getCoffeesDrunk()
        );

        // Odświeżenie danych w module kadr
        if (simulation.getHRManager() != null) {
            hrDashboard.update(simulation.getHRManager());
        }

        // Odświeżenie ogólnego panelu statystyk
        if (statsPanel != null) {
            statsPanel.update(simulation);
        }
    }

    /**
     * Zwraca instancję zintegrowanego komponentu zarządzania kadrami HRDashboard.
     *
     * @return Obiekt pulpitu HR.
     */
    public game.view.HRDashboard getHRDashboard() {
        return hrDashboard;
    }
}