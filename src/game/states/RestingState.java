package game.states;

import game.core.Simulation;
import game.model.Worker;
import game.core.GameConfiguration;
import game.model.Boss;
import game.model.Cell;
import game.model.GameBoard;

public class RestingState implements WorkerState {

    private final String restPlaceType;
    private int restTurnsRemaining = 3; // Odpoczynek trwa np. 3 tury

    public RestingState(String restPlaceType) {
        this.restPlaceType = restPlaceType;
    }

    @Override
    public void enter(Worker worker) {
        System.out.println("[STAN] " + worker.getName() + " rozpoczął odpoczynek w strefie: " + restPlaceType);
    }

    @Override
    public void act(Worker worker, GameBoard board, Simulation sim) {
        // 1. Unikalna zasada z diagramu: przyłapanie na dworze przez szefa
        if (restPlaceType.equals("outside") && isBossNeighbor(worker, board)) {
            worker.markFired(); // Flagujemy pracownika do wywalenia z firmy!
            System.out.println("!!! SKANDAL! Szef przyłapał pracownika " + worker.getName() + " na obijaniu się na dworze!");
        }

        // Co turę odpoczynku regeneruje się wydajność (np. o +20%)
        worker.setEfficiency(Math.min(1.0, worker.getEfficiency() + 0.20));
        restTurnsRemaining--;

        System.out.println("  -> " + worker.getName() + " odpoczywa... (Aktualna wydajność: "
                + String.format("%.2f", worker.getEfficiency()) + ")");

        // Koniec odpoczynku
        if (restTurnsRemaining <= 0) {
            System.out.println("  -> " + worker.getName() + " odpoczął i wraca do pracy.");
            worker.changeState(new MovingToDeskState());
        }
    }

    private boolean isBossNeighbor(Worker worker, GameBoard board) {
        int x = worker.getX();
        int y = worker.getY();
        int[][] neighbors = {{x+1, y}, {x-1, y}, {x, y+1}, {x, y-1}};
        for (int[] pos : neighbors) {
            if (pos[0] >= 0 && pos[0] < GameConfiguration.MAP_WIDTH && pos[1] >= 0 && pos[1] < GameConfiguration.MAP_HEIGHT) {
                Cell cell = board.getCell(pos[0], pos[1]);
                if (cell != null && cell.getAgent() instanceof Boss) {
                    return true;
                }
            }
        }
        return false;
    }
}