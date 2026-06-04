package game.view;

import game.model.Worker;
import game.model.Cell;
import game.model.GameBoard;
import game.model.Agent;
import game.model.Boss;
import game.model.Senior;
import game.model.Junior;
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

    private static final int TILE_SIZE = 128;

    private Image floorImage;
    private Image bossOfficeImage;
    private Image outsideImage;
    private Image wallImage;

    private Image juniorImg;
    private Image seniorImg;
    private Image bossImg;

    private Image juniorCoffeeImg;
    private Image seniorCoffeeImg;
    private Image bossCoffeeImg;

    private Image juniorCryingImg; // Zmienna na płaczącego Juniora

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

            // Ładowanie obrazka płaczącego Juniora
            this.juniorCryingImg = new Image(getClass().getResourceAsStream("/images/junior_crying.png"));

            floorImage = new Image(getClass().getResourceAsStream("/images/floor.png"));
            bossOfficeImage = new Image(getClass().getResourceAsStream("/images/boss_office.png"));
            outsideImage = new Image(getClass().getResourceAsStream("/images/outside.png"));
            wallImage = new Image(getClass().getResourceAsStream("/images/wall.png"));

        } catch (Exception e) {
            System.out.println("Nie udało się załadować obrazków, używam kolorów zastępczych.");
        }
    }

    public void render(game.core.Simulation sim) {
        // 1. Czyszczenie ekranu przed każdym rysowaniem
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

// 2. Rysowanie planszy
        for (int y = 0; y < board.getHeight(); y++) {
            for (int x = 0; x < board.getWidth(); x++) {
                Cell cell = board.getCell(x, y);

                int px = x * TILE_SIZE;
                int py = y * TILE_SIZE;

                // Pobieramy typ tekstowy z komórki, który ustawia GameBoard
                String cellType = cell.getType();

                switch (cellType) {
                    case "wall":
                        if (wallImage != null) gc.drawImage(wallImage, px, py, TILE_SIZE, TILE_SIZE);
                        else drawPlaceholder(px, py, Color.DARKGRAY);
                        break;

                    case "boss_office":
                        if (bossOfficeImage != null) gc.drawImage(bossOfficeImage, px, py, TILE_SIZE, TILE_SIZE);
                        else drawPlaceholder(px, py, Color.LIGHTGOLDENRODYELLOW);
                        break;

                    case "outside":
                        if (outsideImage != null) gc.drawImage(outsideImage, px, py, TILE_SIZE, TILE_SIZE);
                        else drawPlaceholder(px, py, Color.GREEN);
                        break;

                    default:
                        // Domyślnie dla zwykłej podłogi ("floor"), ale też "desk" i "coffee",
                        // dopóki nie dodasz dla nich osobnych rysunków mebli.
                        if (floorImage != null) gc.drawImage(floorImage, px, py, TILE_SIZE, TILE_SIZE);
                        else drawPlaceholder(px, py, Color.LIGHTGRAY);
                        break;
                }
            }
        }

        // 3. Rysowanie Agentów
        for (Agent agent : sim.getAgents()) {

            double px = agent.getVisualX() * TILE_SIZE;
            double py = agent.getVisualY() * TILE_SIZE;

            boolean czyPijeKawe = false;
            Cell currentCell = board.getCell(agent.getX(), agent.getY());
            if (currentCell != null && "coffee".equalsIgnoreCase(currentCell.getType())) {
                czyPijeKawe = true;
            }

            // --- B. WYBÓR OBRAZKA DO NARYSOWANIA ---
            Image imgToDraw = juniorImg;

            if (agent instanceof Boss) {
                imgToDraw = (czyPijeKawe && bossCoffeeImg != null) ? bossCoffeeImg : bossImg;
            } else if (agent instanceof Senior) {
                imgToDraw = (czyPijeKawe && seniorCoffeeImg != null) ? seniorCoffeeImg : seniorImg;
            } else if (agent instanceof Junior) {
                Junior junior = (Junior) agent;
                // Sprawdzamy czy Junior jest w stanie płaczu
                if ("CryingState".equals(junior.getCurrentStateName()) && juniorCryingImg != null) {
                    imgToDraw = juniorCryingImg;
                } else {
                    imgToDraw = (czyPijeKawe && juniorCoffeeImg != null) ? juniorCoffeeImg : juniorImg;
                }
            }

            // --- C. RYSOWANIE POSTACI (Już prawidłowo wyciągnięte z Juniora!) ---
            if (imgToDraw != null) {
                gc.drawImage(imgToDraw, px, py, TILE_SIZE, TILE_SIZE);
            } else {
                gc.setFill(agent instanceof Boss ? Color.RED : Color.BLUE);
                gc.fillOval(px + 32, py + 32, 64, 64);
            }

            // --- D. RYSOWANIE IMIENIA NAD POSTACIĄ ---
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(agent.getName(), px + (TILE_SIZE / 2), py + 20);

            // --- E. RYSUNEK PASKA TASKA ---
            if (agent instanceof Worker) {
                Worker worker = (Worker) agent;

                if (worker.hasTask()) {
                    int pozostaleTury = worker.getTurnsLeft();
                    String pasek = "";
                    for (int i = 0; i < pozostaleTury; i++) {
                        pasek += "-";
                    }
                    gc.setFill(Color.BLUE);
                    gc.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 24));
                    gc.fillText(pasek, px + (TILE_SIZE / 2), py + 10);
                }
            }
        } // <--- Koniec pętli for (agent)
    } // <--- Koniec metody render

    // Pomocnicza metoda do rysowania kwadratów (Bezpiecznie na zewnątrz)
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