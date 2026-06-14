package game.states;

import game.agents.Worker;
import game.model.GameBoard;
import game.core.Simulation;

/**
 * Stan wściekłości/wzburzenia (Mad State) realizujący wzorzec projektowy State.
 * Jest to stan przejściowy o charakterze wizualno-animacyjnym. Agenci (najczęściej Seniorzy)
 * wprowadzani są w ten stan automatycznie przez silnik symulacji w momencie wystąpienia
 * awarii krytycznej (Fatal Error). Stan ten czasowo zawiesza standardowe obowiązki
 * pracownika na rzecz odegrania animacji gniewu.
 */
public class MadState implements WorkerState {

    /** Liczba tur (kroków symulacji), przez które odgrywana jest wizualna animacja złości. */
    private int madTurns = 2;

    /**
     * Logika wywoływana w momencie wejścia agenta w stan wściekłości.
     * Rejestruje zdarzenie w logach systemowych.
     *
     * @param worker Obiekt pracownika, który został wprowadzony w stan wzburzenia.
     */
    @Override
    public void enter(Worker worker) {
        System.out.println("[STAN] " + worker.getName() + " odgrywa animację wściekłości!");
    }

    /**
     * Aktualizuje licznik czasu trwania animacji w każdej turze symulacji.
     * Po upłynięciu zaplanowanych tur złości (madTurns), automatycznie przywraca
     * pracownika do domyślnego stanu oczekiwania na nowe zadania projektowe.
     *
     * @param worker Pracownik przechodzący przez fazę wzburzenia.
     * @param board Logiczna plansza gry.
     * @param sim Główny silnik symulacji zarządzający zdarzeniami w biurze.
     */
    @Override
    public void act(Worker worker, GameBoard board, Simulation sim) {
        madTurns--;
        System.out.println("  -> " + worker.getName() + " wścieka się wizualnie... Zostało tur: " + madTurns);

        if (madTurns <= 0) {
            System.out.println("  -> " + worker.getName() + " kończy animację złości i wraca do pracy.");
            worker.changeState(new WaitingForTaskState());
        }
    }
}