package game.states;

import game.core.Simulation;
import game.model.Worker;
import game.model.Cell;
import game.model.GameBoard;

public class MovingToDeskState implements WorkerState {

    @Override
    public void enter(Worker worker) {
        System.out.println("[STAN] " + worker.getName() + " szuka wolnego biurka, aby wrócić do pracy.");
    }

    @Override
    public void act(Worker worker, GameBoard board, Simulation sim) {
        Cell deskCell = board.findFirstEmptyCell("desk");

        if (deskCell != null) {
            // Czyszczenie starej pozycji
            board.getCell(worker.getX(), worker.getY()).setAgent(null);

            // Przypisanie nowej pozycji przy biurku
            worker.setX(deskCell.getX());
            worker.setY(deskCell.getY());
            deskCell.setAgent(worker);

            System.out.println("  -> " + worker.getName() + " usiadł przy swoim biurku.");
            worker.changeState(new WaitingForTaskState());
        }
    }
}