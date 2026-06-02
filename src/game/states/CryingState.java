package game.states;

import game.core.Simulation;
import game.model.GameBoard;
import game.model.Junior;
import game.model.Worker;

public class CryingState implements WorkerState {

    private int cryTurnsRemaining = 2; // Czas trwania płaczu (np. 2 tury)

    @Override
    public void enter(Worker worker) {
        worker.setEfficiency(Math.max(0.0, worker.getEfficiency() - 0.10));
        System.out.println("[STAN] " + worker.getName() + " załamał się błędem i głośno płacze!");
    }

    @Override
    public void act(Worker worker, GameBoard board, Simulation sim) {
        cryTurnsRemaining--;
        System.out.println("  -> " + worker.getName() + " wciąż płacze... Pozostało tur: " + cryTurnsRemaining);

        if (cryTurnsRemaining <= 0) {
            System.out.println("  -> " + worker.getName() + " uspokoił się.");

            if (worker instanceof Junior) {
                Junior junior = (Junior) worker;
                // Zakładam, że w klasie Junior (lub Worker) masz metodę pobierającą liczbę błędów
                if (junior.getNumberOfFails() >= 5) {
                    junior.markFired(); // Flagujemy do usunięcia z planszy
                    System.out.println("!!! ZWOLNIENIE! Junior " + junior.getName()
                            + " popełnił już " + junior.getNumberOfFails() + " błędów. Zostaje wyrzucony z firmy!");
                    return; // Przerywamy działanie, agent kończy swój żywot w symulacji
                }
            }

            // Po płaczu sprawdzamy, czy nie jest przemęczony
            if (worker.getEfficiency() < 0.45) {
                worker.changeState(new MovingToRestState());
            } else {
                worker.changeState(new WaitingForTaskState());
            }
        }
    }
}