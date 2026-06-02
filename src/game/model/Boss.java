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

        // 1. ZNALEZIENIE CELU (Porównanie budżetu i sprawdzenie kawy)
        Cell target = chooseTarget(board, sim);

        // 2. RUCH W STRONĘ CELU (Wywolanie metody moveTo)
        if (target != null) {
            moveTo(target, board);
        }

        // Jeśli szef wszedł na pole z kawą i minął czas -> pije kawę i resetuje timer
        if (this.coffeeTimer >= 10 && board.getCell(getX(), getY()).getType().equals("coffee")) {
            this.coffeeTimer = 0;
            System.out.println("Szef " + this.getName() + " wypił kawę. Timer zresetowany!");
        }

        // 3. SPRAWDZENIE SĄSIADÓW (Czy kogoś zwolnić?)
        checkNeighborsAndFire(board, sim);

        // Zapisanie budżetu na kolejną turę
        this.previousBudget = sim.getBudget();
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

    public void moveTo(Cell target, GameBoard board) {
        if (getX() == target.getX() && getY() == target.getY()) return; // Szef jest na miejscu

        // Proste obliczenie kierunku (o 1 pole w stronę celu)
        int nextX = getX() + Integer.compare(target.getX(), getX());
        int nextY = getY() + Integer.compare(target.getY(), getY());

        // Próba ruchu przez planszę (silnik GameBoard sprawdza, czy pole nie jest ścianą)
        if (board.moveAgent(getX(), getY(), nextX, nextY)) {
            setX(nextX);
            setY(nextY);
        }
    }

    private void checkNeighborsAndFire(GameBoard board, Simulation sim) {
        // [3] Sprawdzenie sąsiadów - pracownicy reagują sami na szefa, my sprawdzamy tylko zwalnianie
        int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}}; // Góra, dół, prawo, lewo

        for (int[] dir : directions) {
            int checkX = getX() + dir[0];
            int checkY = getY() + dir[1];

            // Zabezpieczenie przed wyjściem poza mapę
            if (checkX >= 0 && checkX < board.getWidth() && checkY >= 0 && checkY < board.getHeight()) {
                Agent neighbor = board.getCell(checkX, checkY).getAgent();

                if (neighbor instanceof Worker) {
                    Worker worker = (Worker) neighbor;
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

        // Mediator usuwa go z gry
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