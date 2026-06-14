package game.states;

import game.core.GameConfiguration;
import game.core.Simulation;
import game.agents.Worker;
import game.model.Cell;
import game.model.GameBoard;
import java.util.Random;

/**
 * Stan przemieszczania się na odpoczynek (Moving To Rest State) realizujący wzorzec projektowy State.
 * Aktywowany w momencie, gdy poziom wydajności (Efficiency) pracownika spadnie poniżej 
 * krytycznego progu konfiguracyjnego. Klasa losuje formę regeneracji sił (przerwa kawowa 
 * lub wyjście na zewnątrz) i nawiguje agenta do odpowiedniej, wolnej strefy regeneracji.
 */
public class MovingToRestState implements WorkerState {

    /** Generator liczb pseudolosowych używany do determinowania wyboru miejsca odpoczynku. */
    private static final Random RANDOM = new Random();

    /** Wybrany typ docelowego kafelka odpoczynku (kawa lub przestrzeń zewnętrzna). */
    private String destinationType;

    /** Referencja do konkretnej, zarezerwowanej komórki planszy, do której zmierza agent. */
    private Cell targetCell;

    /**
     * Logika wywoływana w momencie wejścia agenta w stan poszukiwania odpoczynku.
     * Wybiera drogą losową (prawdopodobieństwo 50/50) pomiędzy udaniem się do ekspresu z kawą 
     * a wyjściem na zewnątrz biura na papierosa/świeże powietrze.
     *
     * @param worker Obiekt zmęczonego pracownika.
     */
    @Override
    public void enter(Worker worker) {
        if (RANDOM.nextBoolean()) {
            this.destinationType = GameConfiguration.TILE_TYPE_COFFEE;
            System.out.println("[STAN] " + worker.getName() + " poczuł zmęczenie. Postanawia iść do kuchni na kawę.");
        } else {
            this.destinationType = GameConfiguration.TILE_TYPE_OUTSIDE;
            System.out.println("[STAN] " + worker.getName() + " poczuł zmęczenie. Postanawia wyjść na dwór zaczerpnąć powietrza.");
        }
    }

    /**
     * Odpowiada za wykonanie kroków nawigacyjnych w stronę wyznaczonej strefy socjalnej.
     * Wykorzystuje zdefiniowaną w konfiguracji liczbę kroków na turę. W momencie dotarcia
     * na miejsce, odnotowuje ten fakt w globalnych statystykach symulacji i przełącza 
     * pracownika w stacjonarny stan odpoczynku (RestingState).
     *
     * @param worker Pracownik wykonujący ruch w bieżącej turze.
     * @param board Logiczna plansza gry, na której wyznaczana jest ścieżka.
     * @param sim Główny silnik symulacji służący do raportowania globalnego spożycia kawy.
     */
    @Override
    public void act(Worker worker, GameBoard board, Simulation sim) {
        // Alokacja celu: jeśli agent nie posiada przypisanego kafelka docelowego, szuka wolnego miejsca
        if (targetCell == null) {
            targetCell = board.findFirstEmptyCell(destinationType);
        }

        // Zabezpieczenie: jeśli brak wolnych miejsc w wybranej strefie, pracownik oczekuje na zwolnienie zasobów
        if (targetCell == null) {
            System.out.println("  -> " + worker.getName() + " nie może znaleźć wolnego miejsca typu: " + destinationType + ". Czeka.");
            return;
        }

        boolean reachedDestination = false;
        int steps = GameConfiguration.WORKER_MOVE_STEPS_PER_TURN;

        // Pętla mikrokroków przypisana do pojedynczej tury symulacji
        for (int i = 0; i < steps; i++) {
            worker.navigateTo(targetCell, board);

            if (worker.getX() == targetCell.getX() && worker.getY() == targetCell.getY()) {
                reachedDestination = true;
                break;
            }
        }

        // Finalizacja podróży i zmiana stanu autonomicznego
        if (reachedDestination) {
            if (GameConfiguration.TILE_TYPE_COFFEE.equals(destinationType)) {
                sim.recordCoffeeDrunk();
            }
            worker.changeState(new RestingState(destinationType));
        }
    }
}