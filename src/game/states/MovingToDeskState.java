package game.states;

import game.core.Simulation;
import game.model.Worker;
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
        // Jeśli nie mamy jeszcze biurka docelowego, szukamy go na planszy
        if (targetDesk == null) {
            targetDesk = board.findFirstEmptyCell("desk");
        }

        if (targetDesk == null) {
            System.out.println("  -> " + worker.getName() + " nie może znaleźć wolnego biurka. Czeka.");
            return;
        }

        boolean reachedDestination = false;

        for (int i = 0; i < 2; i++) {
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