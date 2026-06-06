package game.states;

import game.agents.Junior;
import game.agents.Senior;
import game.agents.Worker;
import game.core.GameConfiguration;
import game.model.GameBoard;
import game.core.Simulation;

public class WaitingForTaskState implements WorkerState {

    @Override
    public void enter(Worker worker) {
        System.out.println("[STAN] " + worker.getName() + " siedzi przy biurku i czeka.");
    }

    @Override
    public void act(Worker worker, GameBoard board, Simulation sim) {

        boolean isBossPresentNow = worker.isBossNeighbor(sim);

        // 1. Obsługa Juniora (Boost motywacyjny i aktualizacja pamięci)
        if (worker instanceof Junior) {
            Junior junior = (Junior) worker;

            if (isBossPresentNow) {
                if (!junior.wasBossNeighborInPreviousTurn()) {
                    // Boss właśnie podszedł (w poprzedniej turze go tu nie było)
                    // REFAKTOR: Zastąpienie surowych liczb stałymi konfiguracyjnymi
                    double newEff = junior.getEfficiency() + GameConfiguration.BOSS_MOTIVATION_EFFICIENCY_BOOST;
                    double newExp = junior.getExperience() + GameConfiguration.BOSS_MOTIVATION_EXPERIENCE_BOOST;

                    junior.setEfficiency(Math.min(1.0, newEff));
                    junior.setExperience(Math.min(1.0, newExp));

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
        if (worker instanceof Senior && sim.getTotalFails() > 0) {
            System.out.println("  -> " + worker.getName() + " (Senior) zauważył błędy Juniorów i idzie je naprawiać!");
            // PRZYPOMNIENIE: Konstruktor zostaje w pełni zachowany tak jak w Twoim kodzie!
            worker.changeState(new RepairingState(sim));
            return;
        }

        if (worker.getEfficiency() < GameConfiguration.EFFICIENCY_REST_THRESHOLD) {
            // Jeśli miał przypisane zadanie, anulujemy je/oddajemy,
            // żeby nie poszedł z nim pić kawy i nie zablokował systemu.
            if (worker.hasTask()) {
                worker.setHasTask(false);
            }

            worker.changeState(new MovingToRestState());
            return;
        }

        if (worker.hasTask()) {
            worker.changeState(new WorkingState());
            return;
        }
    }
}