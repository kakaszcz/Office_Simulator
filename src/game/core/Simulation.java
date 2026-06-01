package game.core;

import game.model.Worker;
import game.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class Simulation {
    //public static void main(String[] args) {
    //System.out.println("helool");

    private GameBoard gameBoard;
    private List<Agent> agents;
    private String[] names = {"Mateusz", "Karolina", "Wiktoria", "Jan", "Anna", "Piotr", "Maria", "Krzysztof", "Kasia", "Tomasz", "Magda", "Michał", "Gienek", "Sebastian", "Brajan", "Artur",  "Zofia", "Marek", "Barbara", "Adam", "Ewa", "Paweł", "Małgorzata", "Robert"};

    private double budget;
    private int stepCount;
    private boolean isRunning;

    // NOWE POLA
    private int totalFails = 0;

    public Simulation(int numJuniors, int numSeniors, int initialBudget) {
        this.gameBoard = new GameBoard();
        this.agents = new ArrayList<>();
        this.budget = initialBudget;
        this.stepCount = 0;
        this.isRunning = true;

        Random rand = new Random();

        // Stworzenie szefa
        String bossName = names[rand.nextInt(names.length)];
        Boss boss = new Boss(bossName, 2, 3, initialBudget);
        agents.add(boss);
        gameBoard.getCell(2, 3).setAgent(boss);
        System.out.println("Stworzono szefa " + bossName + ".");

        int seniorsToCreate = Math.min(numSeniors, GameConfiguration.MAX_SENIORS);
        int juniorsToCreate = Math.min(numJuniors, GameConfiguration.MAX_JUNIORS);

        // Stworzenie pracowników
        createWorkers(seniorsToCreate, "game.model.Senior", rand);
        createWorkers(juniorsToCreate, "game.model.Junior", rand);
    }

    private void createWorkers (int num, String type, Random rand) {
         /*
    Ta część kodu odpowiada za przydzielanie biurek pracownikom, po kolej
    W poprzedniej klasie wyszukuje biurek, a tu przydziela miejsca
     */

        for (int i = 0; i < num; i++) {
            Cell freeDesk = gameBoard.findFirstEmptyCell("desk");

            if (freeDesk != null) {
                String randomName = names[rand.nextInt(names.length)];

                //wydajność 0.4 - 0.9
                double eff = 0.4 + (0.5) * rand.nextDouble();
                double exp;

                Worker w;
                if (type.endsWith("Junior")) {
                    // game.model.Junior - doświadczenie od 10% do 40%
                    exp = 0.1 + (0.4 - 0.1) * rand.nextDouble();
                    w = new Junior(freeDesk.getX(), freeDesk.getY(), eff, exp);
                } else {
                    // game.model.Senior - doświadczenie od 60% do 95%
                    exp = 0.6 + (0.95 - 0.6) * rand.nextDouble();
                    w = new Senior(freeDesk.getX(), freeDesk.getY(), eff, exp);
                }

                w.setName(randomName);
                agents.add(w);
                freeDesk.setAgent(w);

                //log o utworzeniu pracownika
                System.out.println("Stworzono " + type + " o imieniu " + randomName + " (Doświadczenie: "
                        + String.format("%.2f", exp) + " Wydajność:" + String.format("%.2f", eff) + ")");
            }
        }
    }

    //============ GETTER PLANSZY potrzebny gameview

    public GameBoard getGameBoard() {
        return gameBoard;
    }

    //=============== GŁÓWNA PĘTLA CZASU
    public void step() {
        if (!isRunning) return;
        stepCount++;
        System.out.println("--- TURA " + stepCount + " ---");

        // Tworzymy kopię listy agentów na czas trwania tej tury
        List<Agent> agentsCopy = new ArrayList<>(this.agents);

        for (Agent agent : agentsCopy) {
            // Sprawdzamy, czy agent nie został zwolniony w tej samej turze (np. przez Szefa, który ruszał się wcześniej)
            if (this.agents.contains(agent)) {
                agent.act(gameBoard, this);
            }
        }
        // TUTAJ TRZEBA BEDZIE DAC BUDZET I WYDATKI ITP
    }

    // Metoda wywoływana, gdy Junior popełni błąd
    public void reportJuniorFail() {
        this.totalFails++;
        System.out.println("!!! Wykryto błąd Juniora. Aktualna liczba błędów w firmie: " + totalFails);

        // Używamy stałej konfiguracyjnej
        if (this.totalFails >= GameConfiguration.MAX_FAILS_LIMIT) {
            triggerFatalError();
        }
    }

    // Metoda wywoływana, gdy Senior naprawi błąd
    public void repairFail() {
        if (this.totalFails > 0) {
            this.totalFails--;
            System.out.println("=== Senior naprawił błąd! Aktualna liczba błędów w firmie: " + totalFails);
        }
    }

    // Obsługa krytycznego błędu i kary finansowej
    private void triggerFatalError() {
        double penalty = GameConfiguration.FATAL_ERROR_PENALTY;
        this.budget -= penalty;
        this.totalFails = 0; // Resetujemy licznik po naliczeniu kary (lub zgodnie z zasadami gry)
        System.out.println("FATAL ERROR! Firma płaci karę: " + penalty + "$. Aktualny budżet: " + this.budget);

        if (this.budget <= 0) {
            System.out.println("BANKRUCTWO! Koniec gry.");
            this.isRunning = false;
        }
    }

    public int getTotalFails() { return totalFails; }

    public double getBudget() {
        return this.budget;
    }

    public List<Agent> getAgents() {
        return this.agents;
    }

    public void removeAgent(Agent agent) {
        if (this.agents.contains(agent)) {
            this.agents.remove(agent);
            System.out.println(agent.getName() + " został zwolniony.");
        }
    }

    //do zrobienia tutaj
    //metoda step() -> tury ZROBIONA - K
    //metoda z mechanizmem wypłat
    //metoda checkBudget()

    //mozna sie zastanowic nad osobna klasa Budget (musimy sie zastanowic bo bardziej clean by bylo z osobna imo) no i przy wykresach itd bedzie trzeba zrobic klase SimulationView
    //ogolnie jak na koncu to bedziemy oddawac to trzeba bedzie troche poprawic te nasze dokumentacje wszystkie zeby sie zgadzalo
}
