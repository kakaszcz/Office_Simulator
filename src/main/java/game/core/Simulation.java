package game.core;

import game.agents.Agent;
import game.agents.Boss;
import game.agents.Junior;
import game.agents.Worker;
import game.model.*;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Główny silnik symulacji (klasa menedżerska fasady biznesowej).
 * Zarządza cyklem życia wszystkich agentów w biurze, kontroluje upływ tur (kroków),
 * obsługuje finanse (budżet, pensje, kary), dystrybuuje zadania oraz
 * monitoruje globalne statystyki wydajności i błędów aplikacji.
 */
public class Simulation {

    private GameBoard gameBoard;
    private List<Agent> agents;
    private double budget;

    private int stepCount;
    private boolean isRunning;
    private int totalFails = 0;

    private int totalCoffeesDrank = 0;
    private int totalTearsShed = 0;
    private int totalTasksSuccess = 0;
    private int totalTasksFailed = 0;
    private int totalCigarettesSmoked = 0;
    private int totalFatalErrors = 0;

    private AgentFactory factory;
    private HRManager hrManager;
    private MainApp mainApp;

    /**
     * Tworzy i inicjalizuje strukturę nowego środowiska symulacji biura IT.
     * Powołuje do życia Szefa w jego gabinecie, rozmieszcza startowych pracowników
     * przy wolnych biurkach oraz rejestruje ich w module HRManager.
     *
     * @param numJuniors Początkowa liczba pracowników poziomu Junior (ograniczona limitem konfiguracji).
     * @param numSeniors Początkowa liczba pracowników poziomu Senior (ograniczona limitem konfiguracji).
     * @param initialBudget Początkowy stan kapitału zakładowego firmy.
     */
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
        }

        int seniorsToCreate = Math.min(numSeniors, GameConfiguration.MAX_SENIORS);
        int juniorsToCreate = Math.min(numJuniors, GameConfiguration.MAX_JUNIORS);

        createWorkers(seniorsToCreate, "Senior");
        createWorkers(juniorsToCreate, "Junior");

        this.hrManager = new HRManager();

        for (Agent agent : this.agents) {
            if (agent instanceof game.agents.Worker) {
                this.hrManager.registerHire(agent); //tworzenie kartoteki
            }
        }
    }

    /**
     * Rejestruje główną instancję aplikacji JavaFX w celu umożliwienia
     * wywoływania ekranów powiadomień i końca gry.
     *
     * @param mainApp Instancja klasy uruchomieniowej MainApp.
     */
    public void setMainApp(MainApp mainApp) { this.mainApp = mainApp; }

    /**
     * Sprawdza, czy proces symulacji jest aktualnie aktywny.
     *
     * @return true, jeśli symulacja działa; false w przypadku zatrzymania lub końca gry.
     */
    public boolean isRunning() { return this.isRunning; }

    /**
     * Wstrzymuje flagę działania pętli głównej symulacji.
     */
    public void stop() {
        this.isRunning = false;
    }

    /**
     * Pomocnicza fabryka rozlokowująca określoną liczbę pracowników danego typu
     * przy pierwszych wolnych kafelkach oznaczonych jako biurka.
     *
     * @param num Liczba pracowników do wygenerowania.
     * @param type Typ pracownika (np. "Junior" lub "Senior").
     */
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
            }
        }
    }

    /**
     * Pobiera logiczną planszę gry.
     *
     * @return Obiekt typu GameBoard reprezentujący mapę biura.
     */
    public GameBoard getGameBoard() { return gameBoard; }

    /**
     * Główna metoda wykonawcza pojedynczego kroku (tury) symulacji.
     * Odpowiada za sekwencyjne rozliczanie pensji pracowniczych, dystrybucję nowych zadań,
     * weryfikację pozycji agentów, wywołanie ich logiki wewnętrznej (stanów) oraz procesy HR.
     */
    public void step() {
        if (!isRunning) return;
        stepCount++;

        System.out.println(">>> TURA " + stepCount + " | Stan konta: " + String.format("%.2f", this.budget));

        // 1. ROZLICZANIE PENSJI
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

            if (this.budget <= 0) {
                this.isRunning = false;
                if (mainApp != null) {
                    javafx.application.Platform.runLater(() -> {
                        mainApp.showGameOverScreen("B A N K R U C T W O !\n\nFirma nie miała z czego zapłacić pracownikom.");
                    });
                }
                return;
            }
        }

        // 2. Dystrybucja zadań projektowych i weryfikacja położenia pracowników
        if (stepCount % GameConfiguration.TASK_DISTRIBUTION_INTERVAL == 0) {
            System.out.println(">>> [Manager] Rozdzielanie nowej puli zadań w tej turze!");
            for (Agent agent : this.agents) {
                if (agent instanceof Worker) {
                    Worker worker = (Worker) agent;
                    Cell currentCell = gameBoard.getCell(worker.getX(), worker.getY());
                    String currentState = worker.getCurrentStateName();

                    // Jeśli pracownik NIE stoi na biurku, nie ma prawa pracować ani czekać na zadanie
                    if (currentCell != null && !GameConfiguration.TILE_TYPE_DESK.equalsIgnoreCase(currentCell.getType())) {
                        if ("WorkingState".equalsIgnoreCase(currentState) || "WaitingForTaskState".equalsIgnoreCase(currentState)) {
                            System.out.println(">>> [Korekta HR] " + worker.getName() + " złapany w strefie '" + currentCell.getType() + "'! Wymuszony marsz do biurka.");
                            worker.setHasTask(false); // Resetujemy zadanie, jeśli wisiało w locie
                            worker.changeState(new game.states.MovingToDeskState());
                            continue; // Przejdź do następnego agenta, tego ignorujemy w tej turze
                        }
                    }

                    // Bezpieczny przydział: Tylko dla tych, którzy fizycznie siedzą na biurku i grzecznie czekają
                    if (!worker.hasTask() && "WaitingForTaskState".equalsIgnoreCase(currentState)) {
                        if (currentCell != null && GameConfiguration.TILE_TYPE_DESK.equalsIgnoreCase(currentCell.getType())) {
                            worker.assignTask();
                            worker.changeState(new game.states.WorkingState());
                            System.out.println("   -> [Sukces] " + worker.getName() + " siedzi przy biurku, dostał zadanie i zaczyna kodować!");
                        } else {
                            // Jeśli jakimś cudem ominął poprzedni filtr, a nie jest przy biurku - zawracamy go
                            worker.changeState(new game.states.MovingToDeskState());
                        }
                    }
                }
            }
        }

        List<Agent> agentsToFire = new ArrayList<>();
        List<Agent> juniorsToHire = new ArrayList<>();

        // 3. Aktualizacja zachowania i stanów autonomicznych agentów
        for (Agent agent : this.agents) {
            agent.act(gameBoard, this);
            if (agent instanceof Worker && ((Worker) agent).shouldBeFired()) {
                agentsToFire.add(agent);
            }
        }

        // 4. Obsługa procesów kadrowych (Zwalnianie i rekrutacja zastępcza Juniorów)
        for (Agent firedAgent : agentsToFire) {
            this.removeAgent(firedAgent);
            this.hrManager.registerFire(firedAgent);

            if (firedAgent instanceof Junior) {
                Cell freeDesk = this.gameBoard.findFirstEmptyCell("desk");
                if (freeDesk != null) {
                    Junior newJunior = this.factory.createJunior(freeDesk.getX(), freeDesk.getY());
                    freeDesk.setAgent(newJunior);
                    juniorsToHire.add(newJunior);
                    this.hrManager.registerHire(newJunior);
                }
            }
        }

        if (!juniorsToHire.isEmpty()) {
            this.agents.addAll(juniorsToHire);
        }

        this.hrManager.updateAllRecords();
    }

    /**
     * Raportuje wystąpienie błędu popełnionego przez Juniora.
     * Weryfikuje, czy osiągnięto limit awarii wywołujący błąd krytyczny systemu.
     */
    public void reportJuniorFail() {
        this.totalFails++;
        if (this.totalFails >= GameConfiguration.MAX_FAILS_LIMIT) {
            triggerFatalError();
        }
    }

    /**
     * Zmniejsza licznik bieżących awarii w biurze (wywoływane po udanej naprawie przez Seniora).
     */
    public void repairFail() {
        if (this.totalFails > 0) this.totalFails--;
    }

    /**
     * Aktywuje stan awarii krytycznej (Fatal Error). Nalicza karę finansową,
     * wprowadza Seniorów w stan zdenerwowania (MadState) oraz wymusza animację gniewu u Szefa.
     * W przypadku utraty płynności finansowej wywołuje ekran końca gry.
     */
    private void triggerFatalError() {
        this.totalFatalErrors++;
        double penalty = GameConfiguration.FATAL_ERROR_PENALTY;
        this.budget -= penalty;
        this.totalFails = 0;

        for (Agent agent : this.agents) {
            if (agent instanceof game.agents.Senior) {
                ((game.agents.Worker) agent).changeState(new game.states.MadState());
            }
            if (agent instanceof game.agents.Boss) {
                ((game.agents.Boss) agent).triggerMadAnimation();
            }
        }

        if (this.budget <= 0) {
            this.isRunning = false;
            if (mainApp != null) {
                javafx.application.Platform.runLater(() -> {
                    mainApp.showGameOverScreen("B A N K R U C T W O !\n\nKary za błędy wykończyły budżet firmy.");
                });
            }
        }
    }

    /**
     * Zwiększa aktualny stan konta firmy o podaną kwotę (zarobek za zadanie).
     *
     * @param amount Kwota zysku dodawana do budżetu.
     */
    public void earnMoney(double amount) { this.budget += amount; }

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
    public int getTotalFatalErrors() { return totalFatalErrors; }

    /**
     * Oblicza procentowy współczynnik pomyślnie ukończonych zadań w stosunku do wszystkich oddanych.
     *
     * @return Wartość typu double z zakresu 0.0 - 100.0.
     */
    public double getSuccessRate() {
        int totalTasks = totalTasksSuccess + totalTasksFailed;
        if (totalTasks == 0) return 100.0;
        return ((double) totalTasksSuccess / totalTasks) * 100.0;
    }

    /**
     * Formatuje licznik kroków symulacji na czytelną formę czasu (dni oraz godziny robocze).
     * Zakłada, że na jeden dzień roboczy (od 09:00) składa się 5 tur.
     *
     * @return Sformatowany ciąg tekstowy (np. "Dzień 1, godz. 11:00").
     */
    public String getSimulationTimeFormatted() {
        int turnsPerDay = 5;
        int day = (stepCount / turnsPerDay) + 1;
        int hour = 9 + (stepCount % turnsPerDay);
        return String.format("Dzień %d, godz. %02d:00", day, hour);
    }

    /**
     * Oblicza średnią arytmetyczną wydajności (Efficiency) wszystkich zatrudnionych pracowników.
     *
     * @return Średnia wartość procentowa z zakresu 0.0 - 100.0.
     */
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

    public double getAverageOfficeEfficiency() { return getAverageEfficiency(); }

    /**
     * Całkowicie usuwa wskazanego agenta z listy aktywnych obiektów symulacji
     * oraz czyści referencję do niego na powiązanym kafelku planszy.
     *
     * @param agent Obiekt agenta przeznaczony do usunięcia.
     */
    public void removeAgent(Agent agent) {
        if (this.agents.contains(agent)) {
            this.agents.remove(agent);
            Cell agentCell = this.gameBoard.getCell(agent.getX(), agent.getY());
            if (agentCell != null && agentCell.getAgent() == agent) {
                agentCell.setAgent(null);
            } else {
                for (int x = 0; x < gameBoard.getWidth(); x++) {
                    for (int y = 0; y < gameBoard.getHeight(); y++) {
                        Cell cell = gameBoard.getCell(x, y);
                        if (cell != null && cell.getAgent() == agent) cell.setAgent(null);
                    }
                }
            }
        }
    }

    /**
     * Wyszukuje i zwraca obiekt Szefa zarządzającego biurem.
     *
     * @return Instancja klasy Boss lub null, jeśli szef nie został odnaleziony.
     */
    public Boss getBoss() {
        for (Agent agent : this.agents) {
            if (agent instanceof Boss) return (Boss) agent;
        }
        return null;
    }

    public HRManager getHRManager() { return this.hrManager; }
    public void incrementCigarettes() { this.totalCigarettesSmoked++; }
    public int getTotalCigarettesSmoked() { return this.totalCigarettesSmoked; }
}