package game.states;

import game.model.Worker;
import game.model.GameBoard;
import game.core.Simulation;

public class TalkingState implements WorkerState {

    private int talkingTurns = 1; // Zgodnie z diagramem: "1 tura"

    @Override
    public void enter(Worker worker) {
        worker.setEfficiency(Math.max(0.0, worker.getEfficiency() - 0.10));
        System.out.println("[STAN] Szef podszedł do Seniora. " + worker.getName() + " rozmawia z Szefem.");
    }

    @Override
    public void act(Worker worker, GameBoard board, Simulation sim) {
        talkingTurns--;

        System.out.println("  -> " + worker.getName() + " kończy rozmowę z Szefem (Eff: " + String.format("%.2f", worker.getEfficiency()) + ")");

        if (talkingTurns <= 0) {
            // Wraca do czekania przy biurku
            worker.changeState(new WaitingForTaskState());
        }
    }
}