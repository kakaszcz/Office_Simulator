package game.states;

import game.core.Simulation;
import game.model.GameBoard;
import game.agents.Worker;
import game.agents.Junior;
import game.core.GameConfiguration;
import java.util.Random;

/**
 * Główny stan operacyjny pracownika (Working State) realizujący wzorzec projektowy State.
 * Odpowiada za proces wykonywania przypisanego zadania programistycznego, cykliczne obniżanie
 * wydajności (Efficiency) w wyniku zmęczenia oraz ewaluację końcowego rezultatu pracy (Sukces / Porażka).
 * Klasa implementuje algorytm krzywej uczenia się dla Juniorów oraz mechanizmy defensywne
 * chroniące symulację przed zablokowaniem wątku operacyjnego.
 */
public class WorkingState implements WorkerState {

    /** Generator liczb pseudolosowych używany do probabilistycznej oceny powodzenia zadania. */
    private static final Random RANDOM = new Random();

    /**
     * Logika wywoływana w momencie wejścia pracownika w stan aktywnej pracy.
     * Pobiera bazowy czas wykonania zadania, aplikuje twardy bezpiecznik czasowy (Hard Timeout Ceiling)
     * w celu ochrony płynności symulacji oraz pobiera początkowy koszt energetyczny (Efficiency) za podjęcie wyzwania.
     *
     * @param worker Obiekt pracownika rozpoczynającego proces produkcyjny.
     */
    @Override
    public void enter(Worker worker) {
        int workTime = worker.computeTaskTime();

        // Twardy bezpiecznik (Hard Timeout Ceiling): chroni pętlę symulacji przed zamrożeniem z powodu wycieńczenia agenta
        if (workTime <= 0 || workTime > 50) {
            workTime = 15;
        }

        worker.setTurnsLeft(workTime);
        worker.setTotalTaskTime(workTime);

        // Obniżenie wydajności na starcie jako koszt wejścia w głębokie skupienie (Deep Work Focus)
        worker.setEfficiency(Math.max(0.0, worker.getEfficiency() - GameConfiguration.WORK_ENTER_EFFICIENCY_DECREASE));
        System.out.println("[STAN] " + worker.getName() + " zaczyna pracę. Zadanie zajmie mu " + workTime + " tur.");
    }

    /**
     * Realizuje cykliczny postęp prac nad zadaniem w każdej turze symulacji.
     * Dekrementuje licznik pozostałego czasu, systematycznie obniża wydajność pracownika
     * i po osiągnięciu zera wywołuje ostateczną ewaluację biznesową taska.
     *
     * @param worker Pracownik wykonujący zadanie w bieżącej turze.
     * @param board Logiczna plansza gry.
     * @param sim Główny silnik symulacji.
     */
    @Override
    public void act(Worker worker, GameBoard board, Simulation sim) {
        worker.decrementTurnsLeft();
        worker.setEfficiency(Math.max(0.0, worker.getEfficiency() - GameConfiguration.WORK_STEP_EFFICIENCY_DECREASE));

        System.out.println("  -> " + worker.getName() + " pracuje... Pozostało tur: " + worker.getTurnsLeft() + " (Eff: " + String.format("%.2f", worker.getEfficiency() * 100) + "%)");

        if (worker.getTurnsLeft() <= 0) {
            evaluateTaskResult(worker, sim);
        }
    }

    /**
     * Dokonuje probabilistycznej ewaluacji rezultatu ukończonego zadania.
     * W przypadku porażki (Failure path): generuje błędy i wprowadza agenta w stan kryzysu emocjonalnego.
     * W przypadku sukcesu (Success path): generuje przychód finansowy, aktualizuje doświadczenie
     * pracownika według algorytmu nieliniowego i przełącza go w stan świętowania.
     *
     * @param worker Pracownik kończący zadanie.
     * @param sim Silnik symulacji przyjmujący przychody bądź raporty o błędach.
     */
    private void evaluateTaskResult(Worker worker, Simulation sim) {
        worker.setHasTask(false);

        // Probabilistyczna weryfikacja szansy na błąd na podstawie profilu i zmęczenia agenta
        if (RANDOM.nextDouble() < worker.getFailChance()) {
            // SCENARIUSZ PORAŻKI
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
            // SCENARIUSZ SUKCESU
            sim.incrementSuccess();
            worker.recordTaskCompleted();

            // Nieliniowa krzywa uczenia się Juniora (Asymptotyczny przyrost wiedzy - Diminishing Returns)
            if (worker instanceof Junior) {
                Junior junior = (Junior) worker;
                double currentExp = junior.getExperience();
                double gain = GameConfiguration.JUNIOR_EXPERIENCE_GAIN_PER_TASK * (1.0 - currentExp);
                junior.setExperience(Math.min(1.0, currentExp + gain));

                System.out.println("  -> " + junior.getName() + " nauczył się czegoś nowego. Obecny Exp: "
                        + String.format("%.2f", junior.getExperience()));
            }

            // Algorytm wyceny efektu pracy z uwzględnieniem modyfikatora doświadczenia
            double zarobek = GameConfiguration.TASK_BASE_REWARD + (worker.getExperience() * GameConfiguration.TASK_EXPERIENCE_MULTIPLIER);
            sim.earnMoney(zarobek);
            System.out.println("  -> Zadanie zakończone SUKCESEM przez " + worker.getName() + ".");

            worker.changeState(new SuccessState());
        }
    }
}