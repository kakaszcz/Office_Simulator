package game.states;

import game.core.GameConfiguration;
import game.core.Simulation;
import game.agents.Worker;
import game.model.Cell;
import game.model.GameBoard;

public class MovingToDeskState implements WorkerState {

    private Cell targetDesk;

    @Override
    public void enter(Worker worker) {
        System.out.println("[STAN] " + worker.getName() + " szuka wolnego biurka, aby wrócić do pracy.");
    }

    @Override
    public void act(Worker worker, GameBoard board, Simulation sim) {
        // Jeśli ktoś zajął nasze biurko w międzyczasie, porzuć je i szukaj od nowa
        if (targetDesk != null && !targetDesk.isEmpty() && targetDesk.getAgent() != worker) {
            System.out.println("  -> " + worker.getName() + " zauważył, że biurko zostało podkradzione! Szuka innego...");
            targetDesk = null;
        }

        if (targetDesk == null) {
            targetDesk = board.findFirstEmptyCell(GameConfiguration.TILE_TYPE_DESK);
        }

        if (targetDesk == null) {
            System.out.println("  -> " + worker.getName() + " nie może znaleźć wolnego biurka. Czeka.");
            return;
        }

        boolean reachedDestination = false;

        int steps = GameConfiguration.WORKER_MOVE_STEPS_PER_TURN;
        for (int i = 0; i < steps; i++) {
            // Wywołujemy nasz PathFinder ukryty w klasie Worker
            worker.navigateTo(targetDesk, board);

            // Sprawdzamy na bieżąco, czy pracownik dotarł już do biurka
            if (worker.getX() == targetDesk.getX() && worker.getY() == targetDesk.getY()) {
                reachedDestination = true;
                break; // Dotarł, przerywamy pętlę ruchu
            }
        }

        // Jeśli dotarł do celu, siada i czeka na zadania
        if (reachedDestination) {
            System.out.println("  -> " + worker.getName() + " usiadł przy swoim biurku.");
            worker.changeState(new WaitingForTaskState());
        }
    }
}