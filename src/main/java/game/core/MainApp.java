package game.core;

import game.agents.Agent;
import game.view.GameView;
import game.view.MainLayout;
import javafx.animation.AnimationTimer; //mechanizm do wykonywania kodu przy kazdej klatce animacji (plynne przesuwanie, odswiezanie)
import javafx.application.Application; //bazowa klasa aplikacji javafx
import javafx.geometry.Pos; // do wyrownywania elementow
import javafx.scene.Scene; //zawartosc okna
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner; //kontrolka z wyborem liczby ze strzalkami
import javafx.scene.layout.HBox; //ukl elementy poziomo
import javafx.scene.layout.VBox; //ukl elem pionowo
import javafx.scene.layout.StackPane; //ukl elementy warstowowo
import javafx.stage.Stage; //glowne okno

/**
 * Główna klasa uruchomieniowa aplikacji (punkt wejścia JavaFX).
 * Odpowiada za inicjalizację okna, zarządzanie przełączaniem ekranów (menu startowe,
 * właściwa symulacja, ekran końca gry) oraz obsługę płynnej animacji agentów
 * za pomocą timera systemowego.
 */
public class MainApp extends Application {

    /** Główne okno aplikacji (JavaFX). */
    private Stage primaryStage;

    private Simulation simulation;
    private GameView gameView;
    private MainLayout mainLayout; //plansza, panele boczne, logi itp
    private GameController gameController; //odpala lub zatrzymuje petle tur

    /** Główny licznik odświeżania grafiki odpowiedzialny za płynny render klatek animacji. */
    private AnimationTimer timer;

    /** Główny kontener przechowujący warstwy interfejsu (widok gry oraz nakładkę Game Over). */
    private StackPane rootContainer;

    /** Kolejka punktów nawigacyjnych ścieżki wizualnej dla płynnych przejść agentów. */
    private java.util.Queue<int[]> visualPath = new java.util.LinkedList<>();

    /**
     * Główna metoda startowa inicjalizująca parametry okna i wywołująca ekran konfiguracji.
     *
     * @param primaryStage Główna scena dostarczona przez system.
     */
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle(GameConfiguration.APP_WINDOW_TITLE); //tytul brany z klasy game config
        this.primaryStage.setMaximized(true);

