package game.agents;

import game.model.Cell;
import game.model.GameBoard;
import game.model.PathFinder;
import game.states.*;
import game.core.Simulation;
import game.core.GameConfiguration;

import java.util.List;

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




    public Worker(int x, int y, double efficiency, double experience) {
        super(x, y);
        this.efficiency = efficiency;
        this.experience = experience;

        // FIX: Każdy pracownik rodzi się od razu w stanie oczekiwania na zadanie,
        // co zapobiega powstawaniu "zawieszonych" agentów bez przypisanego stanu (Idle-zombie)
        this.changeState(new game.states.WaitingForTaskState());
    }

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

    @Override
    public void act(GameBoard board, Simulation sim) {
        if (currentState != null) {
            currentState.act(this, board, sim);
        }
    }

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