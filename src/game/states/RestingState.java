package game.states;

import game.core.Simulation;
import game.model.*;
import game.core.GameConfiguration;

public class RestingState implements WorkerState {

    private final String restPlaceType;
    private int restTurnsRemaining = 3; // Odpoczynek trwa np. 3 tury

    public RestingState(String restPlaceType) {
        this.restPlaceType = restPlaceType;
    }

    @Override
    public void enter(Worker worker) {
        System.out.println("[STAN] " + worker.getName() + " rozpoczął odpoczynek w strefie: " + restPlaceType);
    }

    @Override
    public void act(Worker worker, GameBoard board, Simulation sim) {
        // 1. Unikalna zasada z diagramu: przyłapanie na dworze przez szefa
        if (worker instanceof Junior && restPlaceType.equals("outside") && worker.isBossNeighbor(board)) {
            worker.markFired(); // Flagujemy pracownika do wywalenia z firmy!
            System.out.println("!!! SKANDAL! Szef przyłapał pracownika " + worker.getName() + " na obijaniu się na dworze!");
        }

        if (restPlaceType.equals("outside")) {
            // Na dworze regeneracja jest błyskawiczna (np. od razu 100%)
            worker.setEfficiency(1.0);
        } else {
            // W kuchni (coffeeTable) rośnie powoli, co turę
            worker.setEfficiency(Math.min(1.0, worker.getEfficiency() + 0.20));
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