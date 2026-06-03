package game.model;

import game.states.*;
import game.core.Simulation;

public abstract class Worker extends Agent {

    private double efficiency;
    private double experience;
    protected WorkerState currentState;

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