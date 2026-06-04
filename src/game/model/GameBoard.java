package game.model;

import game.core.GameConfiguration;

public class GameBoard {
    private Cell[][] grid;
    private int[][] floorMap;

    public GameBoard() {
        createEmptyGrid();
        initalizeBoard();
    }

    private void createEmptyGrid() {
        this.grid = new Cell[GameConfiguration.MAP_HEIGHT][GameConfiguration.MAP_WIDTH];
        this.floorMap = new int[GameConfiguration.MAP_HEIGHT][GameConfiguration.MAP_WIDTH];
    }

    private void initalizeBoard() {
        int[][] floorLayout = {
                {0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2},
                {0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2},
                {0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
        };

        int[][] objectLayout = {
                {7, 7, 0, 0, 0, 0, 0, 6, 6, 6, 6},
                {7, 7, 0, 0, 0, 0, 0, 6, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 6, 0, 0, 0},
                {0, 9, 0, 9, 0, 9, 0, 9, 0, 9, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 9, 0, 9, 0, 9, 0, 9, 0, 9, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 6, 6, 6, 6, 6, 6, 6, 6, 6},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
        };
        //7 - coffee, 9 - desk, 6 - wall, 0 - floor,

        // ZMIANA: Twarda weryfikacja poprawności danych konfiguracyjnych
        if (floorLayout.length != GameConfiguration.MAP_HEIGHT || floorLayout[0].length != GameConfiguration.MAP_WIDTH ||
                objectLayout.length != GameConfiguration.MAP_HEIGHT || objectLayout[0].length != GameConfiguration.MAP_WIDTH) {
            throw new IllegalStateException("BŁĄD KRYTYCZNY: Wymiary tablic floorLayout/objectLayout w GameBoard nie zgadzają się z parametrami MAP_WIDTH/MAP_HEIGHT w GameConfiguration!");
        }

        this.floorMap = floorLayout;

        for (int y = 0; y < GameConfiguration.MAP_HEIGHT; y++) {
            for (int x = 0; x < GameConfiguration.MAP_WIDTH; x++) {
                String type = determineCellType(objectLayout[y][x], floorLayout[y][x]);
                grid[y][x] = new Cell(x, y, type);
            }
        }
    }

    private String determineCellType(int objectValue, int floorValue) {
        if (objectValue == GameConfiguration.OBJ_DESK) return "desk";
        if (objectValue == GameConfiguration.OBJ_COFFEE) return "coffee";
        if (objectValue == GameConfiguration.OBJ_WALL) return "wall";

        if (floorValue == GameConfiguration.FLOOR_BOSS_OFFICE) return "boss_office";
        if (floorValue == GameConfiguration.FLOOR_OUTDOOR) return "outside";

        return "floor";
    }

    public boolean isInBounds (int x, int y){
        return x >= 0 && x < GameConfiguration.MAP_WIDTH && y >= 0 && y < GameConfiguration.MAP_HEIGHT;
    }

    public Cell getCell(int x, int y) {
        if (!isInBounds(x, y)) return null;
        return grid[y][x];
    }

    public Cell findFirstEmptyCell(String type) {
        // 1. Tworzymy listę na wszystkie pasujące i wolne kafelki
        java.util.List<Cell> emptyCells = new java.util.ArrayList<>();

        for (int y = 0; y < GameConfiguration.MAP_HEIGHT; y++) {
            for (int x = 0; x < GameConfiguration.MAP_WIDTH; x++) {
                if (grid[y][x].getType().equals(type) && grid[y][x].isEmpty()) {
                    emptyCells.add(grid[y][x]); // Dodajemy do listy
                }
            }
        }

        // 2. Jeśli są jakieś wolne miejsca, LOSUJEMY jedno z nich
        if (!emptyCells.isEmpty()) {
            java.util.Random rand = new java.util.Random();
            return emptyCells.get(rand.nextInt(emptyCells.size()));
        }

        return null; // Brak wolnych miejsc na całej mapie
    }

    public Cell findBossOfficeCell(){
        return findFirstEmptyCell("boss_office");
    }

    public boolean placeAgent(Agent agent, int x, int y) {
        Cell cell = getCell(x, y);
        if (cell != null && cell.isEmpty() && !cell.isWall()) {
            cell.setAgent(agent);
            return true;
        }
        return false;
    }

    public boolean moveAgent(int oldX, int oldY, int newX, int newY) {
        Cell oldCell = getCell(oldX, oldY);
        Cell newCell = getCell(newX, newY);

        if (oldCell != null && newCell != null && !oldCell.isEmpty() && newCell.isEmpty() && !newCell.isWall()) {
            Agent agent = oldCell.getAgent();
            oldCell.setAgent(null);
            newCell.setAgent(agent);
            return true;
        }
        return false;
    }

    public int getWidth() { return GameConfiguration.MAP_WIDTH; }
    public int getHeight() { return GameConfiguration.MAP_HEIGHT; }
    public int[][] getFloorMap() { return floorMap; }
}