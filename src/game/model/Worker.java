package game.model;

import game.states.*;
import game.core.Simulation;
import game.core.GameConfiguration; // NOWY IMPORT

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

    public Worker(int x, int y, double efficiency, double experience) {
        super(x, y);
        this.efficiency = efficiency;
        this.experience = experience;
    }

    public void changeState(WorkerState newState) {
        this.currentState = newState;
        if (this.currentState != null) {
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
                this.currentPath = null;
            }
        }
    }

    public double getFailChance() {
        return 0.0;
    }

    public void handleTaskFailure(Simulation sim) {
        // Domyślnie nic się nie dzieje
    }

    public double getPerformance() {
        return (efficiency + experience) / 2.0;
    }

    // ZMIANA: Pracownicy sami z siebie mają dobre/złe wyniki, ale nie zwalniają się automatycznie.
    // Tę metodę będzie sprawdzał Szef podczas patrolu.
    public boolean hasTerribleMetrics() {
        return getPerformance() < GameConfiguration.MIN_PERFORMANCE_THRESHOLD;
    }

    public void assignTask() {
        if (currentState instanceof WaitingForTaskState) {
            hasTask = true;
        }
    }

    public boolean isBossNeighbor(GameBoard board) {
        int[][] directions = {
                {0, 1},  {0, -1}, {1, 0},  {-1, 0},
                {1, 1},  {1, -1}, {-1, 1}, {-1, -1}
        };

        for (int[] dir : directions) {
            int checkX = getX() + dir[0];
            int checkY = getY() + dir[1];

            if (board.isInBounds(checkX, checkY)) {
                Cell cell = board.getCell(checkX, checkY);
                if (cell != null && cell.getAgent() instanceof Boss) {
                    return true;
                }
            }
        }
        return false;
    }

    // ZMIANA: Zwraca true TYLKO wtedy, gdy Szef fizycznie ich zwolnił (postawił pieczątkę)
    public boolean shouldBeFired() { return shouldBeFired; }
    public void markFired() { this.shouldBeFired = true; }

    public double getEfficiency() { return efficiency; }
    public void setEfficiency(double efficiency) { this.efficiency = efficiency; }
    public double getExperience() { return experience; }
    public void setExperience(double experience) { this.experience = experience; }
    public boolean hasTask() { return hasTask; }
    public void setHasTask(boolean hasTask) { this.hasTask = hasTask; }

    public int computeTaskTime() {
        return Math.max(1, (int) Math.round(1 + 4.0 / (1.0 + getPerformance())));
    }

    public String getCurrentStateName() {
        if (this.currentState == null) {
            return "IdleState";
        }
        return this.currentState.getClass().getSimpleName();
    }

    private int turnsLeft = 0;

    public int getTurnsLeft() { return turnsLeft; }

    public void setTurnsLeft(int turns) { this.turnsLeft = turns; }

    public void decrementTurnsLeft() {
        if (this.turnsLeft > 0) this.turnsLeft--;
    }
}