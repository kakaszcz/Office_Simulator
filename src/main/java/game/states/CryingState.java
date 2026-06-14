package game.states;

import game.core.GameConfiguration;
import game.core.Simulation;
import game.model.GameBoard;
import game.agents.Worker;

/**
 * Stan załamania i płaczu pracownika (Crying State) realizujący wzorzec projektowy State.
 * Juniorzy przechodzą w ten stan po popełnieniu krytycznego błędu.
 * Wejście w stan skutkuje natychmiastowym, drastycznym spadkiem wydajności, a sam stan blokuje
 * możliwość wykonywania jakichkolwiek zadań przez określoną konfiguracją liczbę tur.
 */
public class CryingState implements WorkerState {

    /** Licznik tur pozostających do zakończenia fazy załamania nerwowego. */
    private int cryTurnsRemaining;

    /**
     * Inicjalizuje stan płaczu dla określonego pracownika.
     * Ustawia czas trwania blokady na podstawie konfiguracji globalnej oraz obniża
     * wydajność (Efficiency) agenta, zabezpieczając jej wartość przed spadkiem poniżej zera.
     *
     * @param worker Obiekt pracownika, który wchodzi w stan załamania.
     */
    @Override
    public void enter(Worker worker) {
        this.cryTurnsRemaining = GameConfiguration.CRY_DURATION_TURNS;

        double newEfficiency = worker.getEfficiency() - GameConfiguration.CRYING_EFFICIENCY_DROP;
        worker.setEfficiency(Math.max(0.0, newEfficiency));

        System.out.println("[STAN] " + worker.getName() + " załamał się błędem i głośno płacze!");
    }

    /**
     * Wykonuje cykliczną logikę stanu płaczu w każdej turze symulacji.
     * Inkrementuje statystyki osobiste płaczu pracownika, zmniejsza licznik blokady
     * oraz decyduje o kolejnym kroku: skierowaniu na odpoczynek (MovingToRestState)
     * lub powrocie do oczekiwania na zadania (WaitingForTaskState).
     *
     * @param worker Pracownik wykonujący akcję w bieżącej turze.
     * @param board Logiczna plansza gry.
     * @param sim Główny silnik symulacji zarządzający globalnym stanem biura.
     */
    @Override
    public void act(Worker worker, GameBoard board, Simulation sim) {
        this.cryTurnsRemaining--;
        worker.recordCrying();

        System.out.println("  -> " + worker.getName() + " wciąż płacze... Pozostało tur: " + this.cryTurnsRemaining);

        if (this.cryTurnsRemaining <= 0) {
            System.out.println("  -> " + worker.getName() + " uspokoił się.");

            if (worker.getEfficiency() < GameConfiguration.EFFICIENCY_REST_THRESHOLD) {
                worker.changeState(new MovingToRestState());
            } else {
                worker.changeState(new WaitingForTaskState());
            }
        }
    }
}