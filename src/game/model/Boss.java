package game.model;

import game.core.Simulation;

public class Boss extends Agent {

    private int controlRange;
    private int coffeeTimer;
    private double previousBudget;

    public Boss(String name, int x, int y, double initialBudget) {
        super(x, y);
        this.setName(name);
        this.controlRange = 1;
        this.coffeeTimer = 0;
        this.previousBudget = initialBudget;
    }

    @Override
    public void act(GameBoard board, Simulation sim) {
        this.coffeeTimer++;

        // 1. KONTROLA KAWY - Głód kawowy zawsze wygrywa
        if (this.coffeeTimer >= 10) {
            Cell coffeeTarget = board.findFirstEmptyCell("coffee");
            if (coffeeTarget != null) {
                moveToTarget(coffeeTarget, board);
            }
        } else {
            // 2. DYNAMICZNA SZANSA NA SPACER (Zależna od budżetu)
            double chanceToMove = 0.40; // Domyślnie 40% szans na ruch (szef odpoczywa w gabinecie)

            // Jeśli obecny budżet jest mniejszy niż w poprzedniej turze -> Szef jest zły
            if (sim.getBudget() < previousBudget) {
                chanceToMove = 0.90; // Szansa wzrasta do 90% – szef natychmiast rusza na patrol!
                System.out.println("Szef " + this.getName() + " zauważył spadek budżetu! Rusza na intensywny patrol (90% szans na ruch).");
            }

            // Losujemy, czy w tej turze szef faktycznie zrobi krok
            if (Math.random() < chanceToMove) {
                moveRandomly(board);
            } else {
                System.out.println("Szef " + this.getName() + " relaksuje się w gabinecie...");
            }
        }

        // Jeśli szef wszedł na pole z kawą i minął czas -> pije kawę i resetuje timer
        if (this.coffeeTimer >= 10 && board.getCell(getX(), getY()).getType().equalsIgnoreCase("coffee")) {
            this.coffeeTimer = 0;
            System.out.println("Szef " + this.getName() + " wypił kawę. Timer zresetowany!");
        }

        // 3. SPRAWDZENIE SĄSIADÓW (Czy kogoś zwolnić?)
        checkNeighborsAndFire(board, sim);

        // Zapisanie budżetu na kolejną turę (kluczowe do wykrycia spadku w następnym kroku!)
        this.previousBudget = sim.getBudget();
    }

    private void moveRandomly(GameBoard board) {
        int[][] directions = {
                {0, 1}, {0, -1}, {1, 0}, {-1, 0},
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
        };

        java.util.List<Integer> indices = new java.util.ArrayList<>();
        for (int i = 0; i < directions.length; i++) indices.add(i);
        java.util.Collections.shuffle(indices);

        for (int index : indices) {
            int nextX = getX() + directions[index][0];
            int nextY = getY() + directions[index][1];

            if (nextX >= 0 && nextX < board.getWidth() && nextY >= 0 && nextY < board.getHeight()) {
                if (board.moveAgent(getX(), getY(), nextX, nextY)) {
                    setX(nextX);
                    setY(nextY);
                    return;
                }
            }
        }
    }

    private Cell chooseTarget(GameBoard board, Simulation sim) {
        // [1] Sprawdzenie coffeeTimer >= 10 -> idź do coffeeTable
        if (this.coffeeTimer >= 10) {
            return board.findFirstEmptyCell("coffee");
        }

        // [2] Porównanie previousBudget z obecnym
        if (sim.getBudget() < previousBudget) {
            // Budżet spadł -> Szef jest zły, idzie szukać Juniora
            return findWorker(sim, Junior.class, board);
        } else {
            // Budżet jest OK -> Szef idzie do Seniora
            return findWorker(sim, Senior.class, board);
        }
    }

    // Metoda celowanego poruszania się (używana tylko, gdy szef idzie prosto do kawy)
    private void moveToTarget(Cell target, GameBoard board) {
        if (getX() == target.getX() && getY() == target.getY()) return;

        int nextX = getX() + Integer.compare(target.getX(), getX());
        int nextY = getY() + Integer.compare(target.getY(), getY());

        if (board.moveAgent(getX(), getY(), nextX, nextY)) {
            setX(nextX);
            setY(nextY);
        }
    }

    private void checkNeighborsAndFire(GameBoard board, Simulation sim) {
        // [3] Sprawdzenie sąsiadów - pracownicy reagują sami na szefa, my sprawdzamy tylko zwalnianie
        int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0},
                {1, 1},  {1, -1}, {-1, 1}, {-1, -1}}; // Góra, dół, prawo, lewo, skosy

        for (int[] dir : directions) {
            int checkX = getX() + dir[0];
            int checkY = getY() + dir[1];

            // Zabezpieczenie przed wyjściem poza mapę
            if (checkX >= 0 && checkX < board.getWidth() && checkY >= 0 && checkY < board.getHeight()) {

                // Pobieramy cały kafelek sąsiada z planszy
                Cell neighborCell = board.getCell(checkX, checkY);

                //  Z pobranego kafelka wyciągamy stojącego tam agenta
                Agent neighbor = neighborCell.getAgent();

                if (neighbor instanceof Worker) {
                    Worker worker = (Worker) neighbor;

                    // Pobieramy nazwę stanu pracownika
                    String state = worker.getCurrentStateName();

                    // Szef go ignoruje – na kawie panuje immunitet!
                    if ("CoffeeState".equalsIgnoreCase(state) ||
                            "MovingToRestState".equalsIgnoreCase(state) ||
                            neighborCell.getType().equalsIgnoreCase("coffee")) {
                        continue; // Przeskakujemy tego pracownika, szef udaje, że go nie widzi
                    }
                    if (worker.shouldBeFired()) {
                        fireWorker(worker, board, sim);
                    }
                }
            }
        }
    }

    public void fireWorker(Worker worker, GameBoard board, Simulation sim) {
        System.out.println("!!! SZEF " + this.getName() + " ZWALNIA PRACOWNIKA: " + worker.getName() + " !!!");

        // Zwalniamy miejsce na planszy
        board.getCell(worker.getX(), worker.getY()).setAgent(null);

        // usuwa go z gry
        sim.removeAgent(worker);
    }

    // Pomocnicza metoda szukająca pracownika danego typu
    private Cell findWorker(Simulation sim, Class<? extends Worker> workerClass, GameBoard board) {
        for (Agent agent : sim.getAgents()) {
            if (workerClass.isInstance(agent)) {
                return board.getCell(agent.getX(), agent.getY());
            }
        }
        // Jeśli nie znalazł (np. wszyscy juniorzy zwolnieni), wraca do gabinetu
        return board.findFirstEmptyCell("boss_office");
    }
}