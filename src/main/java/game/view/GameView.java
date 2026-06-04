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

    //Plansza floor
    private Image floorImage;
    private Image bossOfficeImage;
    private Image outsideImage;
    private Image wallImage;

    //Plansza Object
    private Image deskImage;
    private Image bossDeskImage;
    private Image coffeeImage;
    private Image wallObjImage; // Dla obiektów ścian (6 / "wall")
    private Image wallRightObjImage;
    private Image wallBackObjImage;
    private Image wallCornerObjImage; //to jest ten podstawowy
    private Image wallSRCornerImage;
    private Image wallNRCornerImage;
    private Image wallNLCornerImage;
    private Image wallLeftObjImage;

    //Agenci
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
        // Postacie
        juniorImg = safeLoad("/images/junior.png");
        seniorImg = safeLoad("/images/senior.png");
        bossImg = safeLoad("/images/boss.png");
        juniorCoffeeImg = safeLoad("/images/junior_coffee.png");
        seniorCoffeeImg = safeLoad("/images/senior_coffee.png");
        bossCoffeeImg = safeLoad("/images/boss_coffee.png");
        juniorCryingImg = safeLoad("/images/junior_crying.png");

        // Podłogi
        floorImage = safeLoad("/images/floor.png");
        bossOfficeImage = safeLoad("/images/boss_office_floor.png");
        outsideImage = safeLoad("/images/grass.png");
        wallImage = safeLoad("/images/wallNotWalkable.png");

        // Obiekty / Meble
        deskImage = safeLoad("/images/worker_deskObj.png");
        bossDeskImage = safeLoad("/images/boss_deskObj.png");
        coffeeImage = safeLoad("/images/coffeeObj.png");
        wallObjImage = safeLoad("/images/wallObj.png"); // Teraz nic jej nie zablokuje!
        this.wallRightObjImage = safeLoad("/images/wallRightObj.png");
        this.wallBackObjImage = safeLoad("/images/wallBackObj.png");
        this.wallCornerObjImage = safeLoad("/images/wallCornerObj.png");
        this.wallSRCornerImage = safeLoad("/images/wallSRCorner.png");
        this.wallNRCornerImage = safeLoad("/images/wallNRCorner.png");
        this.wallNLCornerImage = safeLoad("/images/wallNLCorner.png");
        this.wallLeftObjImage = safeLoad("/images/wallLeftObj.png");
    }

    // Magiczna metoda pomocnicza, która sprawdzi w konsoli KAŻDY plik z osobna
    private Image safeLoad(String path) {
        try {
            java.io.InputStream stream = getClass().getResourceAsStream(path);
            if (stream == null) {
                System.out.println("❌ Nie znaleziono pliku na dysku: " + path);
                return null;
            }
            return new Image(stream);
        } catch (Exception e) {
            System.out.println("❌ Błąd podczas ładowania pliku: " + path);
            return null;
        }
    }

    public void render(game.core.Simulation sim) {
        // 1. Czyszczenie ekranu przed każdym rysowaniem
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

// 2. Rysowanie planszy (AUTOMATYCZNE DWUWARSTWOWE)
        for (int y = 0; y < board.getHeight(); y++) {
            for (int x = 0; x < board.getWidth(); x++) {
                Cell cell = board.getCell(x, y);

                int px = x * TILE_SIZE;
                int py = y * TILE_SIZE;

                // --- WARSTWA 1: NAJPIERW ZAWSZE RYSUJEMY PODŁOGĘ (z floorLayout) ---
                int[][] floorMap = board.getFloorMap();
                int floorType = floorMap[y][x];

                switch (floorType) {
                    case 1: // Trawa / Outside
                        if (outsideImage != null) gc.drawImage(outsideImage, px, py, TILE_SIZE, TILE_SIZE);
                        else drawPlaceholder(px, py, Color.GREEN);
                        break;
                    case 2: // Gabinet Szefa
                        if (bossOfficeImage != null) gc.drawImage(bossOfficeImage, px, py, TILE_SIZE, TILE_SIZE);
                        else drawPlaceholder(px, py, Color.LIGHTGOLDENRODYELLOW);
                        break;
                    case 3: // Górna ściana (niechodzalna)
                        if (wallImage != null) gc.drawImage(wallImage, px, py, TILE_SIZE, TILE_SIZE);
                        else drawPlaceholder(px, py, Color.DARKGRAY);
                        break;
                    case 0: // Zwykła podłoga biura
                    default:
                        if (floorImage != null) gc.drawImage(floorImage, px, py, TILE_SIZE, TILE_SIZE);
                        else drawPlaceholder(px, py, Color.LIGHTGRAY);
                        break;
                }

                // --- WARSTWA 2: NA TO NAKŁADAMY MEBEL / OBIEKT (jeśli istnieje w tej komórce) ---
                String cellType = cell.getType();

                switch (cellType) {
                    case "desk":
                        if (deskImage != null) gc.drawImage(deskImage, px, py, TILE_SIZE, TILE_SIZE);
                        else drawPlaceholder(px, py, Color.LIGHTBLUE);
                        break;

                    case "boss_desk":
                        if (bossDeskImage != null) gc.drawImage(bossDeskImage, px, py, TILE_SIZE, TILE_SIZE);
                        else drawPlaceholder(px, py, Color.GOLD);
                        break;

                    case "coffee":
                        if (coffeeImage != null) gc.drawImage(coffeeImage, px, py, TILE_SIZE, TILE_SIZE);
                        else drawPlaceholder(px, py, Color.ORANGE);
                        break;

                    case "wall": // Dolna ściana
                        if (wallObjImage != null) gc.drawImage(wallObjImage, px, py, TILE_SIZE, TILE_SIZE);
                        else drawPlaceholder(px, py, Color.GRAY);
                        break;

                    case "wall_right": // Prawa ściana odcinająca trawę
                        if (wallRightObjImage != null) gc.drawImage(wallRightObjImage, px, py, TILE_SIZE, TILE_SIZE);
                        else drawPlaceholder(px, py, Color.GRAY);
                        break;

                    case "wall_back": // Tylna ściana
                        if (wallBackObjImage != null) gc.drawImage(wallBackObjImage, px, py, TILE_SIZE, TILE_SIZE);
                        else drawPlaceholder(px, py, Color.GRAY);
                        break;

                    case "wall_corner": // Narożna ściana
                        if (wallCornerObjImage != null) gc.drawImage(wallCornerObjImage, px, py, TILE_SIZE, TILE_SIZE);
                        else drawPlaceholder(px, py, Color.GRAY);
                        break;

                    case "wall_sr_corner":
                        if (wallSRCornerImage != null) gc.drawImage(wallSRCornerImage, px, py, TILE_SIZE, TILE_SIZE);
                        else drawPlaceholder(px, py, Color.GRAY);
                        break;

                    case "wall_nr_corner":
                        if (wallNRCornerImage != null) gc.drawImage(wallNRCornerImage, px, py, TILE_SIZE, TILE_SIZE);
                        else drawPlaceholder(px, py, Color.GRAY);
                        break;

                    case "wall_nl_corner":
                        if (wallNLCornerImage != null) gc.drawImage(wallNLCornerImage, px, py, TILE_SIZE, TILE_SIZE);
                        else drawPlaceholder(px, py, Color.GRAY);
                        break;

                    // --- OTO TA JEDNA DODANA LEWA ŚCIANA ---
                    case "wall_left":
                        if (wallLeftObjImage != null) gc.drawImage(wallLeftObjImage, px, py, TILE_SIZE, TILE_SIZE);
                        else drawPlaceholder(px, py, Color.GRAY);
                        break;

                    default:
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