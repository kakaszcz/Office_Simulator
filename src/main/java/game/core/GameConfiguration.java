package game.core;

public class GameConfiguration {

    // ==========================================
    // --- PARAMETRY POCZĄTKOWE SYMULACJI ---
    // ==========================================
    /** Początkowa liczba Juniorów tworzona na starcie symulacji */
    public static final int STARTING_JUNIORS_COUNT = 5;
    /** Początkowa liczba Seniorów tworzona na starcie symulacji */
    public static final int STARTING_SENIORS_COUNT = 3;
    /** Tytuł okna aplikacji */
    public static final String APP_WINDOW_TITLE = "Symulacja Biura IT";

    // ==========================================
    // --- EKONOMIA I FINANSE ---
    // ==========================================
    public static final double STARTING_BUDGET = 2000.0;

    /** Kara za fatal error */
    public static final double FATAL_ERROR_PENALTY = 300.0;

    /** Podstawa zarobku za zadanie */
    public static final double TASK_BASE_REWARD = 50.0;
    /** Mnożnik doświadczenia */
    public static final double TASK_EXPERIENCE_MULTIPLIER = 10.0;

    /** Pensja pracowników */
    public static final double SALARY_SENIOR = 20.0;
    public static final double SALARY_JUNIOR = 5.0;

    // ==========================================
    // --- ZMĘCZENIE I WYDAJNOŚĆ ---
    // ==========================================
    /** Koszt startu pracy */
    public static final double WORK_ENTER_EFFICIENCY_DECREASE = 0.02;
    /** Spadek wydajności podczas pracy */
    public static final double WORK_STEP_EFFICIENCY_DECREASE = 0.025;
    /** Maksymalna możliwa wydajność pracownika */
    public static final double MAX_EFFICIENCY = 1.0;
    /** Próg wydajności, poniżej którego pracownik musi iść odpocząć */
    public static final double EFFICIENCY_REST_THRESHOLD = 0.35;
    /** Próg wydajności dla HR do zwolnienia pracownika (np. na koniec dnia) */
    public static final double MIN_PERFORMANCE_THRESHOLD = 0.30;

    /** Szybkość odnawiania wydajności przy piciu kawy (co turę) */
    public static final double COFFEE_REGEN_RATE = 0.20;
    /** Spadek wydajności, gdy Junior zaczyna płakać przez błąd */
    public static final double CRYING_EFFICIENCY_DROP = 0.05;
    /** Koszt wydajności Seniora za podjęcie się naprawy błędu/awarii */
    public static final double REPAIR_ENTER_EFFICIENCY_DECREASE = 0.10;

    // ==========================================
    // --- INTERAKCJE Z SZEFEM ---
    // ==========================================
    /** Wzrost wydajności Juniora, gdy podejdzie Szef */
    public static final double BOSS_MOTIVATION_EFFICIENCY_BOOST = 0.10;

    // ==========================================
    // --- BALANS PRACOWNIKÓW ---
    // ==========================================
    /** Bazowy mnożnik doświadczenia po pomyślnym ukończeniu zadania */
    public static final double JUNIOR_EXPERIENCE_GAIN_PER_TASK = 0.015;

    /** Minimalna szansa na błąd dla Juniora (nawet przy maksymalnym doświadczeniu) */
    public static final double JUNIOR_MIN_FAIL_CHANCE = 0.1;
    /** Dodatkowy stały bonus do wydajności (Performance) dla Seniora */
    public static final double SENIOR_EXPERIENCE_BONUS = 0.15;

    // ==========================================
    // --- USTAWIENIA CZASOWE (TURY) ---
    // ==========================================
    /** Czas między wypłatami */
    public static final int PAYDAY_INTERVAL = 30;
    /** Co ile tur Manager rozdziela nowe zadania */
    public static final int TASK_DISTRIBUTION_INTERVAL = 3;

    /** Ile tur trwa standardowy odpoczynek w kuchni / na zewnątrz */
    public static final int REST_DURATION_TURNS = 3;
    /** Ile tur Junior spędza na płaczu po błędzie */
    public static final int CRY_DURATION_TURNS = 2;
    /** Ile tur Senior rozmawia z Szefem (blokując swoją pracę) */
    public static final int BOSS_TALK_DURATION_TURNS = 1;

    // ==========================================
    // --- USTAWIENIA SYMULACJI I LIMITÓW ---
    // ==========================================
    /** Maksymalna ilość niepowodzeń przed Fatal Error */
    public static final int MAX_FAILS_LIMIT = 8;
    /** Maksymalna ilość juniorów */
    public static final int MAX_JUNIORS = 10;
    /** Maksymalna ilość seniorów */
    public static final int MAX_SENIORS = 5;

    // ==========================================
    // --- ROZMIAR I KONFIGURACJA PLANSZY ---
    // ==========================================
    /** Szerokość mapy */
    public static final int MAP_WIDTH = 16;
    /** Wysokość mapy */
    public static final int MAP_HEIGHT = 10;

    // ==========================================
    // --- PARAMETRY WIZUALNE I ANIMACJE ---
    // ==========================================
    /** Rozmiar pojedynczego kafelka (szerokość i wysokość) w pikselach na ekranie */
    public static final int UI_TILE_SIZE = 128;
    /** Bazowa prędkość poruszania się agentów na klatkę animacji */
    public static final double AGENT_BASE_VISUAL_SPEED = 0.04;
    /** Co ile 'ticków' (zależnych od prędkości) następuje zmiana klatki animacji chodu */
    public static final int AGENT_ANIMATION_FRAME_DELAY = 8;

