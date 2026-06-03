package game.core;

import game.model.Worker;
import game.model.*;
import game.model.AgentFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList; // NOWY IMPORT!

public class Simulation {

    private GameBoard gameBoard;
    private List<Agent> agents; // Ta lista będzie teraz typu CopyOnWriteArrayList
    private double budget;
    private int stepCount;
    private boolean isRunning;
    private int totalFails = 0;

    private AgentFactory factory;

    public Simulation(int numJuniors, int numSeniors, int initialBudget) {
        this.gameBoard = new GameBoard();
        // ZMIANA: Zastąpienie ArrayList
        this.agents = new CopyOnWriteArrayList<>();
        this.budget = initialBudget;
        this.stepCount = 0;
        this.isRunning = true;

        this.factory = new AgentFactory();

        Boss boss = factory.createBoss(2, 3, initialBudget);
        agents.add(boss);
        gameBoard.getCell(2, 3).setAgent(boss);
        System.out.println("Stworzono szefa " + boss.getName() + ".");

        int seniorsToCreate = Math.min(numSeniors, GameConfiguration.MAX_SENIORS);
        int juniorsToCreate = Math.min(numJuniors, GameConfiguration.MAX_JUNIORS);

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
        System.out.println("============= TURA " + stepCount + "|Stan konta:" + this.budget + "$" + " =============");

        // =========================================================================
        // 1. ROZDZIELANIE ZADAŃ (Co 3 tury)
        // =========================================================================
        if (stepCount % 3 == 0) {
            System.out.println(">>> [Manager] Rozdzielanie nowej puli zadań w tej turze!");

            for (Agent agent : this.agents) {
                if (agent instanceof Worker) {
                    Worker worker = (Worker) agent;

                    if (!worker.hasTask() && "WaitingForTaskState".equalsIgnoreCase(worker.getCurrentStateName())) {
                        worker.assignTask();
                        System.out.println("   -> Zadanie przydzielone dla: " + worker.getName());
                    }
                }
            }
        }

        // =========================================================================
        // RUCH AGENTÓW ORAZ LOGIKA ZWALNIANIA / REKRUTACJI (W KAŻDEJ TURZE)
        // =========================================================================
        for (Agent agent : this.agents) {

            // Każdy agent wykonuje swoją akcję / ruch (Szef może teraz bezkarnie dawać boosty i zmieniać stany innych)
            agent.act(gameBoard, this);

            // Sprawdzamy, czy po wykonaniu akcji agent powinien zostać zwolniony
            if (agent instanceof Worker && ((Worker) agent).shouldBeFired()) {
                System.out.println("!!! " + agent.getName() + " zostaje trwale wymazany z rejestru firmy.");

                // Zwalniamy krzesło na planszy
                Cell cell = this.gameBoard.getCell(agent.getX(), agent.getY());
                if (cell != null && cell.getAgent() == agent) {
                    cell.setAgent(null);
                }

                // natychmiastowe usunięcie z listy
                this.agents.remove(agent);

                // Automatyczne zatrudnienie nowego Juniora na to miejsce
                if (agent instanceof Junior) {
                    System.out.println(">>> [HR] Rozpoczęto rekrutację na miejsce zwolnionego Juniora...");

                    Cell freeDesk = this.gameBoard.findFirstEmptyCell("desk");
                    if (freeDesk != null) {
                        Junior newJunior = this.factory.createJunior(freeDesk.getX(), freeDesk.getY());
                        freeDesk.setAgent(newJunior);

                        // ZMIANA: Wrzucamy nowego rekruta bezpośrednio do głównej listy agents!
                        this.agents.add(newJunior);

                        System.out.println(">>> [HR] Zatrudniono nowego Juniora: " + newJunior.getName() + "!");
                    } else {
                        System.out.println(">>> [HR] OSTRZEŻENIE: Brak wolnych biurek! Nie można zatrudnić zastępstwa.");
                    }
                }
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

    public void earnMoney(double amount) {
        this.budget += amount;
        System.out.println("$$$ Wpływ na konto: +" + amount + "$. Aktualny budżet: " + this.budget + "$");
    }

    public int getTotalFails() { return totalFails; }
    public double getBudget() { return this.budget; }
    public List<Agent> getAgents() { return this.agents; }

    public void removeAgent(Agent agent) {
        if (this.agents.contains(agent)) {
            this.agents.remove(agent);

            // czyścimy KAŻDY kafelek na planszy, który przypadkiem wciąż trzyma tego agenta
            for (int x = 0; x < gameBoard.getWidth(); x++) {
                for (int y = 0; y < gameBoard.getHeight(); y++) {
                    Cell cell = gameBoard.getCell(x, y);
                    if (cell != null && cell.getAgent() == agent) {
                        cell.setAgent(null); // Całkowite wyczyszczenie (biurka, korytarza, czegokolwiek)
                    }
                }
            }

            System.out.println(agent.getName() + " został zwolniony.");
        }
    }
}