package game.core;

import game.model.Agent;
import game.view.GameView;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainApp extends Application {
    private Simulation simulation;
    private GameView gameView;

    //OKIENKO
    @Override
    public void start(Stage primaryStage) {
        // 1. Tworzymy świat (np. 5 juniorów, 3 seniorów, 1000$ budżetu)
        simulation = new Simulation(5, 3, 1000);

        // 2. Tworzymy widok (przekazujemy wygenerowaną planszę)
        gameView = new GameView(simulation.getGameBoard());

        // 3. Konfigurujemy okienko aplikacji
        StackPane root = new StackPane();

        // Tworzymy grupę
        javafx.scene.Group group = new javafx.scene.Group(gameView.getCanvas());
        root.getChildren().add(group);

        // Nakładamy skalowanie bezpośrednio na płótno gry
        double skala = 0.5; // 0.5 to pomniejszenie o połowę
        javafx.scene.transform.Scale scale = new javafx.scene.transform.Scale(skala, skala);
        scale.setPivotX(0);
        scale.setPivotY(0);
        gameView.getCanvas().getTransforms().add(scale);

        Scene scene = new Scene(root);

        primaryStage.setTitle("Symulacja Biura IT");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false); // Blokujemy rozciąganie okna
        primaryStage.show();

        // 4. Rysujemy początkowy stan przed ruchem
        gameView.render(simulation);

        // =========================================================================
        // 5. NOWA PĘTLA CZASU
        // =========================================================================
        AnimationTimer timer = new AnimationTimer() {
            private long lastUpdate = 0;
            // Minimalny czas tury w nanosekundach (tu: 0.5 sekundy).
            // Zapobiega "przewijaniu" gry, gdy wszyscy stoją w miejscu.
            private final long MIN_TURN_TIME = 500_000_000;

            @Override
            public void handle(long now) {

                boolean somebodyWalking = false;

                // B. GRAFIKA GRY
                for (Agent agent : simulation.getAgents()) {
                    agent.updateVisual();

                    if (agent.isCurrentlyWalking()) {
                        somebodyWalking = true;
                    }
                }

                // A. LOGIKA GRY: Odpalamy turę gdy nikt nie idzie ORAZ minął czas
                if (!somebodyWalking && (now - lastUpdate >= MIN_TURN_TIME)) {
                    simulation.step();
                    lastUpdate = now; // Resetujemy stoper
                }

                // C. RENDER
                gameView.render(simulation);
            }
        };

        timer.start();
    }

    public static void main(String[] args) {
        launch(args); // Ta metoda odpala bibliotekę JavaFX
    }
}