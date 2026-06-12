package game.states;

import game.agents.Junior;
import game.agents.Senior;
import game.agents.Worker;
import game.core.GameConfiguration;
import game.model.GameBoard;
import game.core.Simulation;

public class WaitingForTaskState implements WorkerState {

    private int idleTicks = 0;

    @Override
    public void enter(Worker worker) {
        this.idleTicks = 0;
        System.out.println("[STAN] " + worker.getName() + " siedzi przy biurku i czeka.");
    }

    @Override
    public void act(Worker worker, GameBoard board, Simulation sim) {

        if (worker.hasTask()) {
            worker.changeState(new WorkingState());
            return;
        }

        boolean isBossPresentNow = worker.isBossNeighbor(sim);

        // 1. Obsługa Juniora (TYLKO boost motywacyjny, zero darmowego expa!)
        if (worker instanceof Junior) {
            Junior junior = (Junior) worker;

            if (isBossPresentNow) {
                if (!junior.wasBossNeighborInPreviousTurn()) {
                    double newEff = junior.getEfficiency() + GameConfiguration.BOSS_MOTIVATION_EFFICIENCY_BOOST;

                    junior.setEfficiency(Math.min(1.0, newEff));

                    System.out.println("  -> [EVENT] Szef podszedł! Junior " + junior.getName()
                            + " dostał motywacyjnego boosta do wydajności!");
                } else {
                    System.out.println("  -> Junior " + junior.getName() + " pracuje w skupieniu pod czujnym okiem Szefa.");
                }
            }
            junior.setWasBossNeighborInPreviousTurn(isBossPresentNow);
        }

        // 2. Obsługa interakcji Seniora z Szefem
        if (isBossPresentNow && worker instanceof Senior) {
            worker.changeState(new TalkingState());
            return;
        }

        // 3. UNIKALNA LOGIKA SENIORA
        if (worker instanceof Senior && sim.getTotalFails() > 0) {
            System.out.println("  -> " + worker.getName() + " (Senior) zauważył błędy Juniorów i idzie je naprawiać!");
            worker.changeState(new RepairingState(sim));
            return;
        }

        // 4. Sprawdzenie zmęczenia
        if (worker.getEfficiency() < GameConfiguration.EFFICIENCY_REST_THRESHOLD) {
            if (worker.hasTask()) {
                worker.setHasTask(false);
            }
            worker.changeState(new MovingToRestState());
            return;
        }

        // 5. INKREMENTACJA LICZNIKA
        idleTicks++;

        if (idleTicks > 30) {
            System.out.println("  -> " + worker.getName() + " nudzi się brakiem zadań, idzie rozprostować nogi.");
            worker.changeState(new MovingToRestState());
            return;
        }
    }
}