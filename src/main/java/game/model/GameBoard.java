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
        //1 - grass, 0-floor, 2-boss_office_floor, 3-wallNotWalkable, 4- floorNotWalkable

        int[][] objectLayout = {
                {0, 0, 14, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 14, 10, 13},
                {0, 0,  5,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  5,  8, 15}, // 8 to biurko bossa
                {0, 0,  5,  0,  9,  0,  9,  0,  9,  0,  9,  0,  0,  5,  0, 15},
                {0, 0,  5,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 11,  6, 12},
                {0, 0,  5,  0,  9,  0,  9,  0,  9,  0,  9,  0,  0,  0,  0, 15}, // Zwykłe biurka (9)
                {0, 0,  5,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 15},
                {0, 0,  5,  0,  9,  0,  9,  0,  9,  0,  9,  0,  0,  0,  0, 15},
                {0, 0,  5,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  7,  7, 15},
                {0, 0,  5,  0,  9,  0,  9,  0,  9,  0,  9,  0,  0,  0,  0, 15},
                {0, 0, 11,  6,  6,  6,  6,  6,  6,  6,  6,  6,  6,  6,  6, 12}
        };
        //7 - coffeeObj, 9 - worker_deskObj, 6 - wallObj, 0 - floor, 8 - boss_deskObj, 5-wallRightObj

        if (floorLayout.length != GameConfiguration.MAP_HEIGHT || floorLayout[0].length != GameConfiguration.MAP_WIDTH ||
                objectLayout.length != GameConfiguration.MAP_HEIGHT || objectLayout[0].length != GameConfiguration.MAP_WIDTH) {
            throw new IllegalStateException("BŁĄD KRYTYCZNY: Wymiary tablic nie zgadzają się z GameConfiguration!");
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
        // 1. Najpierw sprawdzamy obiekty z objectLayout
        if (objectValue == GameConfiguration.OBJ_DESK) return "desk";
        if (objectValue == GameConfiguration.OBJ_BOSS_DESK) return "boss_desk";
        if (objectValue == GameConfiguration.OBJ_COFFEE) return "coffee";
        if (objectValue == GameConfiguration.OBJ_WALL) return "wall";
        if (objectValue == GameConfiguration.OBJ_WALL_RIGHT) return "wall_right";
        if (objectValue == GameConfiguration.OBJ_WALL_SR_CORNER) return "wall_sr_corner";
        if (objectValue == GameConfiguration.OBJ_WALL_NR_CORNER) return "wall_nr_corner";
        if (objectValue == GameConfiguration.OBJ_WALL_NL_CORNER) return "wall_nl_corner";
        if (objectValue == GameConfiguration.OBJ_WALL_LEFT) return "wall_left";

        // --- OTO NOWE MAPOWANIE NAZW ---
        if (objectValue == GameConfiguration.OBJ_WALL_BACK) return "wall_back";
        if (objectValue == GameConfiguration.OBJ_WALL_CORNER) return "wall_corner";

        // 2. Jeśli w danym miejscu nie ma obiektu, sprawdzamy podłogi z floorLayout
        if (floorValue == GameConfiguration.FLOOR_OUTDOOR) return "outside";
        if (floorValue == GameConfiguration.FLOOR_BOSS_OFFICE) return "boss_office";
        if (floorValue == GameConfiguration.FLOOR_WALL_NOT_WALKABLE) return "wall_not_walkable";
        if (floorValue == GameConfiguration.FLOOR_NOT_WALKABLE) return "floor_not_walkable";

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
        return findFirstEmptyCell("boss_office_floor");
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

        if (oldCell != null && newCell != null && !oldCell.isEmpty() && newCell.isEmpty()) {
            if(!newCell.isWall() || newCell.isDesk()){
                Agent agent = oldCell.getAgent();
                oldCell.setAgent(null);
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