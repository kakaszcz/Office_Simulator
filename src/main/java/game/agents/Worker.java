package game.agents;

import game.model.Cell;
import game.model.GameBoard;
import game.model.PathFinder;
import game.states.*;
import game.core.Simulation;
import game.core.GameConfiguration;

import java.util.List;

//spr.
/**
 * Abstrakcyjna klasa bazowa reprezentująca pracownika (programistę) w biurze.
 * Zarządza cyklem życia agenta za pomocą wzorca projektowego Stanu (WorkerState),
 * realizuje algorytmy nawigacji i unikania zakleszczeń w korytarzach za pomocą PathFindera,
 * a także zbiera szczegółowe statystyki personalne (zadania, kawy, papierosy, płacz).
 */
public abstract class Worker extends Agent {

    private double efficiency;
    private double experience;
    protected WorkerState currentState;

    private final PathFinder pathfinder = new PathFinder();
    private List<Cell> currentPath;
    private Cell currentTargetCell;

    private boolean shouldBeFired = false;
    private boolean hasTask = false;
    private int turnsLeft = 0;

    private int tasksCompleted = 0;
    private int tasksFailed = 0;
    private int personalCoffeesDrunk = 0;
    private int personalCigarettesSmoked = 0;
    private int timesCried = 0;
    private int bossTalks = 0;
    private int bossBoosts;
    private int bugsRepaired = 0;

    private int totalTaskTime;



    /**
     * Tworzy nowego pracownika i inicjalizuje jego parametry bazowe.
     * Automatycznie wprowadza agenta w stan oczekiwania na zadanie (WaitingForTaskState),
     * co eliminuje błędy zawieszania się postaci na starcie symulacji.
     *
     * @param x Początkowa logiczna współrzędna X na siatce planszy.
     * @param y Początkowa logiczna współrzędna Y na siatce planszy.
     * @param efficiency Współczynnik wydajności bazowej pracownika.
     * @param experience Współczynnik doświadczenia zawodowego pracownika.
     */
    public Worker(int x, int y, double efficiency, double experience) {
        super(x, y);
        this.efficiency = efficiency;
        this.experience = experience;

        // FIX: Każdy pracownik rodzi się od razu w stanie oczekiwania na zadanie,
        // co zapobiega powstawaniu "zawieszonych" agentów bez przypisanego stanu (Idle-zombie)
        this.changeState(new game.states.WaitingForTaskState());
    }

    /**
     * Dokonuje bezpiecznej zmiany aktualnego stanu zachowania pracownika.
     * Przy każdej zmianie automatycznie czyści poprzednią ścieżkę ruchu i punkt docelowy,
     * umożliwiając nowemu stanowi niezależne wyznaczenie kolejnej trasy podróży.
     *
     * @param newState Nowy obiekt stanu (WorkerState), w który ma wejść pracownik.
     */
    public void changeState(WorkerState newState) {
        this.currentState = newState;
        if (this.currentState != null) {
            // Przy każdej zmianie stanu czyścimy zapisaną ścieżkę kafelków,
            // żeby nowy stan mógł niezależnie wyznaczyć swój cel podróży
            this.currentPath = null;
            this.currentTargetCell = null;

            this.currentState.enter(this);
        }
    }

    /**
     * Wywołuje logikę przypisaną do aktualnego stanu pracownika w danej turze symulacji.
     * Deleguje wykonanie akcji bezpośrednio do obiektu currentState.
     *
     * @param board Obiekt planszy potrzebny do analizy otoczenia przez stan.
     * @param sim Obiekt silnika symulacji dający dostęp do globalnych parametrów.
     */
    @Override
    public void act(GameBoard board, Simulation sim) {
        if (currentState != null) {
            currentState.act(this, board, sim);
        }
    }

    /**
     * Przemieszcza pracownika krok po kroku w kierunku wyznaczonego kafelka docelowego.
     * Wykorzystuje algorytm PathFindera do wyznaczenia optymalnej ścieżki. W przypadku
     * wykrycia blokady (np. inny agent w wąskim przejściu), metoda resetuje ścieżkę i wykonuje
     * losowy krok omijający, aby odblokować korytarz.
     *
     * @param targetCell Docelowy kafelek (Cell), do którego zmierza pracownik.
     * @param board Obiekt planszy gry (GameBoard) odpowiedzialny za zatwierdzanie ruchu.
     */
    public void navigateTo(Cell targetCell, GameBoard board) {
        if (targetCell == null) return;

        if (this.currentPath == null || !targetCell.equals(this.currentTargetCell)) {
            this.currentTargetCell = targetCell;
            Cell startCell = board.getCell(this.getX(), this.getY());
            this.currentPath = pathfinder.findPath(startCell, targetCell, board);
        }

        if (this.currentPath != null && !this.currentPath.isEmpty()) {
            Cell nextCell = this.currentPath.get(0);

            if (board.moveAgent(this.getX(), this.getY(), nextCell.getX(), nextCell.getY())) {
                this.setX(nextCell.getX());
                this.setY(nextCell.getY());
                this.currentPath.remove(0);
            } else {
                // Droga zablokowana przez innego agenta w wąskim przejściu
                this.currentPath = null; // Zapominamy ścieżkę, by przeliczyć ją na nowo w kolejnej turze

                // Wykonujemy krok omijający w bok, aby odblokować korytarz i pozwolić drugiemu przejść
                moveRandomly(board, false);
            }
        }
    }

