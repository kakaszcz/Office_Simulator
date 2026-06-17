package game.core;

import game.view.HRDashboard;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

/**
 * Kontroler główny zarządzający pętlą czasu (Game Loop) symulacji oraz panelem sterowania.
 * Odpowiada za cykliczne wywoływanie kroków logiki biznesowej, aktualizację widoków
 * oraz dynamiczną manipulację prędkością działania aplikacji za pomocą UI.
 */
public class GameController {

    private final Simulation simulation;
    private final HRDashboard hrDashboard; //przechowuje panel hr
    private Timeline gameLoop; //logika tur
    private Button playPauseBtn; // Pole klasy do synchronizacji stanu przycisku

    /**
     * Tworzy nowy obiekt kontrolera gier i inicjalizuje pętlę czasu.
     *
     * @param simulation Główny obiekt silnika symulacji zawierający logikę biznesową.
     * @param hrDashboard Panel widoku HR, który ma być odświeżany co każdą turę.
     */
    public GameController(Simulation simulation, HRDashboard hrDashboard) {
        this.simulation = simulation;
        this.hrDashboard = hrDashboard;

        // Inicjalizacja pętli gry oparta o czas z konfiguracji
        initGameLoop();
    }

    /**
     * Inicjalizuje pętlę główną gry bazując na interwale czasowym z konfiguracji.
     * Definiuje zachowanie aplikacji przy każdym ticku oraz mechanizm awaryjnego
     * zatrzymania w przypadku wykrycia końca gry lub bankructwa firmy.
     */
    private void initGameLoop() {
        KeyFrame keyFrame = new KeyFrame(Duration.millis(GameConfiguration.GAME_LOOP_BASE_TICK_MS), event -> { //czas miedzy wywolaniami
            // Zatrzymujemy pętlę Timeline po wykryciu bankructwa/końca gry, by nie obciążać procesora w tle
            if (!simulation.isRunning()) {
                System.out.println(">>> [GameLoop] Wykryto zatrzymanie symulacji. Zatrzymuję pętlę główną kontrolera.");
                gameLoop.stop();
                if (playPauseBtn != null) {
                    playPauseBtn.setText("▶ Start"); //ustawia tekst przycisku na start gdy symulacja zostala zatrzymana
                    playPauseBtn.setDisable(true); // Blokujemy przycisk na ekranie Game Over
                }
                return;
            }

            simulation.step();
            hrDashboard.update(simulation.getHRManager());
        });

        gameLoop = new Timeline(keyFrame);
        gameLoop.setCycleCount(Animation.INDEFINITE); // Pętla działa w nieskończoność
    }

    /**
     * Tworzy i konfiguruje graficzny panel sterowania prędkością symulacji (HBox).
     * Panel zawiera przycisk start/pauza oraz suwak umożliwiający płynną,
     * dynamiczną zmianę szybkości wykonywania kolejnych tur symulacji.
     *
     * @return Skonfigurowany kontener HBox z kontrolkami interfejsu użytkownika.
     */
    public HBox createSpeedControlPanel() {
        HBox panel = new HBox(GameConfiguration.UI_SPEED_PANEL_SPACING);
        panel.setAlignment(Pos.CENTER_LEFT);
        panel.setPadding(new Insets(10, 15, 10, 15));
        panel.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");

        playPauseBtn = new Button("⏸ Pauza");

        playPauseBtn.setOnAction(e -> {
            // Sprawdzamy stan pętli gry i przełączamy go dynamicznie
            if (gameLoop.getStatus() == Animation.Status.RUNNING) {
                gameLoop.pause();
                playPauseBtn.setText("▶ Start");
            } else {
                if (simulation.isRunning()) {
                    gameLoop.play();
                    playPauseBtn.setText("⏸ Pauza");
                }
            }
        });

        Slider speedSlider = new Slider(
                GameConfiguration.SPEED_SLIDER_MIN,
                GameConfiguration.SPEED_SLIDER_MAX,
                GameConfiguration.SPEED_SLIDER_DEFAULT
        );
        speedSlider.setBlockIncrement(GameConfiguration.SPEED_SLIDER_BLOCK_INCREMENT);
        speedSlider.setMajorTickUnit(GameConfiguration.SPEED_SLIDER_MAJOR_TICK);
        speedSlider.setShowTickMarks(true);

        Label speedLabel = new Label(String.format("Prędkość: %.2fx", GameConfiguration.SPEED_SLIDER_DEFAULT));
        speedLabel.setStyle("-fx-font-weight: bold;");

        // Łączymy suwak z prędkością Timeline (Dynamiczne przyspieszanie/zwalnianie!)
        gameLoop.rateProperty().bind(speedSlider.valueProperty());

        // Aktualizacja tekstu etykiety przy przesuwaniu suwaka
        speedSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            speedLabel.setText(String.format("Prędkość: %.2fx", newValue.doubleValue()));
        });

        // Wrzucamy wszystko do paska
        panel.getChildren().addAll(playPauseBtn, new Label("Szybkość symulacji:"), speedSlider, speedLabel);

        return panel;
    }

    /**
     * Pobiera aktualny mnożnik prędkości działania pętli gry.
     *
     * @return Aktualna wartość prędkości zdefiniowana na suwaku.
     */
    public double getSpeed() {
        if (gameLoop != null) {
            return gameLoop.getRate(); // Pobiera aktualną wartość zbindowaną z suwakiem
        }
        return GameConfiguration.SPEED_SLIDER_DEFAULT;
    }

    /**
     * Uruchamia pętlę czasu symulacji i synchronizuje tekst na przycisku sterującym.
     */
    public void startSimulation() {
        if (gameLoop != null) {
            gameLoop.play();
            if (playPauseBtn != null) {
                playPauseBtn.setText("⏸ Pauza"); // Synchronizacja UI w razie odpalenia z zewnątrz
            }
        }
    }

    /**
     * Całkowicie zatrzymuje pętlę czasu symulacji i przywraca stan początkowy kontrolki UI.
     */
    public void stopSimulationLoop() {
        if (gameLoop != null) {
            gameLoop.stop();
        }

        if (playPauseBtn != null) {
            playPauseBtn.setText("▶ Start");
        }
    }

}