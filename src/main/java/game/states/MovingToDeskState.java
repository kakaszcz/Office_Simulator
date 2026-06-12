package game.states;

import game.core.GameConfiguration;
import game.core.Simulation;
import game.agents.Worker;
import game.model.Cell;
import game.model.GameBoard;

public class MovingToDeskState implements WorkerState {

    private Cell targetDesk;
    private int searchCooldown = 0;
    private int blockedTurnsCount = 0; // Licznik frustracji pamiętany między turami

    @Override
    public void enter(Worker worker) {
        System.out.println("[STAN] " + worker.getName() + " zmierza do biurka.");
        this.blockedTurnsCount = 0;
    }

    @Override
    public void act(Worker worker, GameBoard board, Simulation sim) {

        Cell currentCell = board.getCell(worker.getX(), worker.getY());

        // 1. Sprawdzamy, czy fizycznie stoimy na WŁAŚCIWYM biurku
        if (targetDesk != null && currentCell.getX() == targetDesk.getX() && currentCell.getY() == targetDesk.getY()) {
            finalizeArrival(worker);
            return;
        }

        // Zabezpieczenie przed obciążaniem silnika, jeśli brakuje biurek
        if (searchCooldown > 0) {
            searchCooldown--;
            return;
        }

        // 2. Jeśli ktoś zajął nasze upatrzone biurko, szukamy nowego
        if (targetDesk != null && (!targetDesk.isEmpty() && targetDesk.getAgent() != worker)) {
            targetDesk = null;
        }

        // 3. Szukamy nowego biurka, jeśli nie mamy celu
        if (targetDesk == null) {
            targetDesk = board.findFirstEmptyCell(GameConfiguration.TILE_TYPE_DESK);

            if (targetDesk == null) {
                searchCooldown = 3;
                return;
            }
        }

        // 4. Wykonujemy ruch (wywołujemy Twoje standardowe navigateTo)
        int oldX = worker.getX();
        int oldY = worker.getY();

        worker.navigateTo(targetDesk, board);

        // 5. Sprawdzamy, czy się poruszyliśmy w tej turze
        if (worker.getX() == oldX && worker.getY() == oldY) {
            // Współrzędne się nie zmieniły = agent stoi w korku!
            blockedTurnsCount++;

            if (blockedTurnsCount > 4) {
                System.out.println("[INFO] " + worker.getName() + " utknął w drodze do biurka na 5 tur. Resetuje trasę!");
                targetDesk = null; // Porzuca obecne biurko, poszuka innego
                blockedTurnsCount = 0;
                return;
            }
        } else {
            // Ruch się udał, zerujemy licznik frustracji
            blockedTurnsCount = 0;
        }

        // 6. Sprawdzamy czy doszedł po wykonaniu kroku
        if (worker.getX() == targetDesk.getX() && worker.getY() == targetDesk.getY()) {
            finalizeArrival(worker);
        }
    }

    private void finalizeArrival(Worker worker) {
        System.out.println("  -> " + worker.getName() + " dotarł do biurka.");
        if (worker.hasTask()) {
            worker.changeState(new WorkingState());
        } else {
            worker.changeState(new WaitingForTaskState());
        }
    }
}