package game.states;

import game.agents.Worker;
import game.model.GameBoard;
import game.core.Simulation;

public class SuccessState implements WorkerState {

    private int successTurns = 2; // Czas trwania animacji świętowania

    @Override
    public void enter(Worker worker) {
        // Zgodnie z diagramem: natychmiastowy przyrost statystyk po sukcesie
        worker.setExperience(Math.min(1.0, worker.getExperience() + 0.02));
        worker.setEfficiency(Math.min(1.0, worker.getEfficiency() + 0.05));

        System.out.println("[STAN] " + worker.getName() + " świętuje udane zadanie! 🎉 (Animacja sukcesu)");
    }

    @Override
    public void act(Worker worker, GameBoard board, Simulation sim) {
        successTurns--;
        System.out.println("  -> " + worker.getName() + " cieszy się z sukcesu. Zostało tur animacji: " + successTurns);

        if (successTurns <= 0) {
            worker.changeState(new WaitingForTaskState());
        }
    }
}