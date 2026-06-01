package game.states;

import game.core.Simulation;
import game.model.Worker;
import game.model.Cell;
import game.model.GameBoard;

import java.util.Random;

public class MovingToRestState implements WorkerState {

    private final Random random = new Random();
    private String destinationType; // "coffee" lub "outside"
    private Cell targetCell;

    @Override
    public void enter(Worker worker) {
        // Losowanie 50% na 50% zgodnie z diagramem
        if (random.nextBoolean()) {
            this.destinationType = "coffee";
            System.out.println("[STAN] " + worker.getName() + " poczuł zmęczenie. Postanawia iść do kuchni na kawę.");
        } else {
            this.destinationType = "outside";
            System.out.println("[STAN] " + worker.getName() + " poczuł zmęczenie. Postanawia wyjść na dwór zaczerpnąć powietrza.");
        }
    }

    @Override
    public void act(Worker worker, GameBoard board, Simulation sim) {
        // Jeśli jeszcze nie znaleźliśmy konkretnej komórki docelowej na planszy, szukamy jej
        if (targetCell == null) {
            targetCell = board.findFirstEmptyCell(destinationType);
        }

        if (targetCell == null) {
            System.out.println("  -> " + worker.getName() + " nie może znaleźć wolnego miejsca typu: " + destinationType + ". Czeka.");
            return;
        }

        // --- RUCH AGENTA ---
        // Tutaj docelowo wywołasz swoją logikę poruszania się (np. krok w stronę targetCell).
        // Na ten moment, aby przetestować działanie stanów, zasymulujmy zbliżanie się:
        boolean reachedDestination = simulateMovement(worker, targetCell, board);

        if (reachedDestination) {
            // Dotarł na miejsce! Przechodzi w stan właściwego odpoczynku
            worker.changeState(new RestingState(destinationType));
        }
    }

    private boolean simulateMovement(Worker worker, Cell target, GameBoard board) {
        // Prosta tymczasowa logika teleportacji lub kroku na potrzeby testów pętli stanów:
        // Czyścimy starą komórkę
        board.getCell(worker.getX(), worker.getY()).setAgent(null);

        // Przypisujemy nową pozycję (na razie bezpośrednio cel, żeby sprawdzić stany)
        worker.setX(target.getX());
        worker.setY(target.getY());
        target.setAgent(worker);

        System.out.println("  -> " + worker.getName() + " przemieścił się do strefy: " + destinationType);
        return true; // Zwraca true, jeśli stanął dokładnie na polu docelowym
    }
}