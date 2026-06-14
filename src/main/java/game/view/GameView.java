package game.view;

import game.agents.Worker;
import game.model.Cell;
import game.model.GameBoard;
import game.agents.Agent;
import game.agents.Boss;
import game.agents.Senior;
import game.agents.Junior;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * Klasa silnika graficznego (GameView) odpowiedzialna za renderowanie środowiska symulacji.
 * Stanowi element warstwy widoku w architekturze MVC. Wykorzystuje mechanizm {@link Canvas}
 * z biblioteki JavaFX do płynnego rysowania siatki biura, obiektów statycznych, mebli
 * oraz dynamicznych awatarów agentów w zależności od ich bieżącego stanu behawioralnego.
 */
public class GameView {

    /** Logiczna plansza gry dostarczająca dane o strukturze kafelków. */
    private final GameBoard board;
    /** Komponent JavaFX, na którym rysowana jest grafika. */
    private final Canvas canvas;
    /** Kontekst graficzny 2D używany do bezpośrednich operacji rysowania. */
    private final GraphicsContext gc;

    /** Stały rozmiar pojedynczego kafelka (Grid Tile) wyrażony w pikselach. */
    private static final int TILE_SIZE = 128;

    // --- ZASOBY GRAFICZNE: WARSTWA PODŁOGI (Floor Layer) ---
    private Image floorImage;
    private Image bossOfficeImage;
    private Image outsideImage;
    private Image wallImage;

    // --- ZASOBY GRAFICZNE: WARSTWA OBIEKTÓW I MEBLI (Object Layer) ---
    private Image deskImage;
    private Image bossDeskImage;
    private Image coffeeImage;
    private Image wallObjImage;
    private Image wallRightObjImage;
    private Image wallBackObjImage;
    private Image wallCornerObjImage;
    private Image wallSRCornerImage;
    private Image wallNRCornerImage;
    private Image wallNLCornerImage;
    private Image wallLeftObjImage;
    private Image juniorWorkingImg;
    private Image seniorWorkingImg;

    // --- ZASOBY GRAFICZNE: AWATARY AGENTÓW (Agent Sprites) ---
    private Image juniorImg;
    private Image seniorImg;
    private Image bossImg;

    private Image juniorCoffeeImg;
    private Image seniorCoffeeImg;
    private Image bossCoffeeImg;
    private Image juniorSmokingImg;
    private Image juniorTalkingImg;
    private Image juniorSuccessImg;
    private Image seniorSmokingImg;
    private Image seniorTalkingImg;
    private Image seniorMadImg;
    private Image fatalErrorDeskImg;
    private Image seniorSuccessImg;
    private Image seniorWorkingImgAlt;
    private Image seniorMadImgAlt;
    private Image bossMadImg;
    private Image bossTalkingImg;
    private Image bossZalamanyImg;
    private Image juniorCryingImg;

    /**
     * Inicjalizuje widok gry, oblicza wymiary okna na podstawie rozmiaru planszy
     * oraz wywołuje procedurę bezpiecznego ładowania zasobów graficznych z dysku.
     *
     * @param board Logiczna plansza gry definiująca siatkę biura.
     */
    public GameView(GameBoard board) {
        this.board = board;
        int width = board.getWidth() * TILE_SIZE;
        int height = board.getHeight() * TILE_SIZE;

        this.canvas = new Canvas(width, height);
        this.gc = canvas.getGraphicsContext2D();

        loadImages();
    }

