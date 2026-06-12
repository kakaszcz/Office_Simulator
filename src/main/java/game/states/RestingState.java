package game.states;

import game.agents.Junior;
import game.agents.Worker;
import game.core.GameConfiguration;
import game.core.Simulation;
import game.model.*;

public class RestingState implements WorkerState {

    private final String restPlaceType;
    private int restTurnsRemaining;
    private boolean statsUpdated = false;

    public RestingState(String restPlaceType) {
        this.restPlaceType = restPlaceType;
    }

    @Override
    public void enter(Worker worker) {
        this.restTurnsRemaining = GameConfiguration.REST_DURATION_TURNS;

        System.out.println("[STAN] " + worker.getName() + " rozpoczął odpoczynek w strefie: " + restPlaceType);

        if (GameConfiguration.TILE_TYPE_OUTSIDE.equals(restPlaceType)) {
            worker.recordCigarette();
        } else {
            worker.recordCoffee();
        }
    }

    @Override
    public void act(Worker worker, GameBoard board, Simulation sim) {

        if (!statsUpdated) {
            if (GameConfiguration.TILE_TYPE_OUTSIDE.equals(restPlaceType)) {
                sim.incrementCigarettes();
            } else {
                sim.incrementCoffee();
            }
            statsUpdated = true;
        }

        if (GameConfiguration.TILE_TYPE_OUTSIDE.equals(restPlaceType)) {
            // Na dworze regeneracja jest błyskawiczna (pobieramy MAX_EFFICIENCY)
            worker.setEfficiency(1.0);
        } else {
            // W kuchni rośnie powoli, co turę (pobieramy COFFEE_REGEN_RATE i limitujemy przez MAX_EFFICIENCY)
            double currentEff = worker.getEfficiency();
            double regeneratedEff = currentEff + GameConfiguration.COFFEE_REGEN_RATE;
            worker.setEfficiency(Math.min(1.0, regeneratedEff));
        }

        restTurnsRemaining--;

        System.out.println("  -> " + worker.getName() + " odpoczywa... (Aktualna wydajność: "
                + String.format("%.2f", worker.getEfficiency()) + ")");

        // Koniec odpoczynku
        if (restTurnsRemaining <= 0) {
            System.out.println("  -> " + worker.getName() + " odpoczął i wraca do pracy.");
            worker.changeState(new MovingToDeskState());
        }
    }
}