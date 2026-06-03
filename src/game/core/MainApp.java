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

        // przy użyciu 'Group', poniższa linijka automatycznie stworzy scenę

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

            @Override
            public void handle(long now) {
                // Wykonuj turę w pamięci co 1 sekundę (1 000 000 000 ns)
                // W tym momencie agenci natychmiastowo zmieniają swoje kafelki docelowe (x, y)
                if (now - lastUpdate >= 1_000_000_000) {
                    simulation.step();
                    lastUpdate = now;
                }

                // Wywoływana w każdej klatce (60 razy na sekundę)
                // Każdy agent przybliża się (visualX/Y) do swojego kafelka docelowego
                for (Agent agent : simulation.getAgents()) {
                    agent.updateVisual();
                }

                // C. RENDER: Odświeżamy ekran z pełną prędkością monitora (60 FPS)
                // Rysujemy agentów na ich aktualnych, "pływających" pozycjach wizualnych
                gameView.render(simulation);
            }
        };

        timer.start();
    }

    public static void main(String[] args) {
        launch(args); // Ta metoda odpala bibliotekę JavaFX
    }
}