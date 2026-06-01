package game.core;

public class GameConfiguration {

    /*
    Ta klasa będzie przechowywała wszytskie wartości które pojawią się na planszy
     */

    public static final int MAX_FAILS_LIMIT = 5;
    public static final int MAX_JUNIORS = 10;
    public static final int MAX_SENIORS = 5;
    public static final double STARTING_BUDGET = 1000.0;
    public static final double FATAL_ERROR_PENALTY = 500.0;

//--- ROZMIAR PLANSZY ---
    public static final int MAP_WIDTH = 11;
    public static final int MAP_HEIGHT = 11;

    // --- WARSTWA PODŁÓG (Słownik znaczeń) ---
    public static final int FLOOR_EMPTY = 0;       // Zwykła podłoga w office
    public static final int FLOOR_OUTDOOR = 1;     // Trawa
    public static final int FLOOR_BOSS_OFFICE = 2; // Podłoga w gabinecie szefa

    // --- WARSTWA OBIEKTÓW I LOGIKI (Słownik znaczeń) ---
    public static final int OBJ_NONE = 0;          // Czyste pole - brak mebla
    public static final int OBJ_DESK = 9;          // Desk
    public static final int OBJ_COFFEE = 7;        // Ekspres do kawy
    public static final int OBJ_WALL = 6; // Fizyczna ściana, na którą nie da się wejść
}
