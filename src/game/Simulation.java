package game;

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

    public Simulation(int numJuniors, int numSeniors, int initialBudget) {
        this.gameBoard = new GameBoard();
        this.agents = new ArrayList<>();
        this.budget = initialBudget;
        this.stepCount = 0;
        this.isRunning = true;

        Random rand = new Random();

        //stworzenie szefa
        String bossName = names[rand.nextInt(names.length)];
        Boss boss = new Boss(bossName, 2, 3);
        boss.setName(bossName);
        agents.add(boss);
        gameBoard.getCell(2, 3).setAgent(boss);
        System.out.println("Stworzono szefa " + bossName + ".");

        //max ilość agentów - jeśli podamy więcej, niż limit to wybierze mniejszą liczbę z nawiasu
        int seniorsToCreate = Math.min(numSeniors, 5);
        int juniorsToCreate = Math.min(numJuniors, 10);

        //stworzenie pracowników
        createWorkers(seniorsToCreate, "game.Senior", rand);
        createWorkers(juniorsToCreate, "game.Junior", rand);

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
                if (type.equalsIgnoreCase("game.Junior")) {
                    // game.Junior - doświadczenie od 10% do 40%
                    exp = 0.1 + (0.4 - 0.1) * rand.nextDouble();
                    w = new Junior(freeDesk.getX(), freeDesk.getY(), eff, exp);
                } else {
                    // game.Senior - doświadczenie od 60% do 95%
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
        if (!isRunning) return; //czyli jak nic sie nie dzieje to nic nie rob
        stepCount++;
        System.out.println("--- TURA " + stepCount + " ---");

        //kazdy agent wykonuje swoj jeden ruch
        for (Agent agent : agents) {
            agent.act(gameBoard);
        }
        //TUTAJ TRZEBA BEDZIE DAC BUDZET I WYDATKI ITP

    }


    //do zrobienia tutaj
    //metoda step() -> tury ZROBIONA - K
    //metoda z mechanizmem wypłat
    //metoda checkBudget()

    //mozna sie zastanowic nad osobna klasa Budget (musimy sie zastanowic bo bardziej clean by bylo z osobna imo) no i przy wykresach itd bedzie trzeba zrobic klase SimulationView
    //ogolnie jak na koncu to bedziemy oddawac to trzeba bedzie troche poprawic te nasze dokumentacje wszystkie zeby sie zgadzalo
}
