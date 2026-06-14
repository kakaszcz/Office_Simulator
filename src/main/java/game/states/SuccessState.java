package game.states;

import game.agents.Worker;
import game.model.GameBoard;
import game.core.Simulation;

/**
 * Stan świętowania sukcesu (Success State) realizujący wzorzec projektowy State.
 * Jest to stan przejściowy o charakterze motywacyjno-animacyjnym. Pracownik wprowadzany
 * jest w ten stan natychmiast po pomyślnym ukończeniu i oddaniu zadania programistycznego.
 * Wejście w stan aplikuje natychmiastową nagrodę w postaci przyrostu doświadczenia oraz
 * wydajności, a sam stan blokuje agenta na dwie tury w celu odegrania animacji radości.
 */
public class SuccessState implements WorkerState {

    /** Liczba tur (kroków symulacji), przez które odgrywana jest wizualna animacja świętowania. */
    private int successTurns = 2;

    /**
     * Logika wywoływana w momencie wejścia pracownika w stan sukcesu.
     * Zgodnie ze specyfikacją diagramu stanów, natychmiastowo inkrementuje poziom
     * doświadczenia (Experience) oraz wydajności (Efficiency) agenta, zabezpieczając
     * obie wartości przed przekroczeniem maksymalnego progu.
     *
     * @param worker Obiekt pracownika, który pomyślnie ukończył zadanie.
     */
    @Override
    public void enter(Worker worker) {
        worker.setExperience(Math.min(1.0, worker.getExperience() + 0.02));
        worker.setEfficiency(Math.min(1.0, worker.getEfficiency() + 0.05));

        System.out.println("[STAN] " + worker.getName() + " świętuje udane zadanie! 🎉 (Animacja sukcesu)");
    }

    /**
     * Aktualizuje licznik czasu trwania animacji radości w każdej turze symulacji.
     * Po upłynięciu zaplanowanego czasu świętowania (successTurns), automatycznie
     * przywraca pracownika do stanu gotowości zawodowej (WaitingForTaskState).
     *
     * @param worker Pracownik przechodzący przez fazę świętowania.
     * @param board Logiczna plansza gry.
     * @param sim Główny silnik symulacji zarządzający globalnym stanem biura.
     */
    @Override
    public void act(Worker worker, GameBoard board, Simulation sim) {
        successTurns--;
        System.out.println("  -> " + worker.getName() + " cieszy się z sukcesu. Zostało tur animacji: " + successTurns);

        if (successTurns <= 0) {
            worker.changeState(new WaitingForTaskState());
        }
    }
}