    /**
     * Ładuje wszystkie pliki graficzne formatu PNG z zasobów aplikacji (resources).
     * Wykorzystuje metodę zabezpieczającą przed awarią w przypadku braku plików na dysku.
     */
    private void loadImages() {
        juniorImg = safeLoad("/images/junior.png");
        seniorImg = safeLoad("/images/senior.png");
        bossImg = safeLoad("/images/boss.png");
        juniorCoffeeImg = safeLoad("/images/junior_coffee.png");
        seniorCoffeeImg = safeLoad("/images/senior_coffee.png");
        bossCoffeeImg = safeLoad("/images/boss_coffee.png");

        juniorCryingImg = safeLoad("/images/juniorCrying.png");
        this.juniorSmokingImg = safeLoad("/images/junior_smoking.png");
        this.juniorTalkingImg = safeLoad("/images/junior_talking.png");
        this.juniorSuccessImg = safeLoad("/images/juniorSuccess.png");

        this.seniorSmokingImg = safeLoad("/images/senior_smoking.png");
        this.seniorTalkingImg = safeLoad("/images/senior_talking.png");
        this.seniorMadImg = safeLoad("/images/seniorMad.png");

        this.juniorWorkingImg = safeLoad("/images/juniorWorkingImg.png");
        this.fatalErrorDeskImg = safeLoad("/images/fatalErrorDesk.png");
        this.seniorSuccessImg = safeLoad("/images/senior_success.png");
        this.seniorWorkingImgAlt = safeLoad("/images/seniorWorkingImg.png");
        this.seniorMadImgAlt = safeLoad("/images/senior_mad.png");
        this.bossMadImg = safeLoad("/images/bossMad.png");
        this.bossTalkingImg = safeLoad("/images/boss_talking.png");
        this.bossZalamanyImg = safeLoad("/images/boss_zalamany.png");

        floorImage = safeLoad("/images/floor.png");
        bossOfficeImage = safeLoad("/images/boss_office_floor.png");
        outsideImage = safeLoad("/images/grass.png");
        wallImage = safeLoad("/images/wallNotWalkable.png");

        deskImage = safeLoad("/images/worker_deskObj.png");
        bossDeskImage = safeLoad("/images/boss_deskObj.png");
        coffeeImage = safeLoad("/images/coffeeObj.png");
        wallObjImage = safeLoad("/images/wallObj.png");
        this.wallRightObjImage = safeLoad("/images/wallRightObj.png");
        this.wallBackObjImage = safeLoad("/images/wallBackObj.png");
        this.wallCornerObjImage = safeLoad("/images/wallCornerObj.png");
        this.wallSRCornerImage = safeLoad("/images/wallSRCorner.png");
        this.wallNRCornerImage = safeLoad("/images/wallNRCorner.png");
        this.wallNLCornerImage = safeLoad("/images/wallNLCorner.png");
        this.wallLeftObjImage = safeLoad("/images/wallLeftObj.png");
    }

    /**
     * Wykonuje bezpieczne ładowanie strumienia wejściowego pliku graficznego.
     * W przypadku braku zasobu zapobiega rzuceniu wyjątku NullPointerException,
     * logując błąd w konsoli deweloperskiej.
     *
     * @param path Ścieżka relatywna do pliku w folderze zasobów.
     * @return Obiekt klasy {@link Image} lub null, jeśli ładowanie się nie powiodło.
     */
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

