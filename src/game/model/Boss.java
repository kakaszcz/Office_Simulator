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

        // Pobieramy aktualny kafelek, na którym stoi szef
        Cell currentCell = board.getCell(getX(), getY());

        // 1. KONTROLA KAWY - Czy już stoimy przy ekspresie?
        if (currentCell != null && currentCell.getType().equalsIgnoreCase("coffee")) {
            if (this.coffeeTimer >= 10) {
                this.coffeeTimer = 0;
                System.out.println("Szef " + this.getName() + " wypił kawę na miejscu. Timer zresetowany!");
            }
            // Po napiciu się (lub jeśli jeszcze nie czas) szef może pokręcić się wokół
            moveRandomly(board);
        }
        // Jeśli nie stoi przy kawie, ale bardzo jej chce:
        else if (this.coffeeTimer >= 10) {
            Cell coffeeTarget = board.findFirstEmptyCell("coffee");
            if (coffeeTarget != null) {
                moveToTarget(coffeeTarget, board);
            } else {
                // Jeśli wszystkie ekspresy są okupowane, szef patroluje dalej
                moveRandomly(board);
            }
        }
        // 2. DYNAMICZNA SZANSA NA SPACER (Zależna od budżetu)
        else {
            double chanceToMove = 0.40; // Domyślnie 40% szans na ruch

            // Jeśli budżet spadł -> Szef dostaje szału i patroluje intensywnie
            if (sim.getBudget() < previousBudget) {
                chanceToMove = 0.90;
                System.out.println("Szef " + this.getName() + " zauważył spadek budżetu! Intensywny patrol (90% szans).");
            }

            if (Math.random() < chanceToMove) {
                moveRandomly(board);
            } else {
                System.out.println("Szef " + this.getName() + " relaksuje się w gabinecie...");
            }
        }

        // 3. SPRAWDZENIE SĄSIADÓW
        checkNeighborsAndFire(board, sim);

        // Zapisanie budżetu na kolejną turę
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

            if (board.isInBounds(nextX, nextY)) {
                if (board.moveAgent(getX(), getY(), nextX, nextY)) {
                    setX(nextX);
                    setY(nextY);
                    return;
                }
            }
        }
    }

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
        int[][] directions = {
                {0, 1}, {0, -1}, {1, 0}, {-1, 0},
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
        };

        for (int[] dir : directions) {
            int checkX = getX() + dir[0];
            int checkY = getY() + dir[1];

            if (board.isInBounds(checkX, checkY)) {
                Cell neighborCell = board.getCell(checkX, checkY);
                Agent neighbor = neighborCell.getAgent();

                if (neighbor instanceof Worker) {
                    Worker worker = (Worker) neighbor;
                    String state = worker.getCurrentStateName();

                    // Immunitet na kawie i w drodze do odpoczynku
                    if ("CoffeeState".equalsIgnoreCase(state) ||
                            "MovingToRestState".equalsIgnoreCase(state) ||
                            neighborCell.getType().equalsIgnoreCase("coffee")) {
                        continue;
                    }

                    // ZMIANA: Szef nie usuwa pracownika sam. Oznacza go jako "do zwolnienia".
                    // Centralna pętla Simulation.step() przechwyci to i poprawnie uruchomi rekrutację HR!
                    if (worker.shouldBeFired()) {
                        System.out.println("!!! SZEF " + this.getName() + " PRZYŁAPAŁ PRACOWNIKA " + worker.getName() + " NA ZŁYCH WYNIKACH !!!");
                        worker.markFired();
                    }
                }
            }
        }
    }

    // TODO: Możesz w przyszłości podmienić moveRandomly() w act() na te metody,
    // aby szef celowo ścigał pracowników, kiedy budżet spada!
    private Cell chooseTarget(GameBoard board, Simulation sim) {
        if (this.coffeeTimer >= 10) {
            return board.findFirstEmptyCell("coffee");
        }
        if (sim.getBudget() < previousBudget) {
            return findWorker(sim, Junior.class, board);
        } else {
            return findWorker(sim, Senior.class, board);
        }
    }

    private Cell findWorker(Simulation sim, Class<? extends Worker> workerClass, GameBoard board) {
        for (Agent agent : sim.getAgents()) {
            if (workerClass.isInstance(agent)) {
                return board.getCell(agent.getX(), agent.getY());
            }
        }
        return board.findBossOfficeCell();
    }
}