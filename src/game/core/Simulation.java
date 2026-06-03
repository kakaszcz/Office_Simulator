package game.core;

import game.model.Worker;
import game.model.*;
import game.model.AgentFactory; // Upewnij się, że importujesz poprawny pakiet!

import java.util.ArrayList;
import java.util.List;

public class Simulation {

    private GameBoard gameBoard;
    private List<Agent> agents;
    private double budget;
    private int stepCount;
    private boolean isRunning;
    private int totalFails = 0;

    // Dodajemy fabrykę jako pole w klasie
    private AgentFactory factory;

    public Simulation(int numJuniors, int numSeniors, int initialBudget) {
        this.gameBoard = new GameBoard();
        this.agents = new ArrayList<>();
        this.budget = initialBudget;
        this.stepCount = 0;
        this.isRunning = true;

        // Inicjalizujemy fabrykę
        this.factory = new AgentFactory();

        // Stworzenie szefa ZA POMOCĄ FABRYKI
        Boss boss = factory.createBoss(2, 3, initialBudget);
        agents.add(boss);
        gameBoard.getCell(2, 3).setAgent(boss);
        System.out.println("Stworzono szefa " + boss.getName() + ".");

        int seniorsToCreate = Math.min(numSeniors, GameConfiguration.MAX_SENIORS);
        int juniorsToCreate = Math.min(numJuniors, GameConfiguration.MAX_JUNIORS);

        // Stworzenie pracowników
        createWorkers(seniorsToCreate, "Senior");
        createWorkers(juniorsToCreate, "Junior");
    }

    private void createWorkers(int num, String type) {
        for (int i = 0; i < num; i++) {
            Cell freeDesk = gameBoard.findFirstEmptyCell("desk");

            if (freeDesk != null) {
                Worker w;
                if (type.equals("Junior")) {
                    w = factory.createJunior(freeDesk.getX(), freeDesk.getY());
                } else {
                    w = factory.createSenior(freeDesk.getX(), freeDesk.getY());
                }

                agents.add(w);
                freeDesk.setAgent(w);

                System.out.println("Stworzono " + type + " o imieniu " + w.getName() + " (Doświadczenie: "
                        + String.format("%.2f", w.getExperience()) + " Wydajność:" + String.format("%.2f", w.getEfficiency()) + ")");
            }
        }
    }

    public GameBoard getGameBoard() {
        return gameBoard;
    }

    public void step() {
        if (!isRunning) return;
        stepCount++;
        System.out.println("--- TURA " + stepCount + " ---");

        List<Agent> agentsCopy = new ArrayList<>(this.agents);
        for (Agent agent : agentsCopy) {
            if (this.agents.contains(agent)) {
                agent.act(gameBoard, this);
            }
        }
    }

    public void reportJuniorFail() {
        this.totalFails++;
        System.out.println("!!! Wykryto błąd Juniora. Aktualna liczba błędów w firmie: " + totalFails);
        if (this.totalFails >= GameConfiguration.MAX_FAILS_LIMIT) {
            triggerFatalError();
        }
    }

    public void repairFail() {
        if (this.totalFails > 0) {
            this.totalFails--;
            System.out.println("=== Senior naprawił błąd! Aktualna liczba błędów w firmie: " + totalFails);
        }
    }

    private void triggerFatalError() {
        double penalty = GameConfiguration.FATAL_ERROR_PENALTY;
        this.budget -= penalty;
        this.totalFails = 0;
        System.out.println("FATAL ERROR! Firma płaci karę: " + penalty + "$. Aktualny budżet: " + this.budget);

        if (this.budget <= 0) {
            System.out.println("BANKRUCTWO! Koniec gry.");
            this.isRunning = false;
        }
    }

    public int getTotalFails() { return totalFails; }
    public double getBudget() { return this.budget; }
    public List<Agent> getAgents() { return this.agents; }

    public void removeAgent(Agent agent) {
        if (this.agents.contains(agent)) {
            this.agents.remove(agent);
            System.out.println(agent.getName() + " został zwolniony.");
        }
    }
}