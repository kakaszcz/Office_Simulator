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
        // Pętla wykona się maksymalnie 2 razy w ciągu jednej tury symulacji - poruszanie o 2 kafelki
        for (int i = 0; i < 2; i++) {
            int curX = worker.getX();
            int curY = worker.getY();
            int targetX = target.getX();
            int targetY = target.getY();

            // Jeśli już stoi na celu (np. dotarł w pierwszym kroku), kończymy ruch
            if (curX == targetX && curY == targetY) {
                return true;
            }

            // Obliczamy pojedynczy krok (-1, 0, lub 1)
            int nextX = curX + Integer.compare(targetX, curX);
            int nextY = curY + Integer.compare(targetY, curY);

            // Próba wykonania pojedynczego kroku
            if (board.moveAgent(curX, curY, nextX, nextY)) {
                worker.setX(nextX);
                worker.setY(nextY);
                System.out.println("  -> " + worker.getName() + " biegnie... (" + nextX + ", " + nextY + ")");
            } else {
                // Jeśli napotkał przeszkodę (ścianę), przerywamy pętlę – nie zrobi drugiego kroku
                System.out.println("  -> " + worker.getName() + " został zablokowany na kafelku.");
                break;
            }
        }

        // Zwraca true tylko jeśli po obu krokach stoi dokładnie na celu
        return worker.getX() == target.getX() && worker.getY() == target.getY();
    }
}