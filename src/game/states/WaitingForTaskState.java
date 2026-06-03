package game.states;

import game.model.*;
import game.core.Simulation;

public class WaitingForTaskState implements WorkerState {

    @Override
    public void enter(Worker worker) {
        System.out.println("[STAN] " + worker.getName() + " siedzi przy biurku i czeka.");
    }

    @Override
    public void act(Worker worker, GameBoard board, Simulation sim) {
        // Sprawdzamy obecność Bossa raz na początku
        boolean isBossPresentNow = worker.isBossNeighbor(board);

        // 1. Obsługa Juniora (Boost motywacyjny i aktualizacja pamięci)
        if (worker instanceof Junior) {
            Junior junior = (Junior) worker;

            if (isBossPresentNow) {
                if (!junior.wasBossNeighborInPreviousTurn()) {
                    // Boss właśnie podszedł (w poprzedniej turze go tu nie było)
                    junior.setEfficiency(Math.min(1.0, junior.getEfficiency() + 0.10));
                    junior.setExperience(Math.min(1.0, junior.getExperience() + 0.20));
                    System.out.println("  -> [EVENT] Szef podszedł! Junior " + junior.getName()
                            + " dostał motywacyjnego boosta!");
                } else {
                    // Boss stoi tu już kolejną turę
                    System.out.println("  -> Junior " + junior.getName() + " pracuje w skupieniu pod czujnym okiem Szefa.");
                }
            }

            // ZAPISANIE STANU NA KOLEJNĄ TURĘ (Działa bez względu na to, czy Boss jest, czy odszedł)
            junior.setWasBossNeighborInPreviousTurn(isBossPresentNow);
        }

        // 2. Obsługa interakcji Seniora z Szefem
        if (isBossPresentNow && worker instanceof Senior) {
            worker.changeState(new TalkingState());
            return; // Kończymy akcję w tej turze, bo Senior zaczął rozmawiać
        }

        // 3. UNIKALNA LOGIKA SENIORA: Czy są błędy do naprawy?
        if (worker instanceof Senior && sim.getTotalFails() >= 1) {
            System.out.println("  -> " + worker.getName() + " (Senior) zauważył błędy Juniorów i idzie je naprawiać!");
            worker.changeState(new RepairingState(sim));
            return;
        }

        // 4. Sprawdzenie zmęczenia
        if (worker.getEfficiency() < 0.45) {
            worker.changeState(new MovingToRestState());
            return;
        }

        // 5. Rozpoczęcie zadania
        if (worker.hasTask()) {
            worker.changeState(new WorkingState());
        }
    }
}