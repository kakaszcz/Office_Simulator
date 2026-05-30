public class GameConfiguration {

    /*
    Ta klasa będzie przechowywała wszytskie wartości które pojawią się na planszy
     */

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
