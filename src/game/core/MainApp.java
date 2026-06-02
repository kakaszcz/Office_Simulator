package game.core;

import game.view.GameView;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainApp extends Application {
    private Simulation simulation;
    private GameView gameView;

    @Override
    public void start(Stage primaryStage) {
        // 1. Tworzymy świat (np. 5 juniorów, 3 seniorów, 1000$ budżetu)
        simulation = new Simulation(5, 3, 1000);

        // 2. Tworzymy widok (przekazujemy wygenerowaną planszę)
        gameView = new GameView(simulation.getGameBoard());

        // 3. Konfigurujemy okienko aplikacji
        StackPane root = new StackPane();
        root.getChildren().add(gameView.getCanvas()); // Dodajemy płótno gry

        // --------- -------SKALOWANIE --------
        // 0.5 oznacza pomniejszenie okna o 50% na ekranie, przy zachowaniu ostrości grafik 128px
        double skala = 0.5;
        javafx.scene.transform.Scale scale = new javafx.scene.transform.Scale(skala, skala);
        scale.setPivotX(0);
        scale.setPivotY(0);
        gameView.getCanvas().getTransforms().add(scale);

        // Dynamicznie obliczamy mniejszy rozmiar okna--za pomocą skali
        double windowWidth = gameView.getCanvas().getWidth() * skala;
        double windowHeight = gameView.getCanvas().getHeight() * skala;

        Scene scene = new Scene(root);

        primaryStage.setTitle("Symulacja Biura IT");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false); // Blokujemy rozciąganie okna myszką(to nie popsuje proporcji)
        primaryStage.show();

        // 4. Rysujemy początkowy stan przed ruchem
        gameView.render();

        // 5. Konfiguracja Pętli Czasu (Game Loop)
        AnimationTimer timer = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                // Wykonuj turę co 1 miliard nanosekund (czyli równo co 1 sekundę)
                if (now - lastUpdate >= 1_000_000_000) {
                    simulation.step();   // Logika symulacji (kolejna tura)
                    gameView.render();   // Odświeżenie ekranu
                    lastUpdate = now;
                }
            }
        };

        timer.start();
    }

    public static void main(String[] args) {
        launch(args); // Ta metoda odpala bibliotekę JavaFX
    }
}
