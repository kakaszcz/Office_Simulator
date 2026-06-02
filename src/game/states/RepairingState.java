package game.states;

import game.model.Worker;
import game.model.GameBoard;
import game.core.Simulation;

public class RepairingState implements WorkerState {

    private int repairTimeRemaining;

    @Override
    public void enter(Worker worker) {
        // Zgodnie z diagramem: do / repairTime = taskTime
        this.repairTimeRemaining = worker.computeTaskTime();
        worker.setEfficiency(Math.max(0.0, worker.getEfficiency() - 0.10));
        System.out.println("[STAN] Senior " + worker.getName() + " rozpoczyna naprawianie bugów. Zajmie to: " + repairTimeRemaining + " tur.");
    }

    @Override
    public void act(Worker worker, GameBoard board, Simulation sim) {
        repairTimeRemaining--;

        System.out.println("  -> " + worker.getName() + " naprawia... Pozostało tur: " + repairTimeRemaining);

        if (repairTimeRemaining <= 0) {
            // Sukces naprawy! Informujemy Mediator (Simulation)
            sim.repairFail();

            // Po naprawie sprawdzamy czy nie pora odpocząć
            if (worker.getEfficiency() < 0.45) {
                worker.changeState(new MovingToRestState());
            } else {
                worker.changeState(new WaitingForTaskState());
            }
        }
    }
}