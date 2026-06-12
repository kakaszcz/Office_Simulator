package game.model;

import game.agents.Agent;
import game.agents.Worker;

public class EmployeeRecord {
    public String name;
    public String role;
    public boolean isActive;

    public double efficiency;
    public double experience;
    public int turnsAlive;

    // Podstawowe statystyki
    public int tasksCompleted;
    public int tasksFailed;
    public int coffeesDrunk;
    public int cigarettesSmoked;

    // Ekstra statystyki (łzy, szef, bugi)
    public int timesCried;
    public int bossTalks;
    public int bugsRepaired;
    public int bossBoosts;

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

    // Wywoływane co turę dla żyjących agentów w Simulation
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