    /**
     * Główna metoda renderująca wywoływana w każdym kroku pętli graficznej.
     * Wykonuje potok rysowania podzielony na czyszczenie bufora, rysowanie mapy kafelków (tło),
     * nakładanie obiektów wyposażenia oraz nanoszenie dynamicznych postaci wraz z ich interfejsem
     * tekstowym (imiona) i graficznym (paski postępu zadań).
     *
     * @param sim Instancja głównego silnika symulacji dostarczająca dane o agentach i budżecie.
     */
    public void render(game.core.Simulation sim) {
        // 1. Czyszczenie ekranu przed każdym rysowaniem (Double buffering preparation)
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // 2. Renderowanie dwuwarstwowej mapy środowiska (Floor & Object Batch Rendering)
        for (int y = 0; y < board.getHeight(); y++) {
            for (int x = 0; x < board.getWidth(); x++) {
                Cell cell = board.getCell(x, y);

                int px = x * TILE_SIZE;
                int py = y * TILE_SIZE;

                // --- WARSTWA 1: Podłogi i podłoża ---
                int[][] floorMap = board.getFloorMap();
                int floorType = floorMap[y][x];

                switch (floorType) {
                    case 1:
                        if (outsideImage != null) gc.drawImage(outsideImage, px, py, TILE_SIZE, TILE_SIZE);
                        else drawPlaceholder(px, py, Color.GREEN);
                        break;
                    case 2:
                        if (bossOfficeImage != null) gc.drawImage(bossOfficeImage, px, py, TILE_SIZE, TILE_SIZE);
                        else drawPlaceholder(px, py, Color.LIGHTGOLDENRODYELLOW);
                        break;
                    case 3:
                        if (wallImage != null) gc.drawImage(wallImage, px, py, TILE_SIZE, TILE_SIZE);
                        else drawPlaceholder(px, py, Color.DARKGRAY);
                        break;
                    case 0:
                    default:
                        if (floorImage != null) gc.drawImage(floorImage, px, py, TILE_SIZE, TILE_SIZE);
                        else drawPlaceholder(px, py, Color.LIGHTGRAY);
                        break;
                }

                // --- WARSTWA 2: Wyposażenie i ściany strukturalne ---
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
                    case "wall":
                        if (wallObjImage != null) gc.drawImage(wallObjImage, px, py, TILE_SIZE, TILE_SIZE);
                        else drawPlaceholder(px, py, Color.GRAY);
                        break;
                    case "wall_right":
                        if (wallRightObjImage != null) gc.drawImage(wallRightObjImage, px, py, TILE_SIZE, TILE_SIZE);
                        else drawPlaceholder(px, py, Color.GRAY);
                        break;
                    case "wall_back":
                        if (wallBackObjImage != null) gc.drawImage(wallBackObjImage, px, py, TILE_SIZE, TILE_SIZE);
                        else drawPlaceholder(px, py, Color.GRAY);
                        break;
                    case "wall_corner":
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
                    case "wall_left":
                        if (wallLeftObjImage != null) gc.drawImage(wallLeftObjImage, px, py, TILE_SIZE, TILE_SIZE);
                        else drawPlaceholder(px, py, Color.GRAY);
                        break;
                    default:
                        break;
                }
            }
        }

        // 3. Renderowanie warstwy dynamicznej (Agent Sprite Processing)
        for (Agent agent : sim.getAgents()) {

            double px = agent.getVisualX() * TILE_SIZE;
            double py = agent.getVisualY() * TILE_SIZE;

            boolean isDrinkingCoffee = false;
            boolean isSmokingCigarette = false;

            if (agent instanceof Worker) {
                Worker worker = (Worker) agent;
                String state = worker.getCurrentStateName();
                String tileType = board.getCell(worker.getX(), worker.getY()).getType();

                isDrinkingCoffee = "RestingState".equals(state) && "coffee".equalsIgnoreCase(tileType);
                isSmokingCigarette = "RestingState".equals(state) && "outside".equalsIgnoreCase(tileType);
            }

            Image imgToDraw = null;

            // Polimorficzna selekcja tekstury na podstawie klasy i maszyn stanowych
            if (agent instanceof Boss) {
                Boss boss = (Boss) agent;
                if (sim.getBudget() < 1000.0 && bossMadImg != null) {
                    imgToDraw = bossMadImg;
                } else {
                    imgToDraw = bossImg;
                }
            } else if (agent instanceof Senior) {
                Senior senior = (Senior) agent;
                String seniorState = senior.getCurrentStateName();

                if ("MadState".equals(seniorState)) {
                    imgToDraw = (seniorMadImgAlt != null) ? seniorMadImgAlt : seniorMadImg;
                } else if (isSmokingCigarette && seniorSmokingImg != null) {
                    imgToDraw = seniorSmokingImg;
                } else if (("TalkingState".equals(seniorState) || "ConversationState".equals(seniorState)) && seniorTalkingImg != null) {
                    imgToDraw = seniorTalkingImg;
                } else if ("WorkingState".equals(seniorState)) {
                    imgToDraw = (seniorWorkingImgAlt != null) ? seniorWorkingImgAlt : seniorWorkingImg;
                } else if ("SuccessState".equals(seniorState) && seniorSuccessImg != null) {
                    imgToDraw = seniorSuccessImg;
                } else {
                    imgToDraw = (isDrinkingCoffee && seniorCoffeeImg != null) ? seniorCoffeeImg : seniorImg;
                }
            } else if (agent instanceof Junior) {
                Junior junior = (Junior) agent;
                String juniorState = junior.getCurrentStateName();

                if ("CryingState".equals(juniorState) && juniorCryingImg != null) {
                    imgToDraw = juniorCryingImg;
                } else if (isSmokingCigarette && juniorSmokingImg != null) {
                    imgToDraw = juniorSmokingImg;
                } else if (("TalkingState".equals(juniorState) || "ConversationState".equals(juniorState)) && juniorTalkingImg != null) {
                    imgToDraw = juniorTalkingImg;
                } else if ("SuccessState".equals(juniorState) && juniorSuccessImg != null) {
                    imgToDraw = juniorSuccessImg;
                } else if ("WorkingState".equals(juniorState) && juniorWorkingImg != null) {
                    imgToDraw = juniorWorkingImg;
                } else {
                    imgToDraw = (isDrinkingCoffee && juniorCoffeeImg != null) ? juniorCoffeeImg : juniorImg;
                }
            }

            // Rysowanie wybranego awatara lub wektora zastępczego (Fallback Shapes)
            if (imgToDraw != null) {
                gc.drawImage(imgToDraw, px, py, TILE_SIZE, TILE_SIZE);
            } else {
                gc.setFill(agent instanceof Boss ? Color.RED : Color.BLUE);
                gc.fillOval(px + 32, py + 32, 64, 64);
            }

            // Nakładanie interfejsu tekstowego (HUD tekstowy postaci)
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(agent.getName(), px + (TILE_SIZE / 2), py + 20);

            // Wyznaczanie i rysowanie paska postępu zadania (Task Progress Bar)
            if (agent instanceof Worker) {
                Worker worker = (Worker) agent;

                if (worker.hasTask() && worker.getTotalTaskTime() > 0 && "WorkingState".equals(worker.getCurrentStateName())) {
                    double procent = (double) worker.getTurnsLeft() / worker.getTotalTaskTime();

                    double szerokoscPaska = 80;
                    double wysokoscPaska = 10;
                    double startX = px + (TILE_SIZE - szerokoscPaska) / 2;
                    double startY = py - 8;

                    gc.setFill(Color.web("#424242"));
                    gc.fillRect(startX, startY, szerokoscPaska, wysokoscPaska);

                    gc.setFill(Color.web("#22C55E"));
                    gc.fillRect(startX, startY, szerokoscPaska * procent, wysokoscPaska);

                    gc.setStroke(Color.BLACK);
                    gc.setLineWidth(1.5);
                    gc.strokeRect(startX, startY, szerokoscPaska, wysokoscPaska);
                }
            }
        }
    }

    /**
     * Rysuje geometryczny kształt zastępczy (Placeholder Grid Unit) wraz z obramowaniem.
     * Wywoływana automatycznie, jeśli określona tekstura nie została odnaleziona na dysku.
     *
     * @param x Współrzędna X rzutu pikselowego.
     * @param y Współrzędna Y rzutu pikselowego.
     * @param color Kolor wypełnienia kształtu zastępczego.
     */
    private void drawPlaceholder(int x, int y, Color color) {
        gc.setFill(color);
        gc.fillRect(x, y, TILE_SIZE, TILE_SIZE);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1);
        gc.strokeRect(x, y, TILE_SIZE, TILE_SIZE);
    }

    /**
     * Zwraca gotowy komponent graficzny Canvas zawierający wyrenderowany widok.
     * Służy do wstrzyknięcia widoku do głównej sceny (Scene) w klasie startowej aplikacji.
     *
     * @return Komponent {@link Canvas}.
     */
    public Canvas getCanvas() {
        return canvas;
    }
}