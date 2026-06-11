package game.states;

import game.agents.Worker;
import game.model.GameBoard;
import game.core.Simulation;
import game.core.GameConfiguration;

public class SmokingState implements WorkerState {

    private int smokingTurns = 3; // Pali przez 3 tury

    @Override
    public void enter(Worker worker) {
        worker.recordCigarette();
        // Palenie chwilowo poprawia nastrój — mały bonus do wydajności
        worker.setEfficiency(Math.min(1.0, worker.getEfficiency() + 0.05));
        System.out.println("[STAN] " + worker.getName() + " wychodzi na papierosa.");
    }

    @Override
    public void act(Worker worker, GameBoard board, Simulation sim) {
        smokingTurns--;
        System.out.println("  -> " + worker.getName() + " pali papierosa. Zostało tur: " + smokingTurns);

        if (smokingTurns <= 0) {
            System.out.println("  -> " + worker.getName() + " wraca po papierosie.");
            worker.changeState(new WaitingForTaskState());
        }
    }
}
