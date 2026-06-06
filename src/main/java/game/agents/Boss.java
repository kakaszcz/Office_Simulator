package game.agents;

import game.core.Simulation;
import game.core.GameConfiguration;
import game.model.*;

public class Boss extends Agent {

    private int controlRange;
    private int coffeeTimer;
    private double previousBudget;

    public Boss(String name, int x, int y, double initialBudget) {
        super(x, y);
        this.setName(name);
        this.controlRange = GameConfiguration.BOSS_CONTROL_RANGE;
        this.coffeeTimer = 0;
        this.previousBudget = initialBudget;
    }

    @Override
    public void act(GameBoard board, Simulation sim) {
        this.coffeeTimer++;

        // Pobieramy aktualny kafelek, na którym stoi szef
        Cell currentCell = board.getCell(getX(), getY());

        // Czy już stoimy przy ekspresie i pijemy?
        if (currentCell != null && currentCell.getType().equalsIgnoreCase("coffee")) {
            if (this.coffeeTimer >= GameConfiguration.BOSS_COFFEE_INTERVAL) {
                this.coffeeTimer = 0;
                sim.recordCoffeeDrunk(); // doliczanie szefa do statystyk
                System.out.println("Szef " + this.getName() + " wypił kawę na miejscu. Timer zresetowany!");
            }
            moveRandomly(board, true); // Schodzi z ekspresu, żeby zrobić miejsce innym
        }
        // Szef ma parcie na kawę, idzie tam bezwarunkowo co turę
        else if (this.coffeeTimer >= GameConfiguration.BOSS_COFFEE_INTERVAL) {
            Cell coffeeTarget = chooseTarget(board, sim); // Zwróci wolny ekspres do kawy
            if (coffeeTarget != null) {
                moveToTarget(coffeeTarget, board);
            } else {
                moveRandomly(board, true); // Jeśli brak wolnych ekspresów, kręci się w korytarzu
            }
        }
        // INTELIGENTNY PATROL (Zależny od budżetu firmy)
        else {
            double chanceToMove = GameConfiguration.BOSS_CHANCE_TO_MOVE_NORMAL;

            // Jeśli budżet spadł -> Szef dostaje szału i patroluje bardzo intensywnie
            if (sim.getBudget() < previousBudget) {
                chanceToMove = GameConfiguration.BOSS_CHANCE_TO_MOVE_PANIC;
                System.out.println("Szef " + this.getName() + " zauważył spadek budżetu! Intensywny patrol (" + String.format("%.0f", chanceToMove * 100) + "% szans).");
            }

            if (Math.random() < chanceToMove) {
                // Szef analizuje sytuację finansową i wybiera ofiarę (Juniora lub Seniora)
                Cell targetWorkerCell = chooseTarget(board, sim);

                if (targetWorkerCell != null) {
                    // Szef robi jeden świadomy krok w kierunku wybranego pracownika!
                    moveToTarget(targetWorkerCell, board);
                } else {
                    moveRandomly(board, true); // Jeśli nie ma pracowników, spaceruje losowo
                }
            } else {
                System.out.println("Szef " + this.getName() + " relaksuje się w gabinecie...");
            }
        }

        // SPRAWDZENIE SĄSIADÓW (Zwalnianie)
        interactWithEmployees(board, sim);

        // Zapisanie budżetu na kolejną turę
        this.previousBudget = sim.getBudget();
    }

    private void moveToTarget(Cell target, GameBoard board) {
        if (getX() == target.getX() && getY() == target.getY()) return;

        int nextX = getX() + Integer.compare(target.getX(), getX());
        int nextY = getY() + Integer.compare(target.getY(), getY());

        if (board.moveAgent(getX(), getY(), nextX, nextY)) {
            setX(nextX);
            setY(nextY);
        } else {
            // Jeśli skos jest zablokowany, spróbuj pójść tylko w bok lub tylko w dół/górę
            if (nextX != getX() && board.moveAgent(getX(), getY(), nextX, getY())) {
                setX(nextX);
            } else if (nextY != getY() && board.moveAgent(getX(), getY(), getX(), nextY)) {
                setY(nextY);
            } else {
                // Ostateczność: Szef jest zablokowany, więc robi losowy unik
                moveRandomly(board, true);
            }
        }
    }

    private void interactWithEmployees(GameBoard board, Simulation sim) {
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
                            "RestingState".equalsIgnoreCase(state) ||
                            neighborCell.getType().equalsIgnoreCase("coffee")) {
                        continue;
                    }

                    // konsekwencje podejścia szefa
                    if (worker.shouldBeFired() || worker.hasTerribleMetrics()) {
                        if (worker.shouldBeFired()) {
                            System.out.println("!!! SZEF " + this.getName() + " PRZYŁAPAŁ PRACOWNIKA " + worker.getName() + " NA GORĄCYM UCZYNKU !!!");
                        } else {
                            // REFAKTOR WIZUALNY: Czytelniejszy log w procentach
                            System.out.println("!!! SZEF " + this.getName() + " ZWALNIA PRACOWNIKA " + worker.getName() + " ZA ZŁE WYNIKI (Wydajność: "
                                    + String.format("%.2f", worker.getPerformance() * 100) + "%) !!!");
                            worker.markFired();
                        }
                    }
                    else {
                        // Szef odzywa się tylko do Seniorów i tylko, jeśli już nie gadają
                        if (worker instanceof game.agents.Senior && !state.equalsIgnoreCase("TalkingState")) {
                            System.out.println("Szef " + this.getName() + " ucina sobie przyjacielską pogawędkę z " + worker.getName() + ".");
                            worker.changeState(new game.states.TalkingState());
                        }
                    }
                }
            }
        }
    }

    private Cell chooseTarget(GameBoard board, Simulation sim) {
        if (this.coffeeTimer >= GameConfiguration.BOSS_COFFEE_INTERVAL) {
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