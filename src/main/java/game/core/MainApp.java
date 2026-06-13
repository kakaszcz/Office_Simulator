package game.core;

import game.agents.Agent;
import game.view.GameView;
import game.view.MainLayout;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainApp extends Application {

    private Stage primaryStage; // Główne okno aplikacji

    private Simulation simulation;
    private GameView gameView;
    private MainLayout mainLayout;
    private GameController gameController;

    private AnimationTimer timer;
    private StackPane rootContainer;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle(GameConfiguration.APP_WINDOW_TITLE);
        this.primaryStage.setMaximized(true);

        // Zamiast odpalać grę, najpierw pokazujemy menu startowe
        showSetupScreen();
    }

    // Kolejka ścieżki wizualnej
    private java.util.Queue<int[]> visualPath = new java.util.LinkedList<>();

    // Dodawanie kroku do animacji
    public void addWaypoint(int wx, int wy) {
        visualPath.add(new int[]{wx, wy});
    }

    // EKRAN STARTOWY (MENU KONFIGURACYJNE)
    private void showSetupScreen() {
        VBox setupRoot = new VBox(20);
        setupRoot.setAlignment(Pos.CENTER);
        setupRoot.setStyle("-fx-background-color: #2c3e50; -fx-padding: 50px;");
        Label titleLabel = new Label("SYMULACJA BIURA IT");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 36px; -fx-font-weight: bold;");

        Label subtitleLabel = new Label("Konfiguracja początkowa");
        subtitleLabel.setStyle("-fx-text-fill: #bdc3c7; -fx-font-size: 18px;");

        // 1. Liczba Juniorów
        HBox juniorsBox = new HBox(15);
        juniorsBox.setAlignment(Pos.CENTER);
        Label juniorsLabel = new Label("Liczba Juniorów na start:");
        juniorsLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
        Spinner<Integer> juniorsSpinner = new Spinner<>(0, GameConfiguration.MAX_JUNIORS, GameConfiguration.STARTING_JUNIORS_COUNT);
        juniorsBox.getChildren().addAll(juniorsLabel, juniorsSpinner);

        // 2. Liczba Seniorów
        HBox seniorsBox = new HBox(15);
        seniorsBox.setAlignment(Pos.CENTER);
        Label seniorsLabel = new Label("Liczba Seniorów na start:");
        seniorsLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
        Spinner<Integer> seniorsSpinner = new Spinner<>(0, GameConfiguration.MAX_SENIORS, GameConfiguration.STARTING_SENIORS_COUNT);
        seniorsBox.getChildren().addAll(seniorsLabel, seniorsSpinner);

        // 3. Budżet początkowy
        HBox budgetBox = new HBox(15);
        budgetBox.setAlignment(Pos.CENTER);
        Label budgetLabel = new Label("Początkowy budżet ($):");
        budgetLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
        Spinner<Integer> budgetSpinner = new Spinner<>(100, 10000, (int) GameConfiguration.STARTING_BUDGET, 100);
        budgetSpinner.setEditable(true);
        budgetBox.getChildren().addAll(budgetLabel, budgetSpinner);

        // PRZYCISK START
        Button startButton = new Button("ROZPOCZNIJ SYMULACJĘ");
        startButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: 10 30 10 30;");

        startButton.setOnAction(e -> {
            int startJuniors = juniorsSpinner.getValue();
            int startSeniors = seniorsSpinner.getValue();
            int startBudget = budgetSpinner.getValue();

            startGame(startJuniors, startSeniors, startBudget);
        });

        startButton.setOnMouseEntered(e -> startButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: 10 30 10 30;"));
        startButton.setOnMouseExited(e -> startButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: 10 30 10 30;"));

        Button exitSimulationButton = createExitSimulationButton(false);

        setupRoot.getChildren().addAll(
                titleLabel,
                subtitleLabel,
                new Label(""),
                juniorsBox,
                seniorsBox,
                budgetBox,
                new Label(""),
                startButton,
                exitSimulationButton
        );

        // --- POPRAWKA BEZPIECZNEGO ODŚWIEŻANIA SCENY STARTOWEJ ---
        if (primaryStage.getScene() == null) {
            Scene setupScene = new Scene(setupRoot, 800, 600);
            primaryStage.setScene(setupScene);
        } else {
            primaryStage.getScene().setRoot(setupRoot);
        }
        primaryStage.show();
    }

    // WŁAŚCIWA GRA (Odpalana po kliknięciu START)
    private void startGame(int startJuniors, int startSeniors, int startBudget) {
        simulation = new Simulation(startJuniors, startSeniors, startBudget);
        simulation.setMainApp(this);

        gameView = new GameView(simulation.getGameBoard());
        mainLayout = new MainLayout(simulation, gameView);

        gameController = new GameController(simulation, mainLayout.getHRDashboard());
        HBox speedPanel = gameController.createSpeedControlPanel();

        Button exitSimulationButton = createExitSimulationButton(true);
        speedPanel.getChildren().add(exitSimulationButton);

        VBox appRoot = new VBox();
        appRoot.getChildren().addAll(speedPanel, mainLayout.getScene().getRoot());

        rootContainer = new StackPane();
        rootContainer.getChildren().add(appRoot);

        // --- POPRAWKA PRZEPIĘCIA WIDOKU PLANSZY BEZ ZMIANY ROZMIARU OKNA ---
        if (primaryStage.getScene() != null) {
            primaryStage.getScene().setRoot(rootContainer);
        } else {
            Scene gameScene = new Scene(rootContainer);
            primaryStage.setScene(gameScene);
        }

        // Stabilne wymuszenie pełnego ekranu
        primaryStage.setMaximized(true);

        gameView.render(simulation);

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!simulation.isRunning()) {
                    return;
                }

                double currentSpeed = gameController.getSpeed();

                for (Agent agent : simulation.getAgents()) {
                    agent.updateVisual(currentSpeed);
                }
                gameView.render(simulation);
                mainLayout.update(simulation);
            }
        };

        timer.start();
        gameController.startSimulation();
    }

    private Button createExitSimulationButton(boolean returnToSetupScreen) {
        Button exitSimulationButton = new Button("↩ Wyjdź z symulacji");
        exitSimulationButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 8 18 8 18;");

        exitSimulationButton.setOnAction(e -> {
            stopCurrentSimulation();

            if (returnToSetupScreen) {
                showSetupScreen();
            } else {
                javafx.application.Platform.exit();
            }
        });

        return exitSimulationButton;
    }

    private void stopCurrentSimulation() {
        if (simulation != null) {
            simulation.stop();
        }

        if (timer != null) {
            timer.stop();
            timer = null;
        }

        if (gameController != null) {
            gameController.stopSimulationLoop();
        }
    }

    // Ekran końca gry z raportem i przeglądaniem
    public void showGameOverScreen(String message) {
        if (timer != null) {
            timer.stop(); // Zatrzymujemy silnik wizualny
        }

        // Ciemna nakładka blokująca planszę
        StackPane gameOverOverlay = new StackPane();
        gameOverOverlay.setStyle("-fx-background-color: rgba(21, 32, 43, 0.85);");
        gameOverOverlay.setAlignment(Pos.CENTER);

        // Główna karta raportu
        VBox reportCard = new VBox(25);
        reportCard.setAlignment(Pos.CENTER);
        reportCard.setStyle("-fx-background-color: #2c3e50; -fx-padding: 40px; -fx-border-color: #e74c3c; -fx-border-width: 3px; -fx-border-radius: 10px; -fx-background-radius: 10px;");
        reportCard.setMaxWidth(650);
        reportCard.setMaxHeight(600);

        Label titleLabel = new Label("SYMULACJA ZAKOŃCZONA");
        titleLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 32px; -fx-font-weight: bold;");

        Label reasonLabel = new Label(message);
        reasonLabel.setStyle("-fx-text-fill: #ecf0f1; -fx-font-size: 16px; -fx-font-style: italic;");
        reasonLabel.setWrapText(true);
        reasonLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        // Kontener na twarde dane statystyczne z klasy Simulation
        VBox statsSection = new VBox(12);
        statsSection.setStyle("-fx-background-color: #34495e; -fx-padding: 20px; -fx-background-radius: 6px;");
        statsSection.setAlignment(Pos.CENTER);

        String labelStyle = "-fx-text-fill: #95a5a6; -fx-font-size: 16px;";
        String valueStyle = "-fx-text-fill: #ffffff; -fx-font-size: 16px; -fx-font-weight: bold;";

        statsSection.getChildren().addAll(
                createStatRow("Czas przetrwania:", simulation.getSimulationTimeFormatted() + " (" + simulation.getStepCount() + " tur)", labelStyle, valueStyle),
                createStatRow("Końcowy stan konta:", String.format("%.2f $", simulation.getBudget()), labelStyle, "-fx-text-fill: #e74c3c; -fx-font-size: 16px; -fx-font-weight: bold;"),
                createStatRow("Ukończone zadania (Sukcesy):", String.valueOf(simulation.getTotalTasksSuccess()), labelStyle, "-fx-text-fill: #2ecc71; -fx-font-size: 16px; -fx-font-weight: bold;"),
                createStatRow("Wypuszczone błędy (Porażki):", String.valueOf(simulation.getTotalTasksFailed()), labelStyle, "-fx-text-fill: #e74c3c; -fx-font-size: 16px; -fx-font-weight: bold;"),
                createStatRow("Ogólna skuteczność biura:", String.format("%.1f %%", simulation.getSuccessRate()), labelStyle, "-fx-text-fill: #f1c40f; -fx-font-size: 16px; -fx-font-weight: bold;"),
                createStatRow("Awarie krytyczne (Fatal Errors):", String.valueOf(simulation.getTotalFatalErrors()), labelStyle, valueStyle)
        );

        // Przyciski sterujące raportem
        HBox buttonPanel = new HBox(20);
        buttonPanel.setAlignment(Pos.CENTER);

        Button restartButton = new Button("NOWA SYMULACJA");
        restartButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 20 10 20; -fx-cursor: hand;");
        restartButton.setOnAction(e -> {
            if (rootContainer != null) {
                rootContainer.getChildren().remove(gameOverOverlay);
            }
            showSetupScreen(); // Czysty powrót do konfiguracji startowej
        });

        Button exitSimulationButton = createExitSimulationButton(true);

        buttonPanel.getChildren().addAll(
                restartButton,
                exitSimulationButton
        );
        reportCard.getChildren().addAll(titleLabel, reasonLabel, statsSection, buttonPanel);
        gameOverOverlay.getChildren().add(reportCard);

        if (rootContainer != null) {
            rootContainer.getChildren().add(gameOverOverlay);
            gameOverOverlay.toFront();
        }
    }

    // Pomocnik do generowania czystych wierszy danych
    private HBox createStatRow(String textLabel, String textValue, String styleLabel, String styleValue) {
        Label label = new Label(textLabel);
        label.setStyle(styleLabel);
        label.setPrefWidth(280);

        Label value = new Label(textValue);
        value.setStyle(styleValue);

        HBox row = new HBox(10, label, value);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    public static void main(String[] args) {
        launch(args);
    }
}