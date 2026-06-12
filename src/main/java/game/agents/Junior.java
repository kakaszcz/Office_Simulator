package game.agents;

import game.core.GameConfiguration;
import game.core.Simulation;

public class Junior extends Worker {
    private int numberOfFails;
    private int lifetimeFails; // NOWOŚĆ: Licznik błędów, którego ŻADEN reset w Simulation nie wymaże
    private boolean wasBossNeighborInPreviousTurn = false;

    public Junior(int x, int y, double efficiency, double experience) {
        super(x, y, efficiency, experience);
        this.numberOfFails = 0;
        this.lifetimeFails = 0;
    }

    public boolean wasBossNeighborInPreviousTurn() { return wasBossNeighborInPreviousTurn; }
    public void setWasBossNeighborInPreviousTurn(boolean value) { this.wasBossNeighborInPreviousTurn = value; }

    @Override
    public double getFailChance() {
        // Bazowa szansa wynikająca z braku doświadczenia
        double baseFailChance = 1.0 - this.getExperience();

        // MODYFIKATOR ZMĘCZENIA: Im mniejsza efektywność (bliżej 0), tym większa szansa na błąd.
        // Jeśli efficiency spadnie np. do 0.4, (1.0 - 0.4) doda aż 0.6 do szansy na błąd!
        double fatiguePenalty = 1.0 - this.getEfficiency();

        double totalFailChance = baseFailChance + (fatiguePenalty * 0.5); // 0.5 to siła wpływu zmęczenia

        // Zwracamy wyliczoną szansę, ale nie mniejszą niż absolutne minimum z konfiguracji
        return Math.max(GameConfiguration.JUNIOR_MIN_FAIL_CHANCE, totalFailChance);
    }

    @Override
    public void handleTaskFailure(Simulation sim) {
        this.incrementFails();
        sim.reportJuniorFail();
    }

    @Override
    public boolean hasTerribleMetrics() {
        // HR patrzy na całkowitą historię porażek (lifetimeFails),
        // więc pracownik nie oszuka systemu po globalnym czyszczeniu błędów w biurze
        return super.hasTerribleMetrics() || this.lifetimeFails >= 5;
    }

    public int getTasksFailed() { return this.numberOfFails; }

    // Jeśli w Simulation.java masz metodę czyszczącą błędy (np. junior.resetCurrentFails()),
    // to wyzeruje ona numberOfFails, ale lifetimeFails zostanie nienaruszone!
    public void resetCurrentFails() {
        this.numberOfFails = 0;
    }

    public void incrementFails() {
        this.numberOfFails++;
        this.lifetimeFails++; // Zawsze rośnie, odkłada się w "kartotece" pracownika
        System.out.println(this.getName() + " zawalił zadanie! Indywidualne (obecne): "
                + this.numberOfFails + ", Łącznie w karierze: " + this.lifetimeFails);
    }
}