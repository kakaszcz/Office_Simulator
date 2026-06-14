package game.states;

import game.agents.Worker;
import game.model.GameBoard;
import game.core.Simulation;

/**
 * Stan rozmowy z przełożonym (Talking State) realizujący wzorzec projektowy State.
 * Aktywowany w momencie, gdy Szef (Boss) inicjuje bezpośrednią interakcję z pracownikiem na planszy.
 * Stan ten rejestruje spotkanie w statystykach osobistych agenta, nakłada natychmiastową karę
 * zmęczenia wynikającą z rozproszenia uwagi (Context Switching) oraz czasowo blokuje
 * możliwość wykonywania innych akcji.
 */
public class TalkingState implements WorkerState {

    /** Czas trwania rozmowy wyrażony w liczbie tur (kroków symulacji). */
    private int talkingTurns = 1;

    /**
     * Logika wywoływana w momencie wejścia pracownika w stan rozmowy z Szefem.
     * Inkrementuje licznik odbytych rozmów z przełożonym w kartotece osobistej pracownika
     * oraz obniża jego bieżącą wydajność (Efficiency), dbając o dolny bezpiecznik wartości ($0.0$).
     *
     * @param worker Obiekt pracownika zaangażowanego w rozmowę.
     */
    @Override
    public void enter(Worker worker) {
        worker.recordBossTalk();
        worker.setEfficiency(Math.max(0.0, worker.getEfficiency() - 0.10));
        System.out.println("[STAN] Szef podszedł do Seniora. " + worker.getName() + " rozmawia z Szefem.");
    }

    /**
     * Aktualizuje czas trwania interakcji w każdej turze symulacji.
     * Po zakończeniu rozmowy (gdy talkingTurns osiągnie zero), automatycznie przywraca
     * pracownika do domyślnego stanu oczekiwania na nowe wyzwania projektowe (WaitingForTaskState).
     *
     * @param worker Pracownik rozmawiający z Szefem.
     * @param board Logiczna plansza gry.
     * @param sim Główny silnik symulacji.
     */
    @Override
    public void act(Worker worker, GameBoard board, Simulation sim) {
        talkingTurns--;

        System.out.println("  -> " + worker.getName() + " kończy rozmowę z Szefem (Eff: " + String.format("%.2f", worker.getEfficiency()) + ")");

        if (talkingTurns <= 0) {
            worker.changeState(new WaitingForTaskState());
        }
    }
}