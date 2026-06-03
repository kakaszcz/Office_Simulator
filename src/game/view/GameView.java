package game.view;

import game.states.WorkingState;
import game.model.Worker;
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

            // GDY BEDZIEMY JUZ MIEC GRAFIKI SCIAN I PODLOGI TO ODKOMENTOWAC PONIZEJ
            //this.floorImage = new Image(getClass().getResourceAsStream("/images/floor.png"));
            //this.wallImage = new Image(getClass().getResourceAsStream("/images/wall.png"));
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
        // KROK 3: Rysowanie Agentów z listy z Simulation
        // =========================================================================
        for (Agent agent : sim.getAgents()) {

            // Pozycja na ekranie bazuje na zmiennych visualX/Y (płynne animacje!)
            double px = agent.getVisualX() * TILE_SIZE;
            double py = agent.getVisualY() * TILE_SIZE;

            // --- A. SPRAWDZANIE KAWY (Uniwersalne dla wszystkich) ---
            boolean czyPijeKawe = false;
            Cell currentCell = board.getCell(agent.getX(), agent.getY());
            if (currentCell != null && "coffee".equalsIgnoreCase(currentCell.getType())) {
                czyPijeKawe = true;
            }

            // --- B. WYBÓR OBRAZKA DO NARYSOWANIA ---
            Image imgToDraw = juniorImg; // Domyślny obrazek juniora

            if (agent instanceof Boss) {
                imgToDraw = (czyPijeKawe && bossCoffeeImg != null) ? bossCoffeeImg : bossImg;
            } else if (agent instanceof Senior) {
                imgToDraw = (czyPijeKawe && seniorCoffeeImg != null) ? seniorCoffeeImg : seniorImg;
            } else if (agent instanceof Junior) {
                imgToDraw = (czyPijeKawe && juniorCoffeeImg != null) ? juniorCoffeeImg : juniorImg;
            }

            // --- C. RYSOWANIE POSTACI ---
            if (imgToDraw != null) {
                gc.drawImage(imgToDraw, px, py, TILE_SIZE, TILE_SIZE);
            } else {
                // Awaryjne rysowanie kółka (gdyby brakowalo grafiki)
                gc.setFill(agent instanceof Boss ? Color.RED : Color.BLUE); //dla bossa czerwony dla pozostalych niebieski
                gc.fillOval(px + 32, py + 32, 64, 64);
            }

            // --- D. RYSOWANIE IMIENIA NAD POSTACIĄ ---
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(agent.getName(), px + (TILE_SIZE / 2), py + 20);

            // =========================================================
            // ==== RYSUNEK PASKA TASKA =============
            // =========================================================
            if (agent instanceof Worker) {
                Worker worker = (Worker) agent;

                // Sprawdzamy, czy pracownik w ogóle ma przypisane zadanie
                if (worker.hasTask()) {

                    // POBRANIE LICZBY TUR
                    int pozostaleTury = worker.getTurnsLeft();

                    // 1. Budujemy pasek z minusów w zależności od liczby tur
                    String pasek = "";
                    for (int i = 0; i < pozostaleTury; i++) {
                        pasek += "-"; // Doklejamy jeden minus za każdą turę
                    }

                    // Styl paska
                    gc.setFill(Color.BLUE);
                    gc.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 24));

                    // 3. Rysujemy minusy pod imieniem (py + 10)
                    gc.fillText(pasek, px + (TILE_SIZE / 2), py + 10);
                }
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