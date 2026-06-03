package game.states;

import game.core.Simulation;
import game.model.GameBoard;
import game.model.Worker;
import java.util.Random;

public class WorkingState implements WorkerState {

    private final Random random = new Random();

    @Override
    public void enter(Worker worker) {
        // 1. Zamiast trzymać zmienną tutaj, zapisujemy czas prosto do PRACOWNIKA
        int czasPracy = worker.computeTaskTime();
        worker.setTurnsLeft(czasPracy);

        worker.setEfficiency(Math.max(0.0, worker.getEfficiency() - 0.10));
        System.out.println("[STAN] " + worker.getName() + " zaczyna pracę. Zadanie zajmie mu " + czasPracy + " tur.");
    }

    @Override
    public void act(Worker worker, GameBoard board, Simulation sim) {
        // 2. Odegranie tury = odejmujemy czas z paska pracownika
        worker.decrementTurnsLeft();

        System.out.println("  -> " + worker.getName() + " pracuje... Pozostało tur: " + worker.getTurnsLeft() + " (Eff: " + String.format("%.2f", worker.getEfficiency()) + ")");

        // 3. Sprawdzamy, czy licznik pracownika dobił do zera
        if (worker.getTurnsLeft() <= 0) {
            evaluateTaskResult(worker, sim);
        }
    }

    private void evaluateTaskResult(Worker worker, Simulation sim) {
        // Zdejmujemy flagę zadania – dzięki temu pasek całkowicie znika z ekranu po zakończeniu
        worker.setHasTask(false);

        if (random.nextDouble() < worker.getFailChance()) {
            // PORAŻKA
            worker.handleTaskFailure(sim);
            System.out.println("  -> Zadanie zakończone PORAŻKĄ. " + worker.getName() + " zaczyna płakać!");
            worker.changeState(new CryingState());
        } else {
            // SUKCES
            sim.earnMoney(10.0);
            System.out.println("  -> Zadanie zakończone SUKCESEM przez " + worker.getName() + ".");

            // Sprawdzamy zmęczenie od razu po oddaniu taska
            if (worker.getEfficiency() < 0.45) {
                worker.changeState(new MovingToRestState());
            } else {
                worker.changeState(new WaitingForTaskState());
            }
        }
    }
}