        showSetupScreen();
    }

    /**
     * Dodaje punkt współrzędnych do kolejki kroków animacji wizualnej.
     *
     * @param wx Logiczna współrzędna X punktu docelowego na planszy.
     * @param wy Logiczna współrzędna Y punktu docelowego na planszy.
     */
    public void addWaypoint(int wx, int wy) {
        visualPath.add(new int[]{wx, wy});
    }

    /**
     * Tworzy i wyświetla ekran startowy (menu konfiguracyjne).
     * Umożliwia użytkownikowi dynamiczne dobranie parametrów wejściowych symulacji,
     * takich jak budżet czy początkowy skład osobowy zespołu IT.
     */
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
            int startJuniors = juniorsSpinner.getValue(); //pobieranie wartosci ze spinnera
            int startSeniors = seniorsSpinner.getValue();
            int startBudget = budgetSpinner.getValue();

            startGame(startJuniors, startSeniors, startBudget);
        });

        startButton.setOnMouseEntered(e -> startButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: 10 30 10 30;"));
        startButton.setOnMouseExited(e -> startButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: 10 30 10 30;"));

        Button exitSimulationButton = createExitSimulationButton(false); //false czyli nie ma wracac do ekranu konfiguracji tylko zamknac aplikacje

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

        if (primaryStage.getScene() == null) { //jesli nie ma ustawionej sceny to tworzymy
            Scene setupScene = new Scene(setupRoot, 800, 600);
            primaryStage.setScene(setupScene);
        } else {
            primaryStage.getScene().setRoot(setupRoot);
        }
        primaryStage.show();
    }

    /**
     * Inicjalizuje podstawowe komponenty silnika symulacji, wiąże ze sobą kontrolery,
     * buduje docelową strukturę paneli JavaFX oraz uruchamia pętlę odświeżania grafiki.
     *
     * @param startJuniors Początkowa liczba pracowników poziomu Junior wybrana w menu.
     * @param startSeniors Początkowa liczba pracowników poziomu Senior wybrana w menu.
     * @param startBudget Początkowy stan budżetu firmy zadeklarowany przez użytkownika.
     */
    private void startGame(int startJuniors, int startSeniors, int startBudget) {
        simulation = new Simulation(startJuniors, startSeniors, startBudget); //glowny obiekt symulacji
        simulation.setMainApp(this);

        gameView = new GameView(simulation.getGameBoard());
        mainLayout = new MainLayout(simulation, gameView);

        gameController = new GameController(simulation, mainLayout.getHRDashboard());
        HBox speedPanel = gameController.createSpeedControlPanel(); //panel kontroli predkosci

        Button exitSimulationButton = createExitSimulationButton(true); //czyli po kliknieciu nie zamyka aplikacji tylko wraca do ekr konfig
        speedPanel.getChildren().add(exitSimulationButton);

        VBox appRoot = new VBox();
        appRoot.getChildren().addAll(speedPanel, mainLayout.getScene().getRoot());

        rootContainer = new StackPane();
        rootContainer.getChildren().add(appRoot); //gra jako pierwsza warstwa

        if (primaryStage.getScene() != null) {
            primaryStage.getScene().setRoot(rootContainer); //przejscie z ekr konfig do ekranu gry
        } else { //jak scena nie ist to tworzymy nową
            Scene gameScene = new Scene(rootContainer);
            primaryStage.setScene(gameScene); //ustawiamy scene jako gl element okna
        }

        // Stabilne wymuszenie pełnego ekranu (maksymalizacja ekranu)
        primaryStage.setMaximized(true);

        gameView.render(simulation); //pierwszy raz rysujemy plansze i agentow

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) { //odswiezenie jednej klatki symulacji
                if (!simulation.isRunning()) { //jesli symulacja nie dziala to wychodzi z metoy handle
                    return;
                }

                double currentSpeed = gameController.getSpeed(); //bierze predkosc z suwaka

                for (Agent agent : simulation.getAgents()) {
                    agent.updateVisual(currentSpeed);//przesuwa agentow zgodnie z curr predkoscia
                }
                gameView.render(simulation); //po przesunieciu agentow rysuje od nowa
                mainLayout.update(simulation); //aktualizacja paneli bocznych
            }
        };

        timer.start(); //uruchamia animation timer (grafika)
        gameController.startSimulation(); //uruchamia logike gry
    }

    /**
     * Tworzy uniwersalny przycisk wyjścia/powrotu, pozwalający opuścić tryb symulacji.
     *
     * @param returnToSetupScreen Definiuje czy przycisk ma cofać do menu konfiguracji (true),
     * czy całkowicie wyłączyć aplikację (false).
     * @return Skonfigurowany obiekt przycisku Button.
     */
    private Button createExitSimulationButton(boolean returnToSetupScreen) {
        Button exitSimulationButton = new Button("↩ Wyjdź z symulacji");
        exitSimulationButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 8 18 8 18;");

        exitSimulationButton.setOnAction(e -> {
            stopCurrentSimulation();

            if (returnToSetupScreen) { //gdy true to wraca do setup screen
                showSetupScreen();
            } else { //gdy false to zamyka aplikacje
                javafx.application.Platform.exit();
            }
        });

        return exitSimulationButton;
    }

    /**
     * Przeprowadza procedurę bezpiecznego czyszczenia i zatrzymywania działających pętli czasowych
     * oraz wątków logicznych symulacji przed zmianą ekranu lub zamknięciem programu.
     */
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

    /**
     * Wyświetla ekran podsumowujący (GameOver) nakładki na planszę gry.
     * Generuje szczegółowy raport końcowy z twardymi danymi statystycznymi.
     *
     * @param message Komunikat tekstowy informujący o przyczynie zakończenia symulacji (np. bankructwo).
     */
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

        Button viewStatsButton = new Button("PRZEGLĄDAJ STATYSTYKI");
        viewStatsButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 20 10 20; -fx-cursor: hand;");

        viewStatsButton.setOnAction(e -> {
            if (rootContainer != null) {
                rootContainer.getChildren().remove(gameOverOverlay);
            }
        });

        buttonPanel.getChildren().addAll(
                restartButton,
                viewStatsButton
        );

        reportCard.getChildren().addAll(titleLabel, reasonLabel, statsSection, buttonPanel);
        gameOverOverlay.getChildren().add(reportCard);

        if (rootContainer != null) {
            rootContainer.getChildren().add(gameOverOverlay);
            gameOverOverlay.toFront();
        }
    }

    /**
     * Pomocnik generujący ujednolicony pod względem wizualnym wiersz danych statystycznych.
     *
     * @param textLabel Opis parametru statystycznego.
     * @param textValue Sformatowana wartość liczbowa lub tekstowa parametru.
     * @param styleLabel Styl CSS dla etykiety opisowej.
     * @param styleValue Styl CSS dla pola wartości.
     * @return Skonfigurowany wiersz danych w formacie HBox.
     */
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

    /**
     * Główny punkt wejścia programu w środowisku Java standard. Urządza rozruch środowiska JavaFX.
     *
     * @param args Argumenty wiersza poleceń.
     */
    public static void main(String[] args) {
        launch(args);
    }
}