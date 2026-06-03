package game.view;

import game.model.Cell;
import game.model.GameBoard;
import game.model.Agent; // Czy ścieżka jest poprawna? (np. game.model.Agent lub game.core.Agent)
import game.model.Boss;  // Trzeba zobaczyc na pakiet czy się wszytsko zgadza
import game.model.Senior;
import game.model.Junior;
import game.model.Worker;   //Import klasy, która ma metodę stanu
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

    // Obrazki tekstur - (których jeszcze nie ma)(ale będą) Edit: już są
    // Obrazki tekstur otoczenia (zadeklarowane, żeby nie było błędów w render())
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

        // 3. Rysowanie Agentów (z uwzględnieniem ich ról oraz stanów)
        for (int y = 0; y < board.getHeight(); y++) {
            for (int x = 0; x < board.getWidth(); x++) {
                Cell cell = board.getCell(x, y);

                // Jeśli komórka nie jest pusta, oznacza to, że stoi na niej Agent
                if (cell != null && !cell.isEmpty()) {
                    Agent agent = cell.getAgent();

                    double px = x * TILE_SIZE;
                    double py = y * TILE_SIZE;

                    // Domyślnie zakładamy, że agent nie pije kawy
                    boolean czyPijeKawe = false;

                    // Ponieważ metoda stanu jest w klasie Worker, musimy sprawdzić, czy nasz agent jest Workerem
                    if (agent instanceof Worker) {
                        Worker worker = (Worker) agent;
                        String stanText = worker.getCurrentStateName();

                        // Tutaj wpisz dokładną nazwę klasy stanu, która odpowiada za picie kawy w Waszym projekcie!
                        // Na przykład: "CoffeeState" lub "DrinkingCoffeeState"
                        if (stanText != null && stanText.equalsIgnoreCase("CoffeeState")) {
                            czyPijeKawe = true;
                        }
                    }

                    // Wybieramy odpowiedni obrazek do narysowania
                    Image imgToDraw = juniorImg; // Domyślny bezpieczny wybór (Junior)

                    if (agent instanceof Boss) {
                        // Jeśli szef pije kawę i mamy obrazek, wybierz wersję kawową, w innym wypadku zwykłą
                        imgToDraw = (czyPijeKawe && bossCoffeeImg != null) ? bossCoffeeImg : bossImg;
                    } else if (agent instanceof Senior) {
                        imgToDraw = (czyPijeKawe && seniorCoffeeImg != null) ? seniorCoffeeImg : seniorImg;
                    } else if (agent instanceof Junior) {
                        imgToDraw = (czyPijeKawe && juniorCoffeeImg != null) ? juniorCoffeeImg : juniorImg;
                    }

                    // Rysujemy dopasowany obrazek na płótnie JavaFX
                    if (imgToDraw != null) {
                        gc.drawImage(imgToDraw, px, py, TILE_SIZE, TILE_SIZE);
                    } else {
                        // Awaryjne rysowanie kółka (gdyby zapomniano wgrać pliku PNG)
                        gc.setFill(agent instanceof Boss ? Color.RED : Color.BLUE);
                        gc.fillOval(px + 32, py + 32, 64, 64);
                    }
                }
            }
        }
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