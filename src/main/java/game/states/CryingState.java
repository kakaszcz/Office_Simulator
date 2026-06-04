package game.states;

import game.core.Simulation;
import game.model.GameBoard;
import game.model.Worker;

public class CryingState implements WorkerState {

    private int cryTurnsRemaining = 2;

    @Override
    public void enter(Worker worker) {
        worker.setEfficiency(Math.max(0.0, worker.getEfficiency() - 0.10));
        System.out.println("[STAN] " + worker.getName() + " załamał się błędem i głośno płacze!");
    }

    @Override
    public void act(Worker worker, GameBoard board, Simulation sim) {
        cryTurnsRemaining--;
        System.out.println("  -> " + worker.getName() + " wciąż płacze... Pozostało tur: " + cryTurnsRemaining);

        if (cryTurnsRemaining <= 0) {
            System.out.println("  -> " + worker.getName() + " uspokoił się.");

            // Po płaczu sprawdzamy, czy nie jest przemęczony
            if (worker.getEfficiency() < 0.45) {
                worker.changeState(new MovingToRestState());
            } else {
                worker.changeState(new WaitingForTaskState());
            }
        }
    }
}