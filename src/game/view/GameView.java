package game.view;

import game.model.Cell;
import game.model.GameBoard;
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

    // Obrazki tekstur - (których jeszcze nie ma)(ale będą)
    private Image floorImage;
    private Image wallImage;
    private Image workerImage;

    public GameView(GameBoard board) {
        this.board = board;

        // Dynamicznie obliczamy rozmiar okna na podstawie wymiarów planszy,
        //TEORETYCZNIE wymiar jest już ustalony 11x11, ale w razie gdybyśmy chcieli jakoś zmienić
        //To wystarczy zmienić w klasie GameBoard -- a tutaj się samo zmieni
        int width = board.getWidth() * TILE_SIZE;
        int height = board.getHeight() * TILE_SIZE;

        this.canvas = new Canvas(width, height);
        this.gc = canvas.getGraphicsContext2D();

        loadImages();               // Ładowanie tekstur
    }

    private void loadImages() {         //Zabezpieczenie w chcwili zczytywania z pliku, żeby nic się nie wsyspało
        try {
            //Tutaj doda się te obrazki ****
            // floorImage = new Image(getClass().getResourceAsStream("/images/floor.png"));
            // wallImage = new Image(getClass().getResourceAsStream("/images/wall.png"));
            // workerImage = new Image(getClass().getResourceAsStream("/images/worker.png"));
        } catch (Exception e) {
            System.out.println("Nie udało się załadować obrazków, używam kolorów zastępczych.");
        }
    }

    /**
         ****** GŁÓWNA METODA *******
     * Rysuje cały stan gry
     * Będzie wywoływana w pętli gry przy każdym ruchu/odświeżeniu.
     */
    public void render() {
        // 1. Czyszczenie ekranu przed każdym rysowaniem
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // 2. Rysowanie planszy (kafelek po kafelku)
        for (int y = 0; y < board.getHeight(); y++) {
            for (int x = 0; x < board.getWidth(); x++) {
                Cell cell = board.getCell(x, y);

                // Obliczamy pozycję rysowania w pikselach
                int px = x * TILE_SIZE;
                int py = y * TILE_SIZE;

                // Sprawdzamy typ kafelka i rysujemy obrazek lub kolor zastępczy
                if (cell.isWall()) {
                    if (wallImage != null) gc.drawImage(wallImage, px, py, TILE_SIZE, TILE_SIZE);
                    else drawPlaceholder(px, py, Color.DARKGRAY); // ściana
                } else {
                    if (floorImage != null) gc.drawImage(floorImage, px, py, TILE_SIZE, TILE_SIZE);
                    else drawPlaceholder(px, py, Color.LIGHTGRAY); // podłoga
                }
            }
        }

        // krok 3. Rysowanie agentów (pracowników) na planszy
        /*
        for (Agent agent : board.getAgents()) {
            int px = agent.getX() * TILE_SIZE;
            int py = agent.getY() * TILE_SIZE;

            if (workerImage != null) {
                gc.drawImage(workerImage, px, py, TILE_SIZE, TILE_SIZE);
            } else {
                gc.setFill(Color.BLUE);
                gc.fillOval(px + 10, py + 10, TILE_SIZE - 20, TILE_SIZE - 20); // Kropka zamiast pracownika
            }
        }
        */
    }

    // Pomocnicza metoda do rysowania kwadratów, (zanim będą wgrane grafiki png)
    private void drawPlaceholder(int x, int y, Color color) {
        gc.setFill(color);
        gc.fillRect(x, y, TILE_SIZE, TILE_SIZE);
        gc.setStroke(Color.WHITE); // siatka podziału biura
        gc.setLineWidth(1);
        gc.strokeRect(x, y, TILE_SIZE, TILE_SIZE);
    }

    // GETTER: jest potrzebny dla klasy MainApp, żeby pobrać płótno i włożyć je do okna
    public Canvas getCanvas() {
        return canvas;
    }

}


// Ad.3
//Żeby odkomentować krok 3. musi być lista agentów w GameBoard lub Simulation.