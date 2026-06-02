package game.states;

import game.model.*;
import game.core.Simulation;
import game.core.GameConfiguration;

public class WaitingForTaskState implements WorkerState {

    @Override
    public void enter(Worker worker) {
        System.out.println("[STAN] " + worker.getName() + " siedzi przy biurku i czeka.");
    }

    @Override
    public void act(Worker worker, GameBoard board, Simulation sim) {
        // Sprawdzamy obecność Bossa (używając nowej, wspólnej metody z klasy Worker)
        boolean isBossPresentNow = worker.isBossNeighbor(board);

        // 1. Sprawdzamy interakcję z Szefem
        if (isBossPresentNow) {
            if (worker instanceof Senior) {
                worker.changeState(new TalkingState());
                return;
            } else if (worker instanceof Junior) {
                Junior junior = (Junior) worker;

                //Junior reaguje, gdy Boss podejdzie
                if (!junior.wasBossNeighborInPreviousTurn()) {
                    junior.setEfficiency(Math.min(1.0, junior.getEfficiency() + 0.10));
                    junior.setExperience(Math.min(1.0, junior.getExperience() + 0.20));

                    System.out.println("  -> [EVENT] Szef podszedł! Junior " + junior.getName()
                            + " dostał motywacyjnego boosta!");
                } else {
                    System.out.println("  -> Junior " + junior.getName() + " pracuje w skupieniu pod czujnym okiem Szefa.");
                }
            }

            if (worker instanceof Junior) {
                ((Junior) worker).setWasBossNeighborInPreviousTurn(isBossPresentNow);
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
}