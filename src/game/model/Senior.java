package game.model;

import game.states.WaitingForTaskState;

public class Senior extends Worker {

    private double experienceBonus;

    public Senior(int x, int y, double efficiency, double experience) {
        super(x, y, efficiency, experience);
        this.experienceBonus = 0.15; // Dodatkowy bonus dla Seniora
    }

    @Override
    public double getPerformance() {
        // Pobieramy standardową wydajność z klasy Worker i dodajemy bonus Seniora.
        // Używamy Math.min, aby ogólna wydajność nigdy nie przekroczyła 1.0 (100%).
        return Math.min(1.0, super.getPerformance() + this.experienceBonus);
    }
}