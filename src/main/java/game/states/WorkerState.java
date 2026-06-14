package game.states;

import game.core.Simulation;
import game.model.GameBoard;
import game.agents.Worker;

/**
 * Interfejs bazowy dla stanów agentów (Worker State), stanowiący fundament
 * implementacji wzorca projektowego State (Stan).
 * Definiuje kontrakt dla wszystkich klas reprezentujących poszczególne fazy behawioralne
 * pracowników w biurowym ekosystemie symulacji. Odpowiada za bezpośrednie odwzorowanie
 * przejść stanowych oraz akcji cyklicznych zdefiniowanych na diagramie maszyny stanów UML.
 */
public interface WorkerState {

    /**
     * Metoda akcji wejściowej (Entry Action), wywoływana dokładnie raz w momencie
     * dokonywania tranzycji i inicjalizacji danego stanu u pracownika.
     * Służy do konfigurowania wartości startowych, aplikowania natychmiastowych kar
     * lub nagród ekonomicznych oraz uruchamiania logiki inicjalizującej.
     *
     * @param worker Obiekt pracownika (kontekst wzorca State), u którego następuje zmiana stanu.
     */
    void enter(Worker worker);

    /**
     * Metoda akcji cyklicznej (State Activity), wywoływana sekwencyjnie w każdej turze
     * z poziomu głównej pętli silnika symulacji.
     * Odpowiada za realizację właściwej logiki biznesowej przypisanej do danego stanu,
     * aktualizację liczników oraz ewaluację warunków przejścia (tranzycji) do kolejnych stanów.
     *
     * @param worker Pracownik wykonujący przypisaną do stanu czynność w bieżącej turze.
     * @param board Logiczna plansza gry, dostarczająca kontekst przestrzenny.
     * @param sim Główny silnik symulacji, zarządzający globalnymi rejestrami i czasem.
     */
    void act(Worker worker, GameBoard board, Simulation sim);
}