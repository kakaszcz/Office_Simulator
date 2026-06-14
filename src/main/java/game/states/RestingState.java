package game.states;

import game.agents.Junior;
import game.agents.Worker;
import game.core.GameConfiguration;
import game.core.Simulation;
import game.model.*;

/**
 * Stan stacjonarnego odpoczynku pracownika (Resting State) realizujący wzorzec projektowy State.
 * Obsługuje proces regeneracji współczynnika wydajności (Efficiency) agenta.
 * W zależności od strefy socjalnej, w której znajduje się pracownik, proces regeneracji
 * przebiega skokowo (strefa zewnętrzna - papieros) lub inkrementacyjnie (kuchnia - kawa).
 */
public class RestingState implements WorkerState {

    /** Tekstowy identyfikator strefy, w której agent spędza przerwę. */
    private final String restPlaceType;
    /** Licznik tur (kroków symulacji) pozostałych do zakończenia czasu odpoczynku. */
    private int restTurnsRemaining;
    /** Flaga uniemożliwiająca wielokrotne, błędne naliczanie statystyk globalnych w pętli act(). */
    private boolean statsUpdated = false;

    /**
     * Tworzy nową instancję stanu odpoczynku z określeniem strefy docelowej.
     *
     * @param restPlaceType Identyfikator typu kafelka (np. "coffee" lub "outside").
     */
    public RestingState(String restPlaceType) {
        this.restPlaceType = restPlaceType;
    }

    /**
     * Logika wywoływana w momencie wejścia pracownika w stan odpoczynku.
     * Inicjalizuje czas trwania przerwy na podstawie konfiguracji oraz aktualizuje
     * osobistą kartotekę statystyk agenta o fakt rozpoczęcia konsumpcji zasobu.
     *
     * @param worker Pracownik rozpoczynający przerwę regeneracyjną.
     */
    @Override
    public void enter(Worker worker) {
        this.restTurnsRemaining = GameConfiguration.REST_DURATION_TURNS;

        System.out.println("[STAN] " + worker.getName() + " rozpoczął odpoczynek w strefie: " + restPlaceType);

        if (GameConfiguration.TILE_TYPE_OUTSIDE.equals(restPlaceType)) {
            worker.recordCigarette();
        } else {
            worker.recordCoffee();
        }
    }

    /**
     * Wykonuje cykliczną logikę odzyskiwania sił przez pracownika w każdej turze.
     * Zapewnia jednorazową aktualizację rejestrów globalnych symulacji (idempotentność),
     * aplikuje odpowiedni algorytm regeneracji (stały vs. przyrostowy) oraz po upływie
     * wyznaczonego czasu kieruje pracownika z powrotem do biurka (MovingToDeskState).
     *
     * @param worker Pracownik odpoczywający w bieżącej turze.
     * @param board Logiczna plansza gry.
     * @param sim Główny silnik symulacji zarządzający globalnymi licznikami zużycia zasobów.
     */
    @Override
    public void act(Worker worker, GameBoard board, Simulation sim) {

        // Jednorazowa aktualizacja globalnego rejestru statystyk firmy (Anti-multi-counting system)
        if (!statsUpdated) {
            if (GameConfiguration.TILE_TYPE_OUTSIDE.equals(restPlaceType)) {
                sim.incrementCigarettes();
            } else {
                sim.incrementCoffee();
            }
            statsUpdated = true;
        }

        // Ewaluacja profilu regeneracji wydajności
        if (GameConfiguration.TILE_TYPE_OUTSIDE.equals(restPlaceType)) {
            worker.setEfficiency(1.0);
        } else {
            double currentEff = worker.getEfficiency();
            double regeneratedEff = currentEff + GameConfiguration.COFFEE_REGEN_RATE;
            worker.setEfficiency(Math.min(1.0, regeneratedEff));
        }

        restTurnsRemaining--;

        System.out.println("  -> " + worker.getName() + " odpoczywa... (Aktualna wydajność: "
                + String.format("%.2f", worker.getEfficiency()) + ")");

        // Warunek graniczny wyjścia ze stanu po pełnej regeneracji sił
        if (restTurnsRemaining <= 0) {
            System.out.println("  -> " + worker.getName() + " odpoczął i wraca do pracy.");
            worker.changeState(new MovingToDeskState());
        }
    }
}