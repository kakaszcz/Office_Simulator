package game.states;

import game.agents.Junior;
import game.agents.Senior;
import game.agents.Worker;
import game.core.GameConfiguration;
import game.model.GameBoard;
import game.core.Simulation;

/**
 * Stan oczekiwania na zadanie (Waiting For Task State) realizujący wzorzec projektowy State.
 * Jest to podstawowy, pasywny stan behawioralny pracownika siedzącego przy biurku.
 * Klasa stanowi główny węzeł decyzyjny (Decision Matrix), przekierowujący agenta do innych akcji
 * (praca, odpoczynek, rozmowa, naprawa błędów) w zależności od jego roli (Junior/Senior),
 * poziomu zmęczenia oraz bliskości Szefa.
 */
public class WaitingForTaskState implements WorkerState {

    /** Licznik tur spędzonych bezczynnie na oczekiwaniu na przypisanie zadania z managera. */
    private int idleTicks = 0;

    /**
     * Inicjalizuje stan oczekiwania i resetuje licznik bezczynności.
     *
     * @param worker Obiekt pracownika wchodzącego w fazę oczekiwania.
     */
    @Override
    public void enter(Worker worker) {
        this.idleTicks = 0;
        System.out.println("[STAN] " + worker.getName() + " siedzi przy biurku i czeka.");
    }

    /**
     * Wykonuje zaawansowaną macierz decyzyjną sprawdzającą priorytety zachowań w każdej turze.
     * Kolejność ewaluacji warunków:
     * 1. Rozpoczęcie pracy (jeśli przydzielono zadanie).
     * 2. Obsługa stymulacji przełożonego (boost dla Juniora / rozmowa dla Seniora).
     * 3. Reakcja na awarie w projekcie (wyłącznie dla Seniora).
     * 4. Weryfikacja krytycznego zmęczenia.
     * 5. Monitoring nudy (Anti-idle protection).
     *
     * @param worker Pracownik oczekujący na akcję w bieżącej turze.
     * @param board Logiczna plansza gry.
     * @param sim Główny silnik symulacji zarządzający zdarzeniami globalnymi.
     */
    @Override
    public void act(Worker worker, GameBoard board, Simulation sim) {

        // 1. Priorytet nadrzędny: Podjęcie pracy w przypadku pojawienia się zadania
        if (worker.hasTask()) {
            worker.changeState(new WorkingState());
            return;
        }

        boolean isBossPresentNow = worker.isBossNeighbor(sim);

        // 2. Obsługa logiki dedykowanej dla klasy Junior (Efekt motywacyjny Szefa)
        if (worker instanceof Junior) {
            Junior junior = (Junior) worker;

            if (isBossPresentNow) {
                // Wyzwolenie jednorazowe: aplikowane tylko w momencie wejścia Szefa w sąsiedztwo
                if (!junior.wasBossNeighborInPreviousTurn()) {
                    double oldEff = junior.getEfficiency();
                    double boost = GameConfiguration.BOSS_MOTIVATION_EFFICIENCY_BOOST;
                    double newEff = Math.min(1.0, oldEff + boost);

                    junior.setEfficiency(newEff);
                    junior.recordBossBoost();

                    if (newEff > oldEff) {
                        System.out.println("  -> [BOOST] Junior " + junior.getName()
                                + " otrzymał boost od Szefa: +"
                                + String.format("%.0f", (newEff - oldEff) * 100)
                                + " pkt wydajności (Eff: "
                                + String.format("%.2f", oldEff)
                                + " -> "
                                + String.format("%.2f", newEff)
                                + ").");
                    } else {
                        System.out.println("  -> [BOOST] Junior " + junior.getName()
                                + " otrzymał boost od Szefa, ale wydajność jest już maksymalna.");
                    }
                } else {
                    System.out.println("  -> Junior " + junior.getName()
                            + " utrzymuje boost od Szefa i pracuje w skupieniu.");
                }
            }

            junior.setWasBossNeighborInPreviousTurn(isBossPresentNow);
        }

        // 3. Obsługa logiki dedykowanej dla klasy Senior (Interakcja blokująca z Szefem)
        if (isBossPresentNow && worker instanceof Senior) {
            worker.changeState(new TalkingState());
            return;
        }

        // 4. Obsługa logiki dedykowanej dla klasy Senior (Detekcja i usuwanie awarii/bugów)
        if (worker instanceof Senior && sim.getTotalFails() > 0) {
            System.out.println("  -> " + worker.getName() + " (Senior) zauważył błędy Juniorów i idzie je naprawiać!");
            worker.changeState(new RepairingState(sim));
            return;
        }

        // 5. System ewaluacji energetycznej (Przekierowanie na odpoczynek przy wycieńczeniu)
        if (worker.getEfficiency() < GameConfiguration.EFFICIENCY_REST_THRESHOLD) {
            if (worker.hasTask()) {
                worker.setHasTask(false);
            }
            worker.changeState(new MovingToRestState());
            return;
        }

        // 6. Inkrementacja i kontrola systemu anty-nudowego (Anti-idle timeout protective system)
        idleTicks++;

        if (idleTicks > 30) {
            System.out.println("  -> " + worker.getName() + " nudzi się brakiem zadań, idzie rozprostować nogi.");
            worker.changeState(new MovingToRestState());
            return;
        }
    }
}