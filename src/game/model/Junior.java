package game.model;

import game.core.GameConfiguration;
import game.core.Simulation;

public class Junior extends Worker {
    private int numberOfFails;
    private double failChance;
    private boolean wasBossNeighborInPreviousTurn = false;

    public Junior(int x, int y, double efficiency, double experience) {
        super(x, y, efficiency, experience);
        this.numberOfFails = 0;
        this.failChance = Math.max(0.1, 1.0 - experience);
    }

    public boolean wasBossNeighborInPreviousTurn() { return wasBossNeighborInPreviousTurn; }
    public void setWasBossNeighborInPreviousTurn(boolean value) { this.wasBossNeighborInPreviousTurn = value; }

    @Override
    public double getFailChance() { return this.failChance; }

    @Override
    public void handleTaskFailure(Simulation sim) {
        this.incrementFails();
        sim.reportJuniorFail();
    }

    // ZMIANA: Definiujemy, kiedy wyniki Juniora kwalifikują go do zwolnienia przez Szefa
    @Override
    public boolean hasTerribleMetrics() {
        return super.hasTerribleMetrics() || this.numberOfFails >= GameConfiguration.MAX_FAILS_LIMIT;
    }

    // ZMIANA: Usunięto nadpisanie shouldBeFired() – teraz w pełni polegamy na mechanizmie rodzica (decyzji Szefa)

    public int getNumberOfFails() { return numberOfFails; }

    public void incrementFails() {
        this.numberOfFails++;
        System.out.println(this.getName() + " zawalił zadanie! Liczba jego błędów: " + this.numberOfFails);
    }
}