    // ==========================================
    // --- IDENTYFIKATORY TEKSTOWE KAFELKÓW ---
    // ==========================================
    public static final String TILE_TYPE_DESK = "desk";
    public static final String TILE_TYPE_BOSS_DESK = "boss_desk";
    public static final String TILE_TYPE_COFFEE = "coffee";
    public static final String TILE_TYPE_WALL = "wall";
    public static final String TILE_TYPE_WALL_RIGHT = "wall_right";
    public static final String TILE_TYPE_WALL_SR_CORNER = "wall_sr_corner";
    public static final String TILE_TYPE_WALL_NR_CORNER = "wall_nr_corner";
    public static final String TILE_TYPE_WALL_NL_CORNER = "wall_nl_corner";
    public static final String TILE_TYPE_WALL_LEFT = "wall_left";
    public static final String TILE_TYPE_WALL_BACK = "wall_back";
    public static final String TILE_TYPE_WALL_CORNER = "wall_corner";

    public static final String TILE_TYPE_OUTSIDE = "outside";
    public static final String TILE_TYPE_BOSS_OFFICE = "boss_office";
    public static final String TILE_TYPE_WALL_NOT_WALKABLE = "wall_not_walkable";
    public static final String TILE_TYPE_FLOOR_NOT_WALKABLE = "floor_not_walkable";
    public static final String TILE_TYPE_FLOOR = "floor";

    // ==========================================
    // --- PARAMETRY SZEFA (BOSS) ---
    // ==========================================
    /** Co ile tur Szef musi napić się kawy oraz ile tur trwa jej picie przy ekspresie */
    public static final int BOSS_COFFEE_INTERVAL = 10;
    /** Domyślny zasięg kontroli/wzroku szefa */
    public static final int BOSS_CONTROL_RANGE = 1;
    /** Szansa na wykonanie ruchu przez Szefa podczas normalnego patrolu */
    public static final double BOSS_CHANCE_TO_MOVE_NORMAL = 0.40;
    /** Szansa na wykonanie ruchu przez Szefa, gdy budżet firmy spada (tryb paniki) */
    public static final double BOSS_CHANCE_TO_MOVE_PANIC = 0.90;

    // ==========================================
    // --- ALGORYTMY CZASU PRACY ---
    // ==========================================
    /** Stałe do wzoru wyliczającego czas trwania zadania na podstawie wydajności */
    public static final double TASK_TIME_BASE_OFFSET = 1.0;
    public static final double TASK_TIME_DIVIDEND = 4.0;
    public static final double TASK_TIME_PERFORMANCE_DENOMINATOR_OFFSET = 1.0;

    // ==========================================
    // --- PARAMETRY RUCHU PRACOWNIKÓW ---
    // ==========================================
    /** Liczba kroków (pól), jakie pracownik może przejść w ciągu jednej tury */
    public static final int WORKER_MOVE_STEPS_PER_TURN = 2;


    // ==========================================
    // --- KONTROLA CZASU I UI (TIMELINE / SLIDER) ---
    // ==========================================
    /** Bazowy czas trwania jednej tury w milisekundach przy prędkości 1.0x */
    public static final int GAME_LOOP_BASE_TICK_MS = 1000; //po prostu jedna sekunda

    /** Minimalna prędkość symulacji na suwaku */
    public static final double SPEED_SLIDER_MIN = 0.25;
    /** Maksymalna prędkość symulacji na suwaku */
    public static final double SPEED_SLIDER_MAX = 8.0;
    /** Domyślna początkowa prędkość symulacji */
    public static final double SPEED_SLIDER_DEFAULT = 1.0;
    /** Krok przesunięcia bloku suwaka */
    public static final double SPEED_SLIDER_BLOCK_INCREMENT = 0.25;
    /** Główna jednostka podziałki na suwaku */
    public static final double SPEED_SLIDER_MAJOR_TICK = 1.0;

    /** Odstęp między elementami (spacing) w panelu prędkości */
    public static final int UI_SPEED_PANEL_SPACING = 15;

    // ==========================================
    // --- GENEROWANIE STATYSTYK (FABRYKA) ---
    // ==========================================
    /** Zakresy doświadczenia dla Juniora (min i max) */
    public static final double JUNIOR_START_EXP_MIN = 0.3;
    public static final double JUNIOR_START_EXP_MAX = 0.65;

    /** Zakresy doświadczenia dla Seniora (min i max) */
    public static final double SENIOR_START_EXP_MIN = 0.6;
    public static final double SENIOR_START_EXP_MAX = 0.95;

    /** Ogólne parametry losowania bazowej wydajności */
    public static final double WORKER_START_EFF_MIN = 0.6;
    public static final double WORKER_START_EFF_RANGE_SIZE = 0.5;

    // ==========================================
    // --- MAPOWANIE LICZBOWE UKŁADU MAPY ---
    // ==========================================
    /** Numeryczne typy podłóg */
    public static final int FLOOR_EMPTY = 0;
    public static final int FLOOR_OUTDOOR = 1;
    public static final int FLOOR_BOSS_OFFICE = 2;
    public static final int FLOOR_WALL_NOT_WALKABLE = 3;
    public static final int FLOOR_NOT_WALKABLE = 4;

    /** Numeryczne typy obiektów (objectLayout) */
    public static final int OBJ_WALL_RIGHT = 5;
    public static final int OBJ_WALL = 6;
    public static final int OBJ_COFFEE = 7;
    public static final int OBJ_BOSS_DESK = 8;
    public static final int OBJ_DESK = 9;
    public static final int OBJ_WALL_BACK = 10;
    public static final int OBJ_WALL_CORNER = 11;
    public static final int OBJ_WALL_SR_CORNER = 12;
    public static final int OBJ_WALL_NR_CORNER = 13;
    public static final int OBJ_WALL_NL_CORNER = 14;
    public static final int OBJ_WALL_LEFT = 15;
}