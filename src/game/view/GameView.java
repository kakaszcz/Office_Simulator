package game.view;

import game.model.Cell;
import game.model.GameBoard;
import game.model.Agent;
import game.model.Boss;
import game.model.Senior;
import game.model.Junior;
import game.model.Worker;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public class GameView {

    private final GameBoard board;
    private final Canvas canvas;
    private final GraphicsContext gc;

    private static final int TILE_SIZE = 128; // Rozmiar kafelka na ekranie

    private static final int SPRITE_WIDTH = 128;
    private static final int SPRITE_HEIGHT = 128;

    private Image floorImage;
    private Image wallImage;

    private Image juniorImg;
    private Image seniorImg;
    private Image bossImg;

    private Image juniorCoffeeImg;
    private Image seniorCoffeeImg;
    private Image bossCoffeeImg;

    public GameView(GameBoard board) {
        this.board = board;

        int width = board.getWidth() * TILE_SIZE;
        int height = board.getHeight() * TILE_SIZE;

        this.canvas = new Canvas(width, height);
        this.gc = canvas.getGraphicsContext2D();

        loadImages();
    }

    private void loadImages() {
        try {
            this.juniorImg = new Image(getClass().getResourceAsStream("/images/junior.png"));
            this.seniorImg = new Image(getClass().getResourceAsStream("/images/senior.png"));
            this.bossImg = new Image(getClass().getResourceAsStream("/images/boss.png"));

            this.juniorCoffeeImg = new Image(getClass().getResourceAsStream("/images/junior_coffee.png"));
            this.seniorCoffeeImg = new Image(getClass().getResourceAsStream("/images/senior_coffee.png"));
            this.bossCoffeeImg = new Image(getClass().getResourceAsStream("/images/boss_coffee.png"));
        } catch (Exception e) {
            System.out.println("Nie udało się załadować obrazków, używam kolorów zastępczych.");
        }
    }

    public void render(game.core.Simulation sim) {
        // Czyszczenie ekranu
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Rysowanie planszy (tło)
        for (int y = 0; y < board.getHeight(); y++) {
            for (int x = 0; x < board.getWidth(); x++) {
                Cell cell = board.getCell(x, y);

                int px = x * TILE_SIZE;
                int py = y * TILE_SIZE;

                if (cell.isWall()) {
                    if (wallImage != null) gc.drawImage(wallImage, px, py, TILE_SIZE, TILE_SIZE);
                    else drawPlaceholder(px, py, Color.DARKGRAY);
                } else {
                    if (floorImage != null) gc.drawImage(floorImage, px, py, TILE_SIZE, TILE_SIZE);
                    else drawPlaceholder(px, py, Color.LIGHTGRAY);
                }

                if(cell.isDesk()) {
                    drawPlaceholder(px, py, Color.BROWN);
                } else if (cell.isCoffee()) {
                    drawPlaceholder(px, py, Color.AQUAMARINE);
                }
            }
        }

        // Rysowanie Agentów z uwzględnieniem kierunku i animacji nóg
        for (Agent agent : sim.getAgents()) {

            double px = agent.getVisualX() * TILE_SIZE;
            double py = agent.getVisualY() * TILE_SIZE;

            boolean czyPijeKawe = false;

            if (agent instanceof Boss) {
                Cell currentCell = board.getCell(agent.getX(), agent.getY());
                if (currentCell != null && "coffee".equalsIgnoreCase(currentCell.getType())) {
                    czyPijeKawe = true;
                }
            } else if (agent instanceof Worker) {
                Worker worker = (Worker) agent;
                String stanText = worker.getCurrentStateName();
                if (stanText != null && (stanText.equalsIgnoreCase("CoffeeState") || stanText.contains("Coffee") || stanText.contains("Resting"))) {
                    czyPijeKawe = true;
                }
            }

            // Wybór bazy obrazka
            Image imgToDraw = juniorImg;
            if (agent instanceof Boss) {
                imgToDraw = (czyPijeKawe && bossCoffeeImg != null) ? bossCoffeeImg : bossImg;
            } else if (agent instanceof Senior) {
                imgToDraw = (czyPijeKawe && seniorCoffeeImg != null) ? seniorCoffeeImg : seniorImg;
            } else if (agent instanceof Junior) {
                imgToDraw = (czyPijeKawe && juniorCoffeeImg != null) ? juniorCoffeeImg : juniorImg;
            }

            if (imgToDraw != null) {

                // Kolumna zależy od aktualnej klatki ruchu (0, 1, 2 lub 3)
                double sx = agent.getAnimationFrame() * SPRITE_WIDTH;

                // Wiersz zależy od kierunku, w którym idzie agent
                double sy = 0;
                switch (agent.getDirection()) {
                    case "DOWN" -> sy = 0 * SPRITE_HEIGHT;
                    case "LEFT" -> sy = 1 * SPRITE_HEIGHT;
                    case "RIGHT" -> sy = 2 * SPRITE_HEIGHT;
                    case "UP" -> sy = 3 * SPRITE_HEIGHT;
                }

                // JavaFX drawImage(img, sx, sy, sw, sh, dx, dy, dw, dh)
                // Wycina kawałek grafiki (sx, sy, sw, sh) i rysuje na ekranie (px, py, dw, dh)
                /*gc.drawImage(imgToDraw,
                        sx, sy, SPRITE_WIDTH, SPRITE_HEIGHT, // Skąd wyciąć z pliku graficznego
                        px, py, TILE_SIZE, TILE_SIZE         // Gdzie narysować na płótnie
                );

                 */

                if (imgToDraw != null) {
                    // Rysuje cały obrazek
                    gc.drawImage(imgToDraw, px, py, TILE_SIZE, TILE_SIZE);
                }

            } else {
                // Awaryjny placeholder, jeśli pliki graficzne nie są załadowane
                gc.setFill(agent instanceof Boss ? Color.RED : Color.BLUE);
                gc.fillOval(px + 32, py + 32, 64, 64);
            }

            // RYSOWANIE IMIENIA NAD POSTACIĄ
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(agent.getName(), px + (TILE_SIZE / 2), py + 20);

            if (agent instanceof Worker) {
                Worker worker = (Worker) agent;

                if (worker.hasTask()) {
                    int pozostaleTury = worker.getTurnsLeft();

                    // Parametry paska
                    double barWidth = 80;                           // Szerokość paska w pikselach
                    double barHeight = 10;                          // Wysokość paska
                    double barX = px + (TILE_SIZE - barWidth) / 2;  // Wyśrodkowanie paska nad ludzikiem
                    double barY = py - 15;                          // 15 pikseli NAD głową ludzika

                    // Rysujemy tło paska
                    gc.setFill(Color.web("#333333"));
                    gc.fillRect(barX, barY, barWidth, barHeight);

                    // Obliczamy, ile paska ma być zapełnione
                    int maxTury = worker.computeTaskTime(); // Maksymalny czas trwania zadania
                    if (maxTury <= 0) maxTury = 1;          // Ochrona przed dzieleniem przez 0

                    // Proporcja paska (pozostały czas)
                    double progress = (double) pozostaleTury / maxTury;
                    if (progress > 1.0) progress = 1.0;
                    if (progress < 0.0) progress = 0.0;

                    // Rysujemy wypełnienie paska
                    gc.setFill(Color.web("#00bcd4")); // Jasnoniebieski / Cyjan
                    gc.fillRect(barX, barY, barWidth * progress, barHeight);

                    // Rysujemy czarną ramkę wokół paska
                    gc.setStroke(Color.BLACK);
                    gc.setLineWidth(1.5);
                    gc.strokeRect(barX, barY, barWidth, barHeight);
                }

                // Mały wskaźnik kierunku (kropka), żeby na kółkach też było widać, gdzie idą
                gc.setFill(Color.WHITE);
                switch (agent.getDirection()) {
                    case "UP"    -> gc.fillOval(px + 56, py + 16, 16, 16);
                    case "DOWN"  -> gc.fillOval(px + 56, py + 96, 16, 16);
                    case "LEFT"  -> gc.fillOval(px + 16, py + 56, 16, 16);
                    case "RIGHT" -> gc.fillOval(px + 96, py + 56, 16, 16);
                }
            }
        }
    }

    private void drawPlaceholder(int x, int y, Color color) {
        gc.setFill(color);
        gc.fillRect(x, y, TILE_SIZE, TILE_SIZE);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1);
        gc.strokeRect(x, y, TILE_SIZE, TILE_SIZE);
    }

    public Canvas getCanvas() {
        return canvas;
    }
}