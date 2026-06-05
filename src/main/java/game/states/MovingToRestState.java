// In game/states/MovingToRestState.java
package game.states;

import game.core.Simulation;
import game.model.Worker;
import game.model.Cell;
import game.model.GameBoard;

import java.util.Random;

public class MovingToRestState implements WorkerState {

    private final Random random = new Random();
    private String destinationType; // "coffee" lub "outside"
    private Cell targetCell;

    @Override
    public void enter(Worker worker) {
        // Losowanie 50% na 50%
        if (random.nextBoolean()) {
            this.destinationType = "coffee";
            System.out.println("[STAN] " + worker.getName() + " poczuł zmęczenie. Postanawia iść do kuchni na kawę.");
        } else {
            this.destinationType = "outside";
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

        // Pętla wykona się maksymalnie 2 razy, aby zachować prędkość 2 kafelków na turę.
        for (int i = 0; i < 2; i++) {
            // Wykonaj krok z użyciem inteligentnego pathfinding
            worker.navigateTo(targetCell, board);

            // Sprawdź czy po wykonaniu tego kroku pracownik jest już na miejscu
            if (worker.getX() == targetCell.getX() && worker.getY() == targetCell.getY()) {
                reachedDestination = true;
                break; // Przerwij pętlę, nie rób drugiego kroku.
            }
        }

        //dodane zeby jak dojdzie do kawy to dodawac do statystyk
        if (reachedDestination) {
            if (destinationType.equals("coffee")) {
                sim.recordCoffeeDrunk();
            }
            // Dotarł na miejsce! Przechodzi w stan właściwego odpoczynku
            worker.changeState(new RestingState(destinationType));
        }
    }
}