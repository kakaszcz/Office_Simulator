package game.model;

import game.agents.Agent;
import game.core.GameConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Logiczna plansza gry reprezentująca przestrzeń biurową w strukturze siatki dwuwymiarowej.
 * Odpowiada za inicjalizację layoutu architektonicznego,
 * walidację granic mapy, bezpieczną relokację agentów oraz zarządzanie bezkonfliktową
 * rezerwacją wolnych stanowisk pracy.
 */
public class GameBoard {
    private Cell[][] grid;
    private int[][] floorMap;
    private final Random rand = new Random();

    /**
     * Tworzy i inicjalizuje nową planszę gry o stałych wymiarach pobranych z konfiguracji.
     */
    public GameBoard(){
        createEmptyGrid();
        initalizeBoard();
    }

    /**
     * Alokuje pamięć dla dwuwymiarowych tablic komórek siatki oraz mapy kafelków podłogowych.
     */
    private void createEmptyGrid() {
        this.grid = new Cell[GameConfiguration.MAP_HEIGHT][GameConfiguration.MAP_WIDTH];
        this.floorMap = new int[GameConfiguration.MAP_HEIGHT][GameConfiguration.MAP_WIDTH];
    }

    /**
     * Buduje architekturę biura na podstawie wbudowanych matryc rozmieszczenia.
     * Łączy warstwę podłoża (floorLayout) z warstwą obiektów (objectLayout),
     * weryfikuje spójność wymiarów z plikiem konfiguracyjnym i generuje obiekty komórek.
     *
     * @throws IllegalStateException Jeśli wymiary zadeklarowanych matryc są niezgodne z GameConfiguration.
     */
    private void initalizeBoard() {
        int[][] floorLayout = {
                {1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 2},
                {1, 1, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 2, 2, 2},
                {1, 1, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 2, 2, 2},
                {1, 1, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 2, 2, 2},
                {1, 1, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {1, 1, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {1, 1, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {1, 1, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        };

        int[][] objectLayout = {
                {0, 0, 14, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 14, 10, 13},
                {0, 0,  5,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  5,  8, 15},
                {0, 0,  5,  0,  9,  0,  9,  0,  9,  0,  9,  0,  0,  5,  0, 15},
                {0, 0,  5,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 11,  0, 12},
                {0, 0,  5,  0,  9,  0,  9,  0,  9,  0,  9,  0,  0,  0,  0, 15},
                {0, 0,  5,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 15},
                {0, 0,  0,  0,  9,  0,  9,  0,  9,  0,  9,  0,  0,  0,  7, 15},
                {0, 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  7, 15},
                {0, 0,  0,  0,  9,  0,  9,  0,  9,  0,  9,  0,  0,  0,  7, 15},
                {0, 0, 11,  6,  6,  6,  6,  6,  6,  6,  6,  6,  6,  6,  6, 12}
        };

        if (floorLayout.length != GameConfiguration.MAP_HEIGHT || floorLayout[0].length != GameConfiguration.MAP_WIDTH ||
                objectLayout.length != GameConfiguration.MAP_HEIGHT || objectLayout[0].length != GameConfiguration.MAP_WIDTH) {
            throw new IllegalStateException("BŁĄD KRYTYCZNY: Wymiary tablic nie zgadzają się z GameConfiguration!");
        }

        for (int y = 0; y < GameConfiguration.MAP_HEIGHT; y++) {
            System.arraycopy(floorLayout[y], 0, this.floorMap[y], 0, GameConfiguration.MAP_WIDTH);
        }

        for (int y = 0; y < GameConfiguration.MAP_HEIGHT; y++) {
            for (int x = 0; x < GameConfiguration.MAP_WIDTH; x++) {
                String type = determineCellType(objectLayout[y][x], floorLayout[y][x]);
                grid[y][x] = new Cell(x, y, type);
            }
        }
    }

    /**
     * Mapuje liczbowe identyfikatory z matryc na zdefiniowane, tekstowe typy komórek.
     * Prioritetyzuje obiekty stałe przed podłożem.
     *
     * @param objectValue Liczbowa wartość obiektu z warstwy wyposażenia.
     * @param floorValue Liczbowa wartość typu podłoża z warstwy bazowej.
     * @return Tekstowy identyfikator kafelka (zgodny z GameConfiguration).
     */
    private String determineCellType(int objectValue, int floorValue) {
        if (objectValue == GameConfiguration.OBJ_DESK) return GameConfiguration.TILE_TYPE_DESK;
        if (objectValue == GameConfiguration.OBJ_BOSS_DESK) return GameConfiguration.TILE_TYPE_BOSS_DESK;
        if (objectValue == GameConfiguration.OBJ_COFFEE) return GameConfiguration.TILE_TYPE_COFFEE;
        if (objectValue == GameConfiguration.OBJ_WALL) return GameConfiguration.TILE_TYPE_WALL;
        if (objectValue == GameConfiguration.OBJ_WALL_RIGHT) return GameConfiguration.TILE_TYPE_WALL_RIGHT;
        if (objectValue == GameConfiguration.OBJ_WALL_SR_CORNER) return GameConfiguration.TILE_TYPE_WALL_SR_CORNER;
        if (objectValue == GameConfiguration.OBJ_WALL_NR_CORNER) return GameConfiguration.TILE_TYPE_WALL_NR_CORNER;
        if (objectValue == GameConfiguration.OBJ_WALL_NL_CORNER) return GameConfiguration.TILE_TYPE_WALL_NL_CORNER;
        if (objectValue == GameConfiguration.OBJ_WALL_LEFT) return GameConfiguration.TILE_TYPE_WALL_LEFT;
        if (objectValue == GameConfiguration.OBJ_WALL_BACK) return GameConfiguration.TILE_TYPE_WALL_BACK;
        if (objectValue == GameConfiguration.OBJ_WALL_CORNER) return GameConfiguration.TILE_TYPE_WALL_CORNER;

        if (floorValue == GameConfiguration.FLOOR_OUTDOOR) return GameConfiguration.TILE_TYPE_OUTSIDE;
        if (floorValue == GameConfiguration.FLOOR_BOSS_OFFICE) return GameConfiguration.TILE_TYPE_BOSS_OFFICE;
        if (floorValue == GameConfiguration.FLOOR_WALL_NOT_WALKABLE) return GameConfiguration.TILE_TYPE_WALL_NOT_WALKABLE;
        if (floorValue == GameConfiguration.FLOOR_NOT_WALKABLE) return GameConfiguration.TILE_TYPE_FLOOR_NOT_WALKABLE;

        return GameConfiguration.TILE_TYPE_FLOOR;
    }

    /**
     * Weryfikuje, czy podane współrzędne mieszczą się w dopuszczalnych granicach siatki mapy.
     *
     * @param x Współrzędna kolumny (szerokość).
     * @param y Współrzędna wiersza (wysokość).
     * @return true, jeśli punkt leży wewnątrz granic planszy; false w przeciwnym wypadku.
     */
    public boolean isInBounds (int x, int y){
        return x >= 0 && x < GameConfiguration.MAP_WIDTH && y >= 0 && y < GameConfiguration.MAP_HEIGHT;
    }

    /**
     * Pobiera obiekt komórki (Cell) na podstawie zadanych współrzędnych geograficznych.
     *
     * @param x Pozycja pozioma kafelka.
     * @param y Pozycja pionowa kafelka.
     * @return Obiekt Cell lub null, jeśli współrzędne wykraczają poza mapę.
     */
    public Cell getCell(int x, int y) {
        if (!isInBounds(x, y)) return null;
        return grid[y][x];
    }

    /**
     * Wyszukuje losową, wolną i nieprzypisaną komórkę określonego typu.
     * Wprowadzony mechanizm zabezpieczający sprawdza dostępność agenta oraz stan soft-locka (rezerwacji).
     * Wybrana komórka zostaje automatycznie oznaczona jako zarezerwowana.
     *
     * @param type Tekstowa nazwa poszukiwanego typu kafelka (np. "desk").
     * @return Instancja wolnej komórki Cell lub null, jeśli brak wolnych kafelków tego typu.
     */
    public Cell findFirstEmptyCell(String type) {
        List<Cell> emptyCells = new ArrayList<>();

        for (int y = 0; y < GameConfiguration.MAP_HEIGHT; y++) {
            for (int x = 0; x < GameConfiguration.MAP_WIDTH; x++) {
                // Zwracamy wyłącznie te kafelki, które nie mają agenta ORAZ nie są zarezerwowane
                if (grid[y][x].getType().equals(type) && grid[y][x].isEmpty() && !grid[y][x].isReserved()) {
                    emptyCells.add(grid[y][x]);
                }
            }
        }

        if (!emptyCells.isEmpty()) {
            Cell chosenCell = emptyCells.get(rand.nextInt(emptyCells.size()));
            // Automatycznie rezerwujemy wybrany kafelek dla pracownika, żeby nikt mu go nie zajął!
            chosenCell.setReserved(true);
            return chosenCell;
        }

        return null;
    }

    /**
     * Wyszukuje wolną strefę gabinetu Szefa.
     *
     * @return Wolna komórka przypisana do przestrzeni zarządu.
     */
    public Cell findBossOfficeCell(){
        return findFirstEmptyCell(GameConfiguration.TILE_TYPE_BOSS_OFFICE);
    }

    /**
     * Bezpiecznie umieszcza agenta na planszy we wskazanych współrzędnych,
     * o ile docelowy punkt jest wolny i nie jest przeszkodą stałą.
     *
     * @param agent Obiekt implementujący klasę Agent.
     * @param x Docelowa kolumna na planszy.
     * @param y Docelowy wiersz na planszy.
     * @return true, jeśli osadzenie agenta przebiegło pomyślnie; false w przypadku blokady.
     */
    public boolean placeAgent(Agent agent, int x, int y) {
        Cell cell = getCell(x, y);
        if (cell != null && cell.isEmpty() && !cell.isWall()) {
            cell.setAgent(agent);
            cell.setReserved(false);
            return true;
        }
        return false;
    }

    /**
     * Realizuje logiczne przemieszczenie agenta pomiędzy dwoma kafelkami planszy.
     * Zwalnia rezerwacje oraz referencje na kafelku źródłowym i przypisuje agenta do nowej lokalizacji.
     *
     * @param oldX Dotychczasowa pozycja X agenta.
     * @param oldY Dotychczasowa pozycja Y agenta.
     * @param newX Nowa, docelowa pozycja X.
     * @param newY Nowa, docelowa pozycja Y.
     * @return true, jeśli krok został wykonany poprawnie; false, jeśli ruch był niedozwolony.
     */
    public boolean moveAgent(int oldX, int oldY, int newX, int newY) {
        Cell oldCell = getCell(oldX, oldY);
        Cell newCell = getCell(newX, newY);

        if (oldCell != null && newCell != null && !oldCell.isEmpty() && newCell.isEmpty()) {
            if (newCell.isWalkable()) {
                Agent agent = oldCell.getAgent();
                oldCell.setAgent(null);
                // Agent fizycznie opuszcza kafelek, zwalniamy też rezerwację!
                oldCell.setReserved(false);

                newCell.setAgent(agent);
                return true;
            }
        }
        return false;
    }

    public int getWidth() { return GameConfiguration.MAP_WIDTH; }
    public int getHeight() { return GameConfiguration.MAP_HEIGHT; }
    public int[][] getFloorMap() { return floorMap; }
}