package game.states;

import game.model.Worker;
import game.model.Senior;
import game.model.Boss;
import game.model.Cell;
import game.model.GameBoard;
import game.core.Simulation;
import game.core.GameConfiguration;

public class WaitingForTaskState implements WorkerState {

    @Override
    public void enter(Worker worker) {
        System.out.println("[STAN] " + worker.getName() + " siedzi przy biurku i czeka.");
    }

    @Override
    public void act(Worker worker, GameBoard board, Simulation sim) {
        // 1. Sprawdzamy interakcję z Szefem
        if (isBossNeighbor(worker, board)) {
            if (worker instanceof Senior) {
                // Senior rozmawia z szefem (Zgodnie z diagramem Seniora)
                worker.changeState(new TalkingState());
                return;
            } else {
                // Junior się stresuje (Zgodnie z diagramem Juniora)
                worker.setEfficiency(Math.max(0.0, worker.getEfficiency() - 0.10));
                worker.setExperience(Math.min(1.0, worker.getExperience() + 0.20));
                System.out.println("  -> Junior " + worker.getName() + " stresuje się przy szefie!");
            }
        }

        // 2. UNIKALNA LOGIKA SENIORA: Czy są błędy do naprawy? (fails >= 1)
        if (worker instanceof Senior && sim.getTotalFails() >= 1) {
            System.out.println("  -> " + worker.getName() + " (Senior) zauważył błędy Juniorów i idzie je naprawiać!");
            worker.changeState(new RepairingState());
            return;
        }

        // 3. Sprawdzenie zmęczenia (Wspólne dla obu)
        if (worker.getEfficiency() < 0.45) {
            worker.changeState(new MovingToRestState());
            return;
        }

        // 4. Jeśli ma zadanie i jest Juniorem (lub Seniorem bez błędów globalnych)
        if (worker.hasTask()) {
            worker.changeState(new WorkingState());
        }
    }

    private boolean isBossNeighbor(Worker worker, GameBoard board) {
        int x = worker.getX();
        int y = worker.getY();
        int[][] neighbors = {{x + 1, y}, {x - 1, y}, {x, y + 1}, {x, y - 1}};

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