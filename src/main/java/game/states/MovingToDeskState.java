package game.states;

import game.core.GameConfiguration;
import game.core.Simulation;
import game.agents.Worker;
import game.model.Cell;
import game.model.GameBoard;
import game.model.PathFinder;
import java.util.List;
import java.util.Random;

public class MovingToDeskState implements WorkerState {

    private Cell targetDesk;
    private List<Cell> path;
    private final PathFinder pathFinder = new PathFinder();
    private int blockedTurnsCount = 0;
    private int pathfindingCooldown = 0;
    private static final Random RANDOM = new Random();

    @Override
    public void enter(Worker worker) {
        System.out.println("[STAN] " + worker.getName() + " zmierza do biurka.");
    }

    @Override
    public void act(Worker worker, GameBoard board, Simulation sim) {

        Cell currentCell = board.getCell(worker.getX(), worker.getY());

        // POPRAWIONY BEZPIECZNIK: Sprawdzamy czy stoi na WŁAŚCIWYM biurku (swoim celu)
        if (targetDesk != null && currentCell.getX() == targetDesk.getX() && currentCell.getY() == targetDesk.getY()) {
            targetDesk.setReserved(false);
            targetDesk = null;
            path = null;

            if (worker.hasTask()) {
                worker.changeState(new WorkingState());
            } else {
                worker.changeState(new WaitingForTaskState());
            }
            return;
        }

        if (pathfindingCooldown > 0) {
            pathfindingCooldown--;
            return;
        }

        if (targetDesk != null && (!targetDesk.isEmpty() && targetDesk.getAgent() != worker)) {
            targetDesk.setReserved(false);
            targetDesk = null;
            path = null;
        }

        if (targetDesk == null) {
            // Szukamy najbliższego/pierwszego wolnego biurka
            targetDesk = board.findFirstEmptyCell(GameConfiguration.TILE_TYPE_DESK);
            path = null;
        }

        if (targetDesk == null) {
            pathfindingCooldown = 4;
            return;
        }

        if (path == null) {
            path = pathFinder.findPath(currentCell, targetDesk, board);

            if (path == null || path.isEmpty()) {
                if (targetDesk != null) targetDesk.setReserved(false);
                targetDesk = null;
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

                    if (blockedTurnsCount > 5) {
                        path = null; // Wymuszamy rekalkulację objazdu
                        blockedTurnsCount = 0;
                        pathfindingCooldown = 2;
                        tryDodgeStep(worker, board, nextStep);
                    }
                    break;
                }

                if (worker.getX() == targetDesk.getX() && worker.getY() == targetDesk.getY()) {
                    reachedDestination = true;
                    break;
                }
            } else {
                if (worker.getX() == targetDesk.getX() && worker.getY() == targetDesk.getY()) {
                    reachedDestination = true;
                }
                break;
            }
        }

        if (reachedDestination) {
            if (targetDesk != null) targetDesk.setReserved(false);
            System.out.println("  -> " + worker.getName() + " dotarł do biurka.");
            if (worker.hasTask()) {
                worker.changeState(new WorkingState());
            } else {
                worker.changeState(new WaitingForTaskState());
            }
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