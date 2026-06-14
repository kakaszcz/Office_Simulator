package game.states;

import game.core.GameConfiguration;
import game.core.Simulation;
import game.agents.Worker;
import game.model.Cell;
import game.model.GameBoard;

/**
 * Stan przemieszczania się do stanowiska pracy (Moving To Desk State) realizujący wzorzec projektowy State.
 * Odpowiada za logikę powrotu pracownika do przypisanego lub nowo znalezionego biurka.
 * Zawiera zaawansowane mechanizmy unikania zakleszczeń (Deadlock Prevention) poprzez monitoring
 * zablokowanych tur oraz optymalizację obciążenia procesora (Cool-down system) w przypadku braku zasobów.
 */
public class MovingToDeskState implements WorkerState {

    /** Referencja do kafelka planszy reprezentującego docelowe biurko programisty. */
    private Cell targetDesk;
    /** Licznik tur wstrzymujących ponowne przeszukiwanie mapy, zapobiegający obciążeniu procesora. */
    private int searchCooldown = 0;
    /** Licznik tur, przez które agent stał w miejscu z powodu zablokowania ścieżki przez innych agentów. */
    private int blockedTurnsCount = 0;

    /**
     * Logika wywoływana w momencie wejścia agenta w stan podróży do biurka.
     * Resetuje licznik frustracji/zablokowania drogi.
     *
     * @param worker Obiekt pracownika rozpoczynającego przemieszczanie.
     */
    @Override
    public void enter(Worker worker) {
        System.out.println("[STAN] " + worker.getName() + " zmierza do biurka.");
        this.blockedTurnsCount = 0;
    }

    /**
     * Zarządza procesem nawigacji krokietowej w każdej turze symulacji.
     * Kontroluje zajętość biurka docelowego, dynamicznie rezerwuje nowe zasoby w przypadku podkradnięcia,
     * wywołuje algorytm wykonania kroku oraz resetuje trasę w przypadku wykrycia zatoru korytarzowego.
     *
     * @param worker Pracownik wykonujący ruch w bieżącej turze.
     * @param board Logiczna plansza gry.
     * @param sim Główny silnik symulacji zarządzający globalnym stanem biura.
     */
    @Override
    public void act(Worker worker, GameBoard board, Simulation sim) {

        Cell currentCell = board.getCell(worker.getX(), worker.getY());

        // Weryfikacja bezpośredniego dotarcia do celu przed wykonaniem obliczeń nawigacyjnych
        if (targetDesk != null && currentCell.getX() == targetDesk.getX() && currentCell.getY() == targetDesk.getY()) {
            finalizeArrival(worker);
            return;
        }

        // System cool-down: blokada ponownego skanowania planszy, jeśli w poprzednich turach brakowało wolnych biurek
        if (searchCooldown > 0) {
            searchCooldown--;
            return;
        }

        // Walidacja dynamiczna: jeśli inne źródło zmieniło stan biurka i ktoś je zajął, porzucamy cel
        if (targetDesk != null && (!targetDesk.isEmpty() && targetDesk.getAgent() != worker)) {
            targetDesk = null;
        }

        // Alokacja biurka: wywoływana tylko, gdy agent nie ma aktualnie wyznaczonego celu
        if (targetDesk == null) {
            targetDesk = board.findFirstEmptyCell(GameConfiguration.TILE_TYPE_DESK);

            if (targetDesk == null) {
                searchCooldown = 3; // Odpoczynek dla procesora na 3 tury
                return;
            }
        }

        int oldX = worker.getX();
        int oldY = worker.getY();

        // Wykonanie pojedynczego kroku w stronę celu przy użyciu wbudowanego algorytmu PathFinder
        worker.navigateTo(targetDesk, board);

        // Algorytm detekcji kolizji i zatorów (Anti-collision & Deadlock system)
        if (worker.getX() == oldX && worker.getY() == oldY) {
            blockedTurnsCount++;

            if (blockedTurnsCount > 4) {
                System.out.println("[INFO] " + worker.getName() + " utknął w drodze do biurka na 5 tur. Resetuje trasę!");
                targetDesk = null;
                blockedTurnsCount = 0;
                return;
            }
        } else {
            blockedTurnsCount = 0; // Ruch wykonany pomyślnie - zerowanie licznika frustracji
        }

        // Sprawdzenie stanu dotarcia natychmiast po wykonaniu fizycznego kroku na siatce
        if (worker.getX() == targetDesk.getX() && worker.getY() == targetDesk.getY()) {
            finalizeArrival(worker);
        }
    }

    /**
     * Finalizuje proces podróży agenta, zdejmuje go z reżimu podróży i wprowadza
     * w odpowiedni stan roboczy w zależności od tego, czy posiada aktualnie przydzielone zadanie.
     *
     * @param worker Pracownik, który pomyślnie zajął swoje biurko.
     */
    private void finalizeArrival(Worker worker) {
        System.out.println("  -> " + worker.getName() + " dotarł do biurka.");
        if (worker.hasTask()) {
            worker.changeState(new WorkingState());
        } else {
            worker.changeState(new WaitingForTaskState());
        }
    }
}