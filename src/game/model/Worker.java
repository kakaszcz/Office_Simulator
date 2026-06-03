package game.model;

import game.states.*;
import game.core.Simulation;
import game.model.PathFinder;

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
        // 1. Sprawdzamy, czy cel jest osiągalny
        if (targetCell == null) return;

        // 2. Jeśli cel się zmienił, lub nie mamy jeszcze ścieżki, przeliczamy ją.
        if (this.currentPath == null || !targetCell.equals(this.currentTargetCell)) {
            this.currentTargetCell = targetCell;
            Cell startCell = board.getCell(this.getX(), this.getY());
            this.currentPath = pathfinder.findPath(startCell, targetCell, board);
        }

        // 3. Jeśli ścieżka istnieje i nie jest pusta, robimy krok.
        if (this.currentPath != null && !this.currentPath.isEmpty()) {
            // Pobieramy pierwszy kafelek z zaplanowanej ścieżki
            Cell nextCell = this.currentPath.get(0);

            // Próba wykonania ruchu. board.moveAgent musi sprawdzać ściany.
            if (board.moveAgent(this.getX(), this.getY(), nextCell.getX(), nextCell.getY())) {
                this.setX(nextCell.getX());
                this.setY(nextCell.getY());
                // Ruch udany! Usuwamy ten kafelek z zaplanowanej ścieżki.
                this.currentPath.remove(0);
            } else {
                // Ścieżka została zablokowana (np. przez innego agenta), wymuś przeliczenie w kolejnej turze.
                this.currentPath = null;
            }
        }
    }

    // Ta metoda zostanie nadpisana w klasie Junior!
    public double getFailChance() {
        return 0.0;
    }

    // POLIMORFIZM: Pozwalamy konkretnemu pracownikowi obsłużyć porażkę zadania
    public void handleTaskFailure(Simulation sim) {
        // Domyślnie (np. dla Seniora) nic się nie dzieje
    }

    public double getPerformance() {
        return (efficiency + experience) / 2.0;
    }

    public void assignTask() {
        if (currentState instanceof WaitingForTaskState) {
            hasTask = true;
        }
    }

    public boolean isBossNeighbor(GameBoard board) {
        int[][] directions = {
                {0, 1},  {0, -1}, {1, 0},  {-1, 0}, // Kierunki główne
                {1, 1},  {1, -1}, {-1, 1}, {-1, -1} // Skosy
        };

        for (int[] dir : directions) {
            int checkX = getX() + dir[0];
            int checkY = getY() + dir[1];

            if (checkX >= 0 && checkX < board.getWidth() && checkY >= 0 && checkY < board.getHeight()) {
                Cell cell = board.getCell(checkX, checkY);
                if (cell != null && cell.getAgent() instanceof Boss) {
                    return true;
                }
            }
        }
        return false;
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
        return Math.max(1, (int) Math.round(1 + 4.0 / (1.0 + getPerformance())));
    }


    //Połączenie klasy worker z gameView
    //Dla wyświetlania stanów
    public String getCurrentStateName() {
        if (this.currentState == null) {//Bezpiecznik (sprawdza czy jest przypisany jakikolwiek stan)
            return "IdleState"; // Domyślna nazwa awaryjna
        }
        // Pobiera czystą nazwę klasy stanu, np. "CoffeeState", "WorkingState", "WaitingForTaskState"
        return this.currentState.getClass().getSimpleName();
    }
}