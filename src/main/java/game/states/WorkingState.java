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

        // TWARDY LIMIT: Jeśli wydajność jest tak niska, że czas wybiło w kosmos,
        // zmuszamy go do pracy przez max 15 tur, żeby nie zamroził symulacji.
        if (workTime <= 0 || workTime > 50) {
            workTime = 15;
        }

        worker.setTurnsLeft(workTime);
        worker.setTotalTaskTime(workTime);

        worker.setEfficiency(Math.max(0.0, worker.getEfficiency() - GameConfiguration.WORK_ENTER_EFFICIENCY_DECREASE));
        System.out.println("[STAN] " + worker.getName() + " zaczyna pracę. Zadanie zajmie mu " + workTime + " tur.");
    }

    @Override
    public void act(Worker worker, GameBoard board, Simulation sim) {
        worker.decrementTurnsLeft();
        worker.setEfficiency(Math.max(0.0, worker.getEfficiency() - GameConfiguration.WORK_STEP_EFFICIENCY_DECREASE));

        System.out.println("  -> " + worker.getName() + " pracuje... Pozostało tur: " + worker.getTurnsLeft() + " (Eff: " + String.format("%.2f", worker.getEfficiency() * 100) + "%)");

        if (worker.getTurnsLeft() <= 0) {
            evaluateTaskResult(worker, sim);
        }
    }

    private void evaluateTaskResult(Worker worker, Simulation sim) {
        worker.setHasTask(false);

        if (RANDOM.nextDouble() < worker.getFailChance()) {
            // --- 🔴 PORAŻKA ---
            sim.incrementFailed();
            sim.incrementTears();
            worker.handleTaskFailure(sim);

            if (worker instanceof Junior) {
                sim.reportJuniorFail();
                System.out.println("  -> Zadanie zakończone PORAŻKĄ. " + worker.getName() + " zaczyna płakać!");
                worker.changeState(new CryingState());
            } else {
                System.out.println("  -> Zadanie zakończone PORAŻKĄ. " + worker.getName() + " jest wściekły!");
                worker.changeState(new MadState());
            }
        } else {
            // --- 🟢 SUKCES ---
            sim.incrementSuccess();
            worker.recordTaskCompleted();

            // LOGIKA UCZENIA SIĘ JUNIORA (KRZYWA UCZENIA)
            if (worker instanceof Junior) {
                Junior junior = (Junior) worker;
                double currentExp = junior.getExperience();
                // Im bliżej 1.0, tym mnożnik (1.0 - currentExp) jest mniejszy, więc exp rośnie wolniej!
                double gain = GameConfiguration.JUNIOR_EXPERIENCE_GAIN_PER_TASK * (1.0 - currentExp);
                junior.setExperience(Math.min(1.0, currentExp + gain));

                System.out.println("  -> " + junior.getName() + " nauczył się czegoś nowego. Obecny Exp: "
                        + String.format("%.2f", junior.getExperience()));
            }

            double zarobek = GameConfiguration.TASK_BASE_REWARD + (worker.getExperience() * GameConfiguration.TASK_EXPERIENCE_MULTIPLIER);
            sim.earnMoney(zarobek);
            System.out.println("  -> Zadanie zakończone SUKCESEM przez " + worker.getName() + ".");

            worker.changeState(new SuccessState());
        }
    }
}