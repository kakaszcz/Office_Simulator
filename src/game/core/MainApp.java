package game.core;

import game.model.Agent;
import game.view.GameView;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

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
        StackPane gameRoot = new StackPane();
        javafx.scene.Group group = new javafx.scene.Group(gameView.getCanvas());
        gameRoot.getChildren().add(group);

        // Nakładamy skalowanie bezpośrednio na płótno gry
        double skala = 0.5; // 0.5 to pomniejszenie o połowę
        javafx.scene.transform.Scale scale = new javafx.scene.transform.Scale(skala, skala);
        scale.setPivotX(0);
        scale.setPivotY(0);
        gameView.getCanvas().getTransforms().add(scale);

        TextArea logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefWidth(400);
        logArea.setWrapText(true);
        logArea.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 13px;");

        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) {
                Platform.runLater(() -> logArea.appendText(String.valueOf((char) b)));
            }

            public void write(byte[] b, int off, int len) {
                String text = new String(b, off, len);
                Platform.runLater(() -> logArea.appendText(text));
            }
        };
        System.setOut(new PrintStream(out, true));

        HBox mainLayout = new HBox();
        mainLayout.getChildren().addAll(gameRoot, logArea);

        Scene scene = new Scene(mainLayout);

        primaryStage.setTitle("Symulacja Biura IT");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false); // Blokujemy rozciąganie okna
        primaryStage.show();

        // 4. Rysujemy początkowy stan przed ruchem
        gameView.render(simulation);

        // 5. NOWA PĘTLA CZASU
        AnimationTimer timer = new AnimationTimer() {
            private long lastUpdate = 0;
            // Minimalny czas tury w nanosekundach (tu: 0.5 sekundy).
            // Zapobiega "przewijaniu" gry, gdy wszyscy stoją w miejscu.
            private final long MIN_TURN_TIME = 500_000_000;

            @Override
            public void handle(long now) {

                boolean somebodyWalking = false;

                // GRAFIKA GRY
                for (Agent agent : simulation.getAgents()) {
                    agent.updateVisual();

                    if (agent.isCurrentlyWalking()) {
                        somebodyWalking = true;
                    }
                }

                // LOGIKA GRY: Odpalamy turę gdy nikt nie idzie ORAZ minął czas
                if (!somebodyWalking && (now - lastUpdate >= MIN_TURN_TIME)) {
                    simulation.step();
                    lastUpdate = now; // Resetujemy stoper
                }

                // RENDER
                gameView.render(simulation);
            }
        };

        timer.start();
    }

    public static void main(String[] args) {
        launch(args); // Ta metoda odpala bibliotekę JavaFX
    }
}