package game.states;

import game.core.Simulation;
import game.model.GameBoard;
import game.model.Worker;
import java.util.Random;

public class WorkingState implements WorkerState {

    private int taskTimeRemaining;

    public int getTasktimeRemaining() {
        return taskTimeRemaining;
    }

    private final Random random = new Random();

    @Override
    public void enter(Worker worker) {
        this.taskTimeRemaining = worker.computeTaskTime();
        worker.setEfficiency(Math.max(0.0, worker.getEfficiency() - 0.10));
        System.out.println("[STAN] " + worker.getName() + " zaczyna pracę. Zadanie zajmie mu " + taskTimeRemaining + " tur.");
    }

    @Override
    public void act(Worker worker, GameBoard board, Simulation sim) {
        taskTimeRemaining--;

        System.out.println("  -> " + worker.getName() + " pracuje... Pozostało tur: " + taskTimeRemaining + " (Eff: " + String.format("%.2f", worker.getEfficiency()) + ")");

        if (taskTimeRemaining <= 0) {
            evaluateTaskResult(worker, sim);
        }
    }

    private void evaluateTaskResult(Worker worker, Simulation sim) {
        // Dzięki polimorfizmowi Junior zwróci swoją szansę, a Senior 0.0
        if (random.nextDouble() < worker.getFailChance()) {
            // PORAŻKA: Delegujemy zachowanie do obiektu pracownika
            worker.handleTaskFailure(sim);

            System.out.println("  -> Zadanie zakończone PORAŻKĄ. " + worker.getName() + " zaczyna płakać!");
            worker.changeState(new CryingState());
        } else {
            // SUKCES
            worker.setHasTask(false);

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