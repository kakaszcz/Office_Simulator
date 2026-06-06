package game.states;

import game.core.Simulation;
import game.model.GameBoard;
import game.agents.Worker;
import game.agents.Junior;
import game.core.GameConfiguration;
import java.util.Random;

public class WorkingState implements WorkerState {

    private static final Random RANDOM = new Random();

    @Override
    public void enter(Worker worker) {
        int workTime = worker.computeTaskTime();
        worker.setTurnsLeft(workTime);
        worker.setTotalTaskTime(workTime);

        // Spadek wydajności na starcie pobierany z GameConfiguration
        worker.setEfficiency(Math.max(0.0, worker.getEfficiency() - GameConfiguration.WORK_ENTER_EFFICIENCY_DECREASE));
        System.out.println("[STAN] " + worker.getName() + " zaczyna pracę. Zadanie zajmie mu " + workTime + " tur.");
    }

    @Override
    public void act(Worker worker, GameBoard board, Simulation sim) {
        // Odegranie tury = odejmujemy czas z paska pracownika
        worker.decrementTurnsLeft();

        // Spadek wydajności co turę pobierany z GameConfiguration
        worker.setEfficiency(Math.max(0.0, worker.getEfficiency() - GameConfiguration.WORK_STEP_EFFICIENCY_DECREASE));

        System.out.println("  -> " + worker.getName() + " pracuje... Pozostało tur: " + worker.getTurnsLeft() + " (Eff: " + String.format("%.2f", worker.getEfficiency() * 100) + "%)");

        // Sprawdzamy, czy licznik pracownika dobił do zera
        if (worker.getTurnsLeft() <= 0) {
            evaluateTaskResult(worker, sim);
        }
    }

    private void evaluateTaskResult(Worker worker, Simulation sim) {
        // Zdejmujemy flagę zadania
        worker.setHasTask(false);

        // REFAKTOR: Użycie współdzielonej instancji RANDOM zamiast tworzenia nowej
        if (RANDOM.nextDouble() < worker.getFailChance()) {
            //  PORAŻKA
            sim.incrementFailed();
            sim.incrementTears();
            worker.handleTaskFailure(sim);

            // Jeśli to Junior popełnił błąd, raportujemy to do systemu kar Simulation
            if (worker instanceof Junior) {
                sim.reportJuniorFail();
            }

            System.out.println("  -> Zadanie zakończone PORAŻKĄ. " + worker.getName() + " zaczyna płakać!");
            worker.changeState(new CryingState());
        } else {
            //  SUKCES
            sim.incrementSuccess();
            worker.recordTaskCompleted();

            // Wyliczanie zarobku na podstawie stałych z GameConfiguration
            double zarobek = GameConfiguration.TASK_BASE_REWARD + (worker.getExperience() * GameConfiguration.TASK_EXPERIENCE_MULTIPLIER);
            sim.earnMoney(zarobek);
            System.out.println("  -> Zadanie zakończone SUKCESEM przez " + worker.getName() + ".");

            // Sprawdzamy zmęczenie przy użyciu progu z konfiguracji
            if (worker.getEfficiency() < GameConfiguration.EFFICIENCY_REST_THRESHOLD) {
                worker.changeState(new MovingToRestState());
            } else {
                worker.changeState(new WaitingForTaskState());
            }
        }
    }
}