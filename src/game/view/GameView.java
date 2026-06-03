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

public class GameView {
    /*
    Widok -- Ta klasa będzie pobierała dane z planszy i renderowała kafelki,
    agentów i inne elementy (meble) -- na płótnie zimportowanej z biblioteki JavaFX
     */
    private final GameBoard board;
    private final Canvas canvas;
    private final GraphicsContext gc;

    private static final int TILE_SIZE = 128;        //Rozmiar pojedyńczego kafelka w pixelach

    // Obrazki tekstur otoczenia
    private Image floorImage;
    private Image wallImage;

    // Obrazki tekstur agentów (podstawowe)
    private Image juniorImg;
    private Image seniorImg;
    private Image bossImg;

    // Obrazki tekstur agentów przy ekspresie (stoisku z kawą)
    private Image juniorCoffeeImg;
    private Image seniorCoffeeImg;
    private Image bossCoffeeImg;

    public GameView(GameBoard board) {
        this.board = board;

        int width = board.getWidth() * TILE_SIZE;
        int height = board.getHeight() * TILE_SIZE;

        this.canvas = new Canvas(width, height);
        this.gc = canvas.getGraphicsContext2D();

        loadImages();               // Ładowanie tekstur
    }

    private void loadImages() {
        try {
            // Ładowanie podstawowych postaci
            this.juniorImg = new Image(getClass().getResourceAsStream("/images/junior.png"));
            this.seniorImg = new Image(getClass().getResourceAsStream("/images/senior.png"));
            this.bossImg = new Image(getClass().getResourceAsStream("/images/boss.png"));

            // Ładowanie postaci z kawą
            this.juniorCoffeeImg = new Image(getClass().getResourceAsStream("/images/junior_coffee.png"));
            this.seniorCoffeeImg = new Image(getClass().getResourceAsStream("/images/senior_coffee.png"));
            this.bossCoffeeImg = new Image(getClass().getResourceAsStream("/images/boss_coffee.png"));

            // Opcjonalnie: Jeśli będziecie mieć już grafiki ścian i podłóg, odkomentujcie poniższe:
            // this.floorImage = new Image(getClass().getResourceAsStream("/images/floor.png"));
            // this.wallImage = new Image(getClass().getResourceAsStream("/images/wall.png"));
        } catch (Exception e) {
            System.out.println("Nie udało się załadować obrazków, używam kolorów zastępczych.");
        }
    }

    /**
     ****** GŁÓWNA METODA *******
     * Rysuje cały stan gry z płynnymi animacjami agentów.
     */
    public void render(game.core.Simulation sim) {
        // 1. Czyszczenie ekranu przed każdym rysowaniem
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // 2. Rysowanie planszy (kafelek po kafelku) - tło zostaje na kafelkach
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
            }
        }

        // =========================================================================
        // POPRAWIONY KROK 3: Rysowanie Agentów prosto z płaskiej listy z Simulation
        // =========================================================================
        for (Agent agent : sim.getAgents()) {

            // KLUCZ DO PŁYNNOŚCI: Pozycja na ekranie bazuje na zmiennych visualX/Y (double)
            double px = agent.getVisualX() * TILE_SIZE;
            double py = agent.getVisualY() * TILE_SIZE;

            // Domyślnie zakładamy, że agent nie pije kawy
            boolean czyPijeKawe = false;

            // 1. SPRAWDZENIE DLA SZEFA (na podstawie jego pozycji logicznej na kafelku)
            if (agent instanceof Boss) {
                Cell currentCell = board.getCell(agent.getX(), agent.getY());
                if (currentCell != null && "coffee".equalsIgnoreCase(currentCell.getType())) {
                    czyPijeKawe = true;
                }
            }
            // 2. SPRAWDZENIE DLA WORKERÓW (na podstawie maszyn stanów)
            else if (agent instanceof Worker) {
                Worker worker = (Worker) agent;
                String stanText = worker.getCurrentStateName();

                if (stanText != null && (stanText.equalsIgnoreCase("CoffeeState") || stanText.contains("Coffee"))) {
                    czyPijeKawe = true;
                }
            }

            // Wybieramy odpowiedni obrazek do narysowania
            Image imgToDraw = juniorImg;

            if (agent instanceof Boss) {
                imgToDraw = (czyPijeKawe && bossCoffeeImg != null) ? bossCoffeeImg : bossImg;
            } else if (agent instanceof Senior) {
                imgToDraw = (czyPijeKawe && seniorCoffeeImg != null) ? seniorCoffeeImg : seniorImg;
            } else if (agent instanceof Junior) {
                imgToDraw = (czyPijeKawe && juniorCoffeeImg != null) ? juniorCoffeeImg : juniorImg;
            }

            // Rysujemy dopasowany obrazek na PŁYNNEJ pozycji (px, py)
            if (imgToDraw != null) {
                gc.drawImage(imgToDraw, px, py, TILE_SIZE, TILE_SIZE);
            } else {
                // Awaryjne rysowanie kółka (współrzędne też zmienione na px, py!)
                gc.setFill(agent instanceof Boss ? Color.RED : Color.BLUE);
                gc.fillOval(px + 32, py + 32, 64, 64);
            }
        }
    }

    // Pomocnicza metoda do rysowania kwadratów
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