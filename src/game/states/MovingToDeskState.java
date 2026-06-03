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
        if (targetDesk == null) {
            targetDesk = board.findFirstEmptyCell("desk");
        }

        if (targetDesk == null) {
            System.out.println("  -> " + worker.getName() + " nie może znaleźć wolnego biurka. Czeka.");
            return;
        }

        // Pętla na maksymalnie 2 kroki w tej samej turze
        for (int i = 0; i < 2; i++) {
            int curX = worker.getX();
            int curY = worker.getY();
            int targetX = targetDesk.getX();
            int targetY = targetDesk.getY();

            // Jeśli dotarł na miejsce (w kroku 1 lub 2)
            if (curX == targetX && curY == targetY) {
                System.out.println("  -> " + worker.getName() + " usiadł przy swoim biurku.");
                worker.changeState(new WaitingForTaskState());
                return; // Przerywamy całą metodę act, bo cel został osiągnięty
            }

            int nextX = curX + Integer.compare(targetX, curX);
            int nextY = curY + Integer.compare(targetY, curY);

            if (board.moveAgent(curX, curY, nextX, nextY)) {
                worker.setX(nextX);
                worker.setY(nextY);
            } else {
                break; // Ściana lub inny agent – zatrzymaj się w tej turze
            }
        }

        // Ostateczne sprawdzenie stanu po wykonaniu pętli ruchów
        if (worker.getX() == targetDesk.getX() && worker.getY() == targetDesk.getY()) {
            System.out.println("  -> " + worker.getName() + " usiadł przy swoim biurku.");
            worker.changeState(new WaitingForTaskState());
        }
    }
}