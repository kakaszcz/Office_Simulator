package game.states;

import game.core.GameConfiguration;
import game.core.Simulation;
import game.model.GameBoard;
import game.agents.Worker;

public class CryingState implements WorkerState {

    private int cryTurnsRemaining;

    @Override
    public void enter(Worker worker) {
        this.cryTurnsRemaining = GameConfiguration.CRY_DURATION_TURNS;

        double newEfficiency = worker.getEfficiency() - GameConfiguration.CRYING_EFFICIENCY_DROP;
        worker.setEfficiency(Math.max(0.0, newEfficiency));

        System.out.println("[STAN] " + worker.getName() + " załamał się błędem i głośno płacze!");
    }

    @Override
    public void act(Worker worker, GameBoard board, Simulation sim) {
        this.cryTurnsRemaining--;
        worker.recordCrying();

        System.out.println("  -> " + worker.getName() + " wciąż płacze... Pozostało tur: " + this.cryTurnsRemaining);

        if (this.cryTurnsRemaining <= 0) {
            System.out.println("  -> " + worker.getName() + " uspokoił się.");

            if (worker.getEfficiency() < GameConfiguration.EFFICIENCY_REST_THRESHOLD) {
                worker.changeState(new MovingToRestState());
            } else {
                worker.changeState(new WaitingForTaskState());
            }
        }
    }
}