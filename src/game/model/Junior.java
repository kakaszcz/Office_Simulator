package game.model;

import game.core.GameConfiguration;
import game.core.Simulation;
import game.states.*;

public class Junior extends Worker {
    private int numberOfFails;
    private double failChance;

    private boolean wasBossNeighborInPreviousTurn = false;

    public Junior(int x, int y, double efficiency, double experience) {
        super(x, y, efficiency, experience);
        this.numberOfFails = 0;

        // Przykładowe obliczenie szansy na błąd (np. zależy odwrotnie od doświadczenia).
        this.failChance = Math.max(0.1, 1.0 - experience);

        this.changeState(new WaitingForTaskState());
    }

    // Gettery i settery dla nowej flagi
    public boolean wasBossNeighborInPreviousTurn() {
        return wasBossNeighborInPreviousTurn;
    }

    public void setWasBossNeighborInPreviousTurn(boolean value) {
        this.wasBossNeighborInPreviousTurn = value;
    }

    @Override
    public double getFailChance() {
        // Zamiast domyślnego 0.0 z klasy Worker, zwracamy realną szansę błędu Juniora
        return this.failChance;
    }

    @Override
    public void handleTaskFailure(Simulation sim) {
        // Gdy Junior zawali, sam dba o swoje liczniki i zgłasza błąd do symulacji
        this.incrementFails();
        sim.reportJuniorFail();
    }

    @Override
    public boolean shouldBeFired() {
        // Sprawdzamy flagę rodzica (przyłapanie) LUB limity Juniora (błędy i performance)
        return super.shouldBeFired() || this.numberOfFails >= GameConfiguration.MAX_FAILS_LIMIT || this.getPerformance() < GameConfiguration.MIN_PERFORMANCE_THRESHOLD;
    }

    public int getNumberOfFails() {
        return numberOfFails;
    }

    // Zmieniona metoda: teraz Junior podbija tylko SWÓJ licznik.
    // Globalnym licznikiem zajmie się stan WorkingState wywołując sim.reportJuniorFail()
    public void incrementFails() {
        this.numberOfFails++;
        System.out.println(this.getName() + " zawalił zadanie! Liczba jego błędów: " + this.numberOfFails);
    }
}
