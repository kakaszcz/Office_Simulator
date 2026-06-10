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

     //EKRAN STARTOWY (MENU KONFIGURACYJNE)
    private void showSetupScreen() {
        VBox setupRoot = new VBox(20);
        setupRoot.setAlignment(Pos.CENTER);
        setupRoot.setStyle("-fx-background-color: #2c3e50; -fx-padding: 50px;");

        Label titleLabel = new Label("SYMULACJA BIURA IT");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 36px; -fx-font-weight: bold;");

        Label subtitleLabel = new Label("Konfiguracja początkowa");
        subtitleLabel.setStyle("-fx-text-fill: #bdc3c7; -fx-font-size: 18px;");

        // POLA DO USTAWIENIA PARAMETRÓW

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

        //  PRZYCISK START
        Button startButton = new Button("ROZPOCZNIJ SYMULACJĘ");
        startButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: 10 30 10 30;");

        startButton.setOnAction(e -> {
            int startJuniors = juniorsSpinner.getValue();
            int startSeniors = seniorsSpinner.getValue();
            int startBudget = budgetSpinner.getValue();

            startGame(startJuniors, startSeniors, startBudget);
        });

        // Efekt najechania na przycisk
        startButton.setOnMouseEntered(e -> startButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: 10 30 10 30;"));
        startButton.setOnMouseExited(e -> startButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: 10 30 10 30;"));

        setupRoot.getChildren().addAll(titleLabel, subtitleLabel, new Label(""), juniorsBox, seniorsBox, budgetBox, new Label(""), startButton);

        Scene setupScene = new Scene(setupRoot, 800, 600);
        primaryStage.setScene(setupScene);
        primaryStage.show();
    }

    //WŁAŚCIWA GRA (Odpalana po kliknięciu START)

    private void startGame(int startJuniors, int startSeniors, int startBudget) {
        // Przekazujemy wartości z menu do symulacji
        simulation = new Simulation(startJuniors, startSeniors, startBudget);
        simulation.setMainApp(this);

        gameView = new GameView(simulation.getGameBoard());

        // Utworzenie całego układu interfejsu
        mainLayout = new MainLayout(simulation, gameView);

        // TWORZENIE KONTROLERA CZASU I PASKA SZYBKOŚCI
        gameController = new GameController(simulation, mainLayout.getHRDashboard());
        HBox speedPanel = gameController.createSpeedControlPanel();

        // Integracja paska z widokiem w jeden pionowy kontener
        VBox appRoot = new VBox();
        appRoot.getChildren().addAll(speedPanel, mainLayout.getScene().getRoot());

        // GŁÓWNY KONTENER WARSTWOWY
        rootContainer = new StackPane();
        rootContainer.getChildren().add(appRoot);

        // Przełączamy scenę na grę i ZABEZPIECZAMY ROZMIAR OKNA
        Scene gameScene = new Scene(rootContainer, primaryStage.getWidth(), primaryStage.getHeight());
        primaryStage.setScene(gameScene);

        primaryStage.setMaximized(false);
        primaryStage.setMaximized(true);

        // Pierwsze narysowanie mapy
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

    public void showGameOverScreen(String message) {
        if (timer != null) {
            timer.stop();
        }

        StackPane gameOverOverlay = new StackPane();
        gameOverOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85);");
        gameOverOverlay.setAlignment(Pos.CENTER);

        Label gameOverLabel = new Label(message + "\n\n[ KLIKNIJ, ABY ZAMKNĄĆ GRĘ ]");
        gameOverLabel.setStyle("-fx-text-fill: #ff3333; -fx-font-size: 42px; -fx-font-weight: bold;");

        gameOverLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        gameOverOverlay.getChildren().add(gameOverLabel);

        gameOverOverlay.setOnMouseClicked(e -> System.exit(0));

        if (rootContainer != null) {
            rootContainer.getChildren().add(gameOverOverlay);
            gameOverOverlay.toFront();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}