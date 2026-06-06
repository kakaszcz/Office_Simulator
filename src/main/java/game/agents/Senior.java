package game.agents;

import game.core.GameConfiguration;

public class Senior extends Worker {

    private double experienceBonus;

    public Senior(int x, int y, double efficiency, double experience) {
        super(x, y, efficiency, experience);
        this.experienceBonus = GameConfiguration.SENIOR_EXPERIENCE_BONUS;
    }

    @Override
    public double getPerformance() {
        // Pobieramy standardową wydajność z klasy Worker i dodajemy bonus Seniora.
        // Używamy Math.min, aby ogólna wydajność nigdy nie przekroczyła 1.0 (100%).
        return Math.min(1.0, super.getPerformance() + this.experienceBonus);
    }
}