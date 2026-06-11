package game.states;

import game.agents.Worker;
import game.model.GameBoard;
import game.core.Simulation;
import game.core.GameConfiguration;

public class MadState implements WorkerState {

    private int madTurns = 4; // Zły przez 4 tury

    @Override
    public void enter(Worker worker) {
        // Złość kosztuje wydajność
        worker.setEfficiency(Math.max(0.0, worker.getEfficiency() - 0.15));
        System.out.println("[STAN] " + worker.getName() + " jest wściekły!");
    }

    @Override
    public void act(Worker worker, GameBoard board, Simulation sim) {
        madTurns--;
        System.out.println("  -> " + worker.getName() + " nadal się wścieka. Zostało tur: " + madTurns);

        if (madTurns <= 0) {
            // Po ochłonięciu idzie zapalić albo wróci do czekania
            if (worker.getEfficiency() < GameConfiguration.EFFICIENCY_REST_THRESHOLD) {
                worker.changeState(new SmokingState());
            } else {
                worker.changeState(new WaitingForTaskState());
            }
        }
    }
}
