package game.model;

import game.agents.Agent;
import game.core.GameConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameBoard {
    private Cell[][] grid;
    private int[][] floorMap;
    private final Random rand = new Random();

    public GameBoard(){
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

        int[][] objectLayout = {
                {0, 0, 14, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 14, 10, 13},
                {0, 0,  5,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  5,  8, 15},
                {0, 0,  5,  0,  9,  0,  9,  0,  9,  0,  9,  0,  0,  5,  0, 15},
                {0, 0,  5,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 11,  0, 12},
                {0, 0,  5,  0,  9,  0,  9,  0,  9,  0,  9,  0,  0,  0,  0, 15},
                {0, 0,  5,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 15},
                {0, 0,  5,  0,  9,  0,  9,  0,  9,  0,  9,  0,  0,  0,  7, 15},
                {0, 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  7, 15},
                {0, 0,  0,  0,  9,  0,  9,  0,  9,  0,  9,  0,  0,  0,  7, 15},
                {0, 0, 11,  6,  6,  6,  6,  6,  6,  6,  6,  6,  6,  6,  6, 12}
        };

        if (floorLayout.length != GameConfiguration.MAP_HEIGHT || floorLayout[0].length != GameConfiguration.MAP_WIDTH ||
                objectLayout.length != GameConfiguration.MAP_HEIGHT || objectLayout[0].length != GameConfiguration.MAP_WIDTH) {
            throw new IllegalStateException("BŁĄD KRYTYCZNY: Wymiary tablic nie zgadzają się z GameConfiguration!");
        }

        // REFAKTOR: Bezpieczne głębokie kopiowanie wartości zamiast nadpisywania referencji
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

        // Domyślny kafelek
        return GameConfiguration.TILE_TYPE_FLOOR;
    }

    public boolean isInBounds (int x, int y){
        return x >= 0 && x < GameConfiguration.MAP_WIDTH && y >= 0 && y < GameConfiguration.MAP_HEIGHT;
    }

    public Cell getCell(int x, int y) {
        if (!isInBounds(x, y)) return null;
        return grid[y][x];
    }

    public Cell findFirstEmptyCell(String type) {
        List<Cell> emptyCells = new ArrayList<>();

        for (int y = 0; y < GameConfiguration.MAP_HEIGHT; y++) {
            for (int x = 0; x < GameConfiguration.MAP_WIDTH; x++) {
                if (grid[y][x].getType().equals(type) && grid[y][x].isEmpty()) {
                    emptyCells.add(grid[y][x]);
                }
            }
        }

        if (!emptyCells.isEmpty()) {
            return emptyCells.get(rand.nextInt(emptyCells.size()));
        }

        return null;
    }

    public Cell findBossOfficeCell(){
        return findFirstEmptyCell(GameConfiguration.TILE_TYPE_BOSS_OFFICE);
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
            if (!newCell.isWall() || newCell.isDesk()) {
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