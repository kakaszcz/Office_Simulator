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

    @Override
    public void enter(Worker worker) {
        if (RANDOM.nextBoolean()) {
            this.destinationType = GameConfiguration.TILE_TYPE_COFFEE;
            System.out.println("[STAN] " + worker.getName() + " poczuł zmęczenie. Postanawia iść do kuchni na kawę.");
        } else {
            this.destinationType = GameConfiguration.TILE_TYPE_OUTSIDE;
            System.out.println("[STAN] " + worker.getName() + " poczuł zmęczenie. Postanawia wyjść na dwór zaczerpnąć powietrza.");
        }
    }

    @Override
    public void act(Worker worker, GameBoard board, Simulation sim) {
        // Jeśli jeszcze nie znaleźliśmy konkretnej komórki docelowej na planszy, szukamy jej
        if (targetCell == null) {
            targetCell = board.findFirstEmptyCell(destinationType);
        }

        if (targetCell == null) {
            System.out.println("  -> " + worker.getName() + " nie może znaleźć wolnego miejsca typu: " + destinationType + ". Czeka.");
            return;
        }

        boolean reachedDestination = false;

        int steps = GameConfiguration.WORKER_MOVE_STEPS_PER_TURN;
        for (int i = 0; i < steps; i++) {
            // Wykonaj krok z użyciem inteligentnego pathfinding
            worker.navigateTo(targetCell, board);

            // Sprawdź czy po wykonaniu tego kroku pracownik jest już na miejscu
            if (worker.getX() == targetCell.getX() && worker.getY() == targetCell.getY()) {
                reachedDestination = true;
                break; // Przerwij pętlę, nie rób drugiego kroku.
            }
        }

        // Dodane zeby jak dojdzie do kawy to dodawac do statystyk
        if (reachedDestination) {
            if (GameConfiguration.TILE_TYPE_COFFEE.equals(destinationType)) {
                sim.recordCoffeeDrunk();
            }
            // Dotarł na miejsce! Przechodzi w stan właściwego odpoczynku
            worker.changeState(new RestingState(destinationType));
        }
    }
}