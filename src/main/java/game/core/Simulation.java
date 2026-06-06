package game.core;

import game.agents.Agent;
import game.agents.Boss;
import game.agents.Junior;
import game.agents.Worker;
import game.model.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Simulation {

    private GameBoard gameBoard;
    private List<Agent> agents;
    private double budget;

    // LICZNIKI STATYSTYK
    private int stepCount;              // Główny i jedyny licznik tur
    private boolean isRunning;
    private int totalFails = 0;         // Bieżące, naprawialne błędy juniorów na planszy

    private int totalCoffeesDrank = 0;  // Łączna liczba wypitych kaw
    private int totalTearsShed = 0;     // Łączna liczba wylanych łez
    private int totalTasksSuccess = 0;  // Historyczna liczba udanych zadań
    private int totalTasksFailed = 0;   // Historyczna liczba nieudanych zadań
    private int totalCigarettesSmoked = 0;

    private AgentFactory factory;
    private HRManager hrManager;

    // NOWE POLE: Referencja do aplikacji okienkowej
    private MainApp mainApp;

    public Simulation(int numJuniors, int numSeniors, int initialBudget) {
        this.gameBoard = new GameBoard();
        this.agents = new CopyOnWriteArrayList<>();
        this.budget = initialBudget;
        this.stepCount = 0;
        this.isRunning = true;
        this.factory = new AgentFactory();

        Boss boss = factory.createBoss(14, 1, initialBudget);
        Cell bossCell = gameBoard.getCell(14, 1);

        if (bossCell != null) {
            agents.add(boss);
            bossCell.setAgent(boss);
            System.out.println("Stworzono szefa " + boss.getName() + " w gabinecie (14,1).");
        } else {
            System.out.println("OSTRZEŻENIE: Nie można umieścić Szefa na pozycji (14,1). Kafelek nie istnieje!");
        }

        int seniorsToCreate = Math.min(numSeniors, GameConfiguration.MAX_SENIORS);
        int juniorsToCreate = Math.min(numJuniors, GameConfiguration.MAX_JUNIORS);

        createWorkers(seniorsToCreate, "Senior");
        createWorkers(juniorsToCreate, "Junior");

        this.hrManager = new HRManager();

        for (Agent agent : this.agents) {
            if (agent instanceof game.agents.Worker) {
                this.hrManager.registerHire(agent);
            }
        }
    }

    // NOWA METODA: Pozwala powiązać symulację z widokiem MainApp
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    // NOWA METODA: Naprawia błąd kompilacji w MainApp.java!
    public boolean isRunning() {
        return this.isRunning;
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

    public GameBoard getGameBoard() { return gameBoard; }

    public void step() {
        if (!isRunning) return;
        stepCount++;

        System.out.println(">>> TURA " + stepCount + " | Stan konta: " + String.format("%.2f", this.budget));

        if (stepCount % GameConfiguration.PAYDAY_INTERVAL == 0) {
            double totalSalaries = 0.0;

            for (Agent agent : this.agents) {
                if (agent instanceof game.agents.Senior) {
                    totalSalaries += GameConfiguration.SALARY_SENIOR;
                } else if (agent instanceof game.agents.Junior) {
                    totalSalaries += GameConfiguration.SALARY_JUNIOR;
                }
            }

            this.budget -= totalSalaries;
            System.out.println(">>> [Księgowość] Dzień wypłaty! Pobrano z konta łącznie: " + String.format("%.2f", totalSalaries) + "$ na pensje.");

            if (this.budget <= 0) {
                System.out.println("BANKRUCTWO! Firma nie miała z czego zapłacić pracownikom. Koniec gry.");
                this.isRunning = false;

                // ODPAŚĆ EKRAN GAME OVER
                if (mainApp != null) {
                    javafx.application.Platform.runLater(() -> {
                        mainApp.showGameOverScreen("B A N K R U C T W O !\n\nFirma nie miała z czego zapłacić pracownikom.");
                    });
                }
                return;
            }
        }

        if (stepCount % GameConfiguration.TASK_DISTRIBUTION_INTERVAL == 0) {
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

        // RUCH AGENTÓW ORAZ LOGIKA ZWALNIANIA / REKRUTACJI
        for (Agent agent : this.agents) {
            agent.act(gameBoard, this);

            if (agent instanceof Worker && ((Worker) agent).shouldBeFired()) {
                System.out.println("!!! " + agent.getName() + " zostaje trwale wymazany z rejestru firmy.");

                this.removeAgent(agent);
                this.hrManager.registerFire(agent);

                if (agent instanceof Junior) {
                    System.out.println(">>> [HR] Rozpoczęto rekrutację na miejsce zwolnionego Juniora...");

                    Cell freeDesk = this.gameBoard.findFirstEmptyCell("desk");
                    if (freeDesk != null) {
                        Junior newJunior = this.factory.createJunior(freeDesk.getX(), freeDesk.getY());
                        freeDesk.setAgent(newJunior);
                        this.agents.add(newJunior);
                        this.hrManager.registerHire(newJunior);

                        System.out.println(">>> [HR] Zatrudniono nowego Juniora: " + newJunior.getName() + "!");
                    } else {
                        System.out.println(">>> [HR] OSTRZEŻENIE: Brak wolnych biurek! Nie można zatrudnić zastępstwa.");
                    }
                }
            }
        }

        this.hrManager.updateAllRecords();
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
        System.out.println("FATAL ERROR! Firma płaci karę: " + penalty + "$. Aktualny budżet: " + String.format("%.2f", this.budget));

        if (this.budget <= 0) {
            System.out.println("BANKRUCTWO! Koniec gry.");
            this.isRunning = false;

            // ODPAŚĆ EKRAN GAME OVER
            if (mainApp != null) {
                javafx.application.Platform.runLater(() -> {
                    mainApp.showGameOverScreen("B A N K R U C T W O !\n\nKary za błędy (Fatal Error) wykończyły budżet firmy.");
                });
            }
        }
    }

    public void earnMoney(double amount) {
        this.budget += amount;
        System.out.println("$$$ Wpływ na konto: +" + String.format("%.2f", amount) + "$. Aktualny budżet: " + String.format("%.2f", this.budget) + "$");
    }

    // --- GETTERY I METODY POMOCNICZE DLA PANELU STATYSTYK ---

    public int getStepCount() { return stepCount; }
    public int getTotalTurns() { return stepCount; }
    public double getBudget() { return this.budget; }
    public List<Agent> getAgents() { return this.agents; }
    public int getTotalFails() { return totalFails; }

    public void recordCoffeeDrunk() { this.totalCoffeesDrank++; }
    public void incrementCoffee() { this.totalCoffeesDrank++; }
    public int getCoffeesDrunk() { return this.totalCoffeesDrank; }
    public int getTotalCoffeesDrank() { return this.totalCoffeesDrank; }

    public void incrementTears() { this.totalTearsShed++; }
    public void incrementSuccess() { this.totalTasksSuccess++; }
    public void incrementFailed() { this.totalTasksFailed++; }

    public int getTotalTearsShed() { return totalTearsShed; }
    public int getTotalTasksSuccess() { return totalTasksSuccess; }
    public int getTotalTasksFailed() { return totalTasksFailed; }

    public double getSuccessRate() {
        int totalTasks = totalTasksSuccess + totalTasksFailed;
        if (totalTasks == 0) return 100.0;
        return ((double) totalTasksSuccess / totalTasks) * 100.0;
    }

    public String getSimulationTimeFormatted() {
        int turnsPerDay = 5;
        int day = (stepCount / turnsPerDay) + 1;
        int hour = 9 + (stepCount % turnsPerDay);
        return String.format("Dzień %d, godz. %02d:00", day, hour);
    }

    public double getAverageEfficiency() {
        double total = 0.0;
        int count = 0;
        for (Agent agent : this.agents) {
            if (agent instanceof Worker) {
                total += ((Worker) agent).getEfficiency();
                count++;
            }
        }
        return count == 0 ? 0.0 : (total / count) * 100.0;
    }

    public double getAverageOfficeEfficiency() {
        return getAverageEfficiency();
    }

    public void removeAgent(Agent agent) {
        if (this.agents.contains(agent)) {
            this.agents.remove(agent);

            for (int x = 0; x < gameBoard.getWidth(); x++) {
                for (int y = 0; y < gameBoard.getHeight(); y++) {
                    Cell cell = gameBoard.getCell(x, y);
                    if (cell != null && cell.getAgent() == agent) {
                        cell.setAgent(null);
                    }
                }
            }
            System.out.println(agent.getName() + " został zwolniony.");
        }
    }

    public Boss getBoss() {
        if (this.agents == null) return null;
        for (game.agents.Agent agent : this.agents) {
            if (agent instanceof Boss) {
                return (Boss) agent;
            }
        }
        return null;
    }

    public HRManager getHRManager() { return this.hrManager; }

    public void incrementCigarettes() {
        this.totalCigarettesSmoked++;
    }

    public int getTotalCigarettesSmoked() {
        return this.totalCigarettesSmoked;
    }
}