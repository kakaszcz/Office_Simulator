package game.model;

import game.agents.Agent;
import game.agents.Worker;

/**
 * Kartoteka (rekord) pracownika przechowująca szczegółowe statystyki historyczne i bieżące.
 * Służy jako kontener danych (Data Snapshot) dla menedżera HR, umożliwiając
 * archiwizowanie osiągnięć oraz zachowań agenta nawet po jego zwolnieniu z biura.
 */
public class EmployeeRecord {

    /** Imię pracownika wraz z przypisaną ikoną emoji. */
    public String name;
    /** Rola pracownika w strukturze firmy (np. "Junior", "Senior"). */
    public String role;
    /** Flaga określająca, czy pracownik jest nadal zatrudniony w biurze. */
    public boolean isActive;

    /** Bieżący współczynnik wydajności pracownika (zakres 0.0 - 1.0). */
    public double efficiency;
    /** Bieżący poziom doświadczenia zawodowego pracownika. */
    public double experience;
    /** Całkowity staż pracy wyrażony w liczbie przetrwanych tur. */
    public int turnsAlive;

    /** Liczba pomyślnie ukończonych i oddanych zadań programistycznych. */
    public int tasksCompleted;
    /** Liczba zadań zakończonych porażką (wypuszczeniem błędu). */
    public int tasksFailed;
    /** Sumaryczna liczba wypitych kubków kawy przy ekspresie. */
    public int coffeesDrunk;
    /** Liczba papierosów wypalonych podczas odpoczynku na zewnątrz. */
    public int cigarettesSmoked;

    /** Liczba tur spędzonych na płaczu po popełnieniu błędu (specyficzne dla Juniorów). */
    public int timesCried;
    /** Liczba odbytych rozmów blokujących z Szefem. */
    public int bossTalks;
    /** Liczba awarii pomyślnie usuniętych z systemu (specyficzne dla Seniorów). */
    public int bugsRepaired;
    /** Liczba otrzymanych od Szefa bonusów motywacyjnych do wydajności. */
    public int bossBoosts;

    /**
     * Tworzy nową, czystą kartotekę pracowniczą na podstawie obiektu Agent.
     * Inicjalizuje wszystkie liczniki statystyk wartościami początkowymi (zero).
     *
     * @param agent Obiekt agenta, dla którego zakładana jest teczka osobowa.
     */
    public EmployeeRecord(Agent agent) {
        this.name = agent.getName();
        this.role = agent.getClass().getSimpleName();
        this.isActive = true;
        this.turnsAlive = 0;
        this.tasksCompleted = 0;
        this.tasksFailed = 0;
        this.coffeesDrunk = 0;
        this.cigarettesSmoked = 0;
        this.timesCried = 0;
        this.bossTalks = 0;
        this.bugsRepaired = 0;
        this.bossBoosts = 0;
    }

    /**
     * Synchronizuje i aktualizuje wartości liczników w kartotece na podstawie
     * aktualnego stanu fizycznego agenta pobranego z symulacji.
     * Metoda inkrementuje również ogólny czas przeżycia pracownika w firmie.
     *
     * @param agent Instancja agenta, z której pobierane są najświeższe metryki.
     */
    public void updateStats(Agent agent) {
        this.turnsAlive++;
        if (agent instanceof Worker) {
            Worker w = (Worker) agent;

            this.efficiency = w.getEfficiency();
            this.experience = w.getExperience();

            this.tasksCompleted = w.getTasksCompleted();
            this.tasksFailed = w.getTasksFailed();
            this.coffeesDrunk = w.getPersonalCoffeesDrunk();
            this.cigarettesSmoked = w.getCigarettesSmoked();

            this.timesCried = w.getTimesCried();
            this.bossTalks = w.getBossTalks();
            this.bossBoosts = w.getBossBoosts();
            this.bugsRepaired = w.getBugsRepaired();
        }
    }
}