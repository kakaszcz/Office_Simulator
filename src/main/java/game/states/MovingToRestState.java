package game.states;

import game.core.GameConfiguration;
import game.core.Simulation;
import game.agents.Worker;
import game.model.Cell;
import game.model.GameBoard;
import java.util.Random;

public class MovingToRestState implements WorkerState {

    private static final Random RANDOM = new Random();
    private String destinationType;
    private Cell targetCell;
    private int searchCooldown = 0;

    @Override
    public void enter(Worker worker) {
        // Zawsze ustawiamy, dokąd idzie (Kawa czy Fajka)
        if (RANDOM.nextBoolean()) {
            this.destinationType = GameConfiguration.TILE_TYPE_COFFEE;
        } else {
            this.destinationType = GameConfiguration.TILE_TYPE_OUTSIDE;
        }
        System.out.println("[STAN] " + worker.getName() + " poczuł zmęczenie. Zmierza do: " + destinationType);
    }

    @Override
    public void act(Worker worker, GameBoard board, Simulation sim) {

        // Zabezpieczenie przed ciągłym obciążaniem PathFindera, jeśli kuchnia/dwór są pełne
        if (searchCooldown > 0) {
            searchCooldown--;
            return;
        }

        // 1. Sprawdzamy, czy musimy poszukać nowego wolnego miejsca
        if (targetCell == null || (!targetCell.isEmpty() && targetCell.getAgent() != worker)) {
            targetCell = board.findFirstEmptyCell(destinationType);

            if (targetCell == null) {
                // Brak wolnych miejsc w kuchni/na zewnątrz! Czekamy 3 tury w miejscu.
                searchCooldown = 3;
                return;
            }
        }

        // 2. Poruszamy się używając wbudowanej, sprawdzonej metody pracownika
        int steps = GameConfiguration.WORKER_MOVE_STEPS_PER_TURN;
        for (int i = 0; i < steps; i++) {

            // Jeśli dotarliśmy do celu, przerywamy ruch i zmieniamy stan na Odpoczynek
            if (worker.getX() == targetCell.getX() && worker.getY() == targetCell.getY()) {
                worker.changeState(new RestingState(destinationType));
                return;
            }

            // Krok w stronę wybranego miejsca
            worker.navigateTo(targetCell, board);

            // Sprawdzamy warunek dojścia jeszcze raz po wykonaniu kroku
            if (worker.getX() == targetCell.getX() && worker.getY() == targetCell.getY()) {
                worker.changeState(new RestingState(destinationType));
                return;
            }
        }
    }
}