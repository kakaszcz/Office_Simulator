package game.states;

import game.agents.Worker;
import game.model.GameBoard;
import game.core.Simulation;

public class MadState implements WorkerState {

    // Liczba tur, przez które odgrywana jest animacja złości
    private int madTurns = 2;

    @Override
    public void enter(Worker worker) {
        System.out.println("[STAN] " + worker.getName() + " odgrywa animację wściekłości!");
    }

    @Override
    public void act(Worker worker, GameBoard board, Simulation sim) {
        madTurns--;
        System.out.println("  -> " + worker.getName() + " wścieka się wizualnie... Zostało tur: " + madTurns);

        if (madTurns <= 0) {
            System.out.println("  -> " + worker.getName() + " kończy animację złości i wraca do pracy.");
            worker.changeState(new WaitingForTaskState());
        }
    }
}