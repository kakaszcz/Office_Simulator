package game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class Simulation {
    //public static void main(String[] args) {
    //System.out.println("helool");

    public enum WorkerType { JUNIOR, SENIOR }

    private GameBoard gameBoard;
    private List<Agent> agents;
    private AgentFactory agentFactory;

    private double budget;
    private int stepCount;
    private boolean isRunning;

    public Simulation(int numJuniors, int numSeniors, int initialBudget) {
        this.gameBoard = new GameBoard();
        this.agents = new ArrayList<>();
        this.budget = initialBudget;
        this.stepCount = 0;
        this.isRunning = true;

        Random rand = new Random();

        // 1. Stworzenie szefa
        Boss boss = agentFactory.createBoss(2, 3);
        agents.add(boss);
        gameBoard.getCell(2, 3).setAgent(boss);
        System.out.println("Stworzono szefa " + boss.getName() + ".");

        // 2. Limity agentów
        int seniorsToCreate = Math.min(numSeniors, 5);
        int juniorsToCreate = Math.min(numJuniors, 10);

        // 3. Stworzenie pracowników przy użyciu Enuma
        createWorkers(seniorsToCreate, WorkerType.SENIOR);
        createWorkers(juniorsToCreate, WorkerType.JUNIOR);

    }

    private void createWorkers(int num, WorkerType type) {
        for (int i = 0; i < num; i++) {
            Cell freeDesk = gameBoard.findFirstEmptyCell("desk");

            if (freeDesk != null) {
                Worker w = null;

                // Fabryka zajmuje się detalami, my tylko decydujemy jakiego typu potrzebujemy
                if (type == WorkerType.JUNIOR) {
                    w = agentFactory.createJunior(freeDesk.getX(), freeDesk.getY());
                } else if (type == WorkerType.SENIOR) {
                    w = agentFactory.createSenior(freeDesk.getX(), freeDesk.getY());
                }

                if (w != null) {
                    agents.add(w);
                    freeDesk.setAgent(w);
                    System.out.println("Stworzono " + type + " o imieniu " + w.getName() + ".");
                }
            } else {
                System.out.println("Brak wolnych biurek dla " + type + "!");
                break; // Skoro nie ma wolnych biurek, przerywamy pętlę
            }
        }
    }

    //do zrobienia tutaj
    //metoda step() -> tury
    //metoda z mechanizmem wypłat
    //metoda checkBudget()

    //mozna sie zastanowic nad osobna klasa Budget (musimy sie zastanowic bo bardziej clean by bylo z osobna imo) no i przy wykresach itd bedzie trzeba zrobic klase SimulationView
    //ogolnie jak na koncu to bedziemy oddawac to trzeba bedzie troche poprawic te nasze dokumentacje wszystkie zeby sie zgadzalo
}