    public double getFailChance() {
        return 0.0;
    }

    public void handleTaskFailure(Simulation sim) {
        // Domyślnie nic się nie dzieje (nadpisywane przez Juniora)
    }

    public double getPerformance() {
        return (efficiency + experience) / 2.0;
    }

    public boolean hasTerribleMetrics() {
        return getPerformance() < GameConfiguration.MIN_PERFORMANCE_THRESHOLD;
    }

    public void assignTask() {
        if (currentState instanceof WaitingForTaskState) {
            hasTask = true;
        }
    }


    /**
     * Weryfikuje, czy w bieżącej turze Szef znajduje się w bezpośrednim sąsiedztwie pracownika.
     * Sprawdzenie obejmuje promień maksymalnie 1 kafelka we wszystkich kierunkach (siatka 3x3).
     *
     * @param sim Obiekt symulacji (Simulation), z którego pobierana jest aktualna pozycja Szefa.
     * @return true, jeśli Szef stoi na sąsiednim kafelku; false w przeciwnym wypadku.
     */
    public boolean isBossNeighbor(Simulation sim) {
        Agent boss = sim.getBoss();
        if (boss == null) return false;

        // Sprawdzenie czy odległość w obu osiach wynosi maksymalnie 1 kafelek (zasięg sąsiedztwa)
        return Math.abs(this.getX() - boss.getX()) <= 1 &&
                Math.abs(this.getY() - boss.getY()) <= 1;
    }

    public boolean shouldBeFired() { return shouldBeFired; }
    public void markFired() { this.shouldBeFired = true; }

    public double getEfficiency() { return efficiency; }
    public void setEfficiency(double efficiency) { this.efficiency = efficiency; }
    public double getExperience() { return experience; }
    public void setExperience(double experience) { this.experience = experience; }
    public boolean hasTask() { return hasTask; }
    public void setHasTask(boolean hasTask) { this.hasTask = hasTask; }


    /**
     * Oblicza liczbę tur potrzebnych pracownikowi na ukończenie zadania.
     * Czas wyliczany jest dynamicznie na podstawie matematycznego wzoru uwzględniającego
     * ogólną wydajność (performance) oraz parametry z konfiguracji gry. Wynik jest zabezpieczony,
     * aby czas trwania zadania wynosił co najmniej 1 turę.
     *
     * @return Liczba tur (liczba całkowita) potrzebna na sfinalizowanie zadania.
     */
    public int computeTaskTime() {
        double dividend = GameConfiguration.TASK_TIME_DIVIDEND;
        double denomOffset = GameConfiguration.TASK_TIME_PERFORMANCE_DENOMINATOR_OFFSET;
        double baseOffset = GameConfiguration.TASK_TIME_BASE_OFFSET;

        int calculatedTime = (int) Math.round(baseOffset + dividend / (denomOffset + getPerformance()));
        return Math.max(1, calculatedTime);
    }

    public String getCurrentStateName() {
        if (this.currentState == null) {
            return "IdleState";
        }
        return this.currentState.getClass().getSimpleName();
    }

    public void setTotalTaskTime(int time) { this.totalTaskTime = time; }
    public int getTotalTaskTime() { return this.totalTaskTime; }

    public int getTurnsLeft() { return turnsLeft; }

    public void setTurnsLeft(int turns) { this.turnsLeft = turns; }

    public void decrementTurnsLeft() {
        if (this.turnsLeft > 0) this.turnsLeft--;
    }

    public void recordTaskCompleted() { this.tasksCompleted++; }
    public void recordTaskFailed() { this.tasksFailed++; }
    public void recordCoffee() { this.personalCoffeesDrunk++; }
    public void recordCigarette() { this.personalCigarettesSmoked++; }
    public void recordCrying() { this.timesCried++; }
    public void recordBossTalk() { this.bossTalks++; }
    public void recordBossBoost() { this.bossBoosts++; }
    public int getBossBoosts() { return bossBoosts; }
    public void recordBugRepaired() { this.bugsRepaired++; }

    public int getTasksCompleted() { return tasksCompleted; }
    public int getTasksFailed() { return tasksFailed; }
    public int getPersonalCoffeesDrunk() { return personalCoffeesDrunk; }
    public int getCigarettesSmoked() { return personalCigarettesSmoked; }
    public int getTimesCried() { return timesCried; }
    public int getBossTalks() { return bossTalks; }
    public int getBugsRepaired() { return bugsRepaired; }

}