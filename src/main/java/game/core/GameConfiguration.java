package game.core;

public class GameConfiguration {


    //Ta klasa będzie przechowywała wszytskie wartości które pojawią się na planszy

    public static final int MAX_FAILS_LIMIT = 5;
    public static final int MAX_JUNIORS = 10;
    public static final int MAX_SENIORS = 5;
    public static final double STARTING_BUDGET = 1000.0;
    public static final double FATAL_ERROR_PENALTY = 500.0;
    public static final double MIN_PERFORMANCE_THRESHOLD = 0.45;

    //--- ROZMIAR PLANSZY ---
    public static final int MAP_WIDTH = 16;
    public static final int MAP_HEIGHT = 10;

    // Podłogi (floorLayout)
    public static final int FLOOR_EMPTY = 0;
    public static final int FLOOR_OUTDOOR = 1;       // U Ciebie: 1 - grass
    public static final int FLOOR_BOSS_OFFICE = 2;  // U Ciebie: 2 - boss_office_floor
    public static final int FLOOR_WALL_NOT_WALKABLE = 3; // U Ciebie: 3 - wallNotWalkable
    public static final int FLOOR_NOT_WALKABLE = 4; // U Ciebie: 4 - floorNotWalkable

    // Obiekty (objectLayout)
    public static final int OBJ_WALL = 6;           // U Ciebie: 6 - wallObj
    public static final int OBJ_COFFEE = 7;         // U Ciebie: 7 - coffeeObj
    public static final int OBJ_BOSS_DESK = 8;      // U Ciebie: 8 - boss_deskObj
    public static final int OBJ_DESK = 9;           // U Ciebie: 9 - worker_deskObj
    public static final int OBJ_WALL_RIGHT = 5;
    public static final int OBJ_WALL_BACK = 10;     // Dla wallBackObj.png
    public static final int OBJ_WALL_CORNER = 11;   // Dla wallCornerObj.png
}