package game.states;

import game.core.GameConfiguration;
import game.core.Simulation;
import game.agents.Worker;
import game.model.Cell;
import game.model.GameBoard;
import game.model.PathFinder;
import java.util.List;
import java.util.Random;

public class MovingToRestState implements WorkerState {

    private static final Random RANDOM = new Random();
    private String destinationType;
    private Cell targetCell;
    private List<Cell> path;
    private final PathFinder pathFinder = new PathFinder();
    private int blockedTurnsCount = 0;
    private int pathfindingCooldown = 0;

    @Override
    public void enter(Worker worker) {
        if (RANDOM.nextBoolean()) {
            this.destinationType = GameConfiguration.TILE_TYPE_COFFEE;
        } else {
            this.destinationType = GameConfiguration.TILE_TYPE_OUTSIDE;
        }
    }

    @Override
    public void act(Worker worker, GameBoard board, Simulation sim) {
        if (pathfindingCooldown > 0) {
            pathfindingCooldown--;
            return;
        }

        if (targetCell != null && (!targetCell.isEmpty() && targetCell.getAgent() != worker)) {
            targetCell.setReserved(false);
            targetCell = null;
            path = null;
        }

        if (targetCell == null) {
            targetCell = board.findFirstEmptyCell(destinationType);
            path = null;
        }

        if (targetCell == null) {
            pathfindingCooldown = 4;
            return;
        }

        if (path == null) {
            Cell currentCell = board.getCell(worker.getX(), worker.getY());
            path = pathFinder.findPath(currentCell, targetCell, board);

            if (path == null || path.isEmpty()) {
                if (targetCell != null) targetCell.setReserved(false);
                targetCell = null;
                pathfindingCooldown = 6;
                return;
            }
        }

        boolean reachedDestination = false;
        int steps = GameConfiguration.WORKER_MOVE_STEPS_PER_TURN;

        for (int i = 0; i < steps; i++) {
            if (path != null && !path.isEmpty()) {
                Cell nextStep = path.remove(0);
                int oldX = worker.getX();
                int oldY = worker.getY();

                if (board.moveAgent(oldX, oldY, nextStep.getX(), nextStep.getY())) {
                    worker.setX(nextStep.getX());
                    worker.setY(nextStep.getY());
                    blockedTurnsCount = 0;
                } else {
                    path.add(0, nextStep);
                    blockedTurnsCount++;

                    // Większa cierpliwość w drodze na przerwę
                    if (blockedTurnsCount > 5) {
                        path = null; // Szukamy objazdu do tej samej strefy odpoczynku
                        blockedTurnsCount = 0;
                        pathfindingCooldown = 2;
                        tryDodgeStep(worker, board, nextStep);
                    }
                    break;
                }

                if (worker.getX() == targetCell.getX() && worker.getY() == targetCell.getY()) {
                    reachedDestination = true;
                    break;
                }
            } else {
                if (worker.getX() == targetCell.getX() && worker.getY() == targetCell.getY()) {
                    reachedDestination = true;
                }
                break;
            }
        }

        if (reachedDestination) {
            if (targetCell != null) targetCell.setReserved(false);
            worker.changeState(new RestingState(destinationType));
        }
    }

    private void tryDodgeStep(Worker worker, GameBoard board, Cell blockedCell) {
        int oldX = worker.getX();
        int oldY = worker.getY();
        int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
        for (int[] dir : directions) {
            int dX = oldX + dir[0];
            int dY = oldY + dir[1];
            if ((dX != blockedCell.getX() || dY != blockedCell.getY()) && board.moveAgent(oldX, oldY, dX, dY)) {
                worker.setX(dX);
                worker.setY(dY);
                return;
            }
        }
    }
}