package game.states;

import game.agents.Worker;
import game.model.GameBoard;
import game.core.Simulation;

public class RepairingState implements WorkerState {

    private int repairTimeRemaining;
    private final Simulation sim;

    public RepairingState(Simulation sim) {
        this.sim = sim;
    }

    @Override
    public void enter(Worker worker) {
        // REFAKTOR: Tutaj tylko inicjalizujemy czas i obniżamy wydajność za wysiłek. Nie naliczamy naprawy przed pracą!
        this.repairTimeRemaining = worker.computeTaskTime();
        worker.setEfficiency(Math.max(0.0, worker.getEfficiency() - 0.10));
        System.out.println("[STAN] Senior " + worker.getName() + " rozpoczyna naprawianie bugów. Zajmie to: " + repairTimeRemaining + " tur.");
    }

    @Override
    public void act(Worker worker, GameBoard board, Simulation sim) {
        repairTimeRemaining--;

        System.out.println("  -> " + worker.getName() + " naprawia... Pozostało tur: " + repairTimeRemaining);

        if (repairTimeRemaining <= 0) {
            // REFAKTOR: Dopiero gdy licznik spadnie do zera, błąd zostaje oficjalnie uznany za naprawiony!
            worker.recordBugRepaired();
            sim.repairFail();
            System.out.println("  -> " + worker.getName() + " pomyślnie naprawił błąd w projekcie!");

            // Po faktycznie zakończonej naprawie sprawdzamy czy nie pora odpocząć
            if (worker.getEfficiency() < 0.45) {
                worker.changeState(new MovingToRestState());
            } else {
                worker.changeState(new WaitingForTaskState());
            }
        }
    }
}