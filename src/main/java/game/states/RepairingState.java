package game.states;

import game.agents.Worker;
import game.model.GameBoard;
import game.core.Simulation;

/**
 * Stan naprawiania błędów systemowych (Repairing State) realizujący wzorzec projektowy State.
 * Dedykowany dla zaawansowanych pracowników (Seniorów), którzy podejmują akcję kryzysową
 * w celu eliminacji awarii zarejestrowanych w silniku głównym.
 * Stan nakłada koszt energetyczny na pracownika i blokuje go na czas wyliczony algorytmicznie.
 */
public class RepairingState implements WorkerState {

    /** Liczba tur pozostałych do pełnego usunięcia awarii z systemu. */
    private int repairTimeRemaining;

    /** Referencja do silnika symulacji, niezbędna do raportowania usunięcia awarii. */
    private final Simulation sim;

    /**
     * Tworzy nową instancję stanu naprawczego z powiązaniem do silnika symulacji.
     *
     * @param sim Instancja głównego silnika symulacji.
     */
    public RepairingState(Simulation sim) {
        this.sim = sim;
    }

    /**
     * Logika wywoływana w momencie wejścia pracownika w stan naprawy awarii.
     * Dynamicznie oblicza czas trwania prac na podstawie statystyk osobistych Seniora,
     * po czym redukuje jego wydajność (Efficiency) o stały koszt operacyjny, dbając o dolny bezpiecznik wartości.
     *
     * @param worker Obiekt pracownika (Seniora) podejmującego się naprawy.
     */
    @Override
    public void enter(Worker worker) {
        this.repairTimeRemaining = worker.computeTaskTime();
        worker.setEfficiency(Math.max(0.0, worker.getEfficiency() - 0.10));
        System.out.println("[STAN] Senior " + worker.getName() + " rozpoczyna naprawianie bugów. Zajmie to: " + repairTimeRemaining + " tur.");
    }

    /**
     * Dekrementuje licznik czasu naprawy w każdym kroku symulacji.
     * W momencie osiągnięcia zera następuje oficjalna finalizacja procesu: usunięcie błędu
     * z rejestru centralnego oraz ewaluacja zmęczenia w celu podjęcia decyzji o regeneracji
     * (MovingToRestState) lub powrocie do puli dostępnych zasobów ludzkich (WaitingForTaskState).
     *
     * @param worker Pracownik wykonujący akcję naprawczą.
     * @param board Logiczna plansza gry.
     * @param sim Główny silnik symulacji zarządzający rejestrem błędów.
     */
    @Override
    public void act(Worker worker, GameBoard board, Simulation sim) {
        repairTimeRemaining--;

        System.out.println("  -> " + worker.getName() + " naprawia... Pozostało tur: " + repairTimeRemaining);

        if (repairTimeRemaining <= 0) {
            worker.recordBugRepaired();
            sim.repairFail();
            System.out.println("  -> " + worker.getName() + " pomyślnie naprawił błąd w projekcie!");

            // Decyzja o dalszym zachowaniu na podstawie progu kondycyjnego
            if (worker.getEfficiency() < 0.45) {
                worker.changeState(new MovingToRestState());
            } else {
                worker.changeState(new WaitingForTaskState());
            }
        }
    }
}