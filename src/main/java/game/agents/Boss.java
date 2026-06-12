package game.agents;

import game.core.Simulation;
import game.core.GameConfiguration;
import game.model.*;

public class Boss extends Agent {

    private int controlRange;
    private int coffeeTimer;
    private double previousBudget;
    private int madTurnsRemaining = 0;

    public Boss(String name, int x, int y, double initialBudget) {
        super(x, y);
        this.setName(name);
        this.controlRange = GameConfiguration.BOSS_CONTROL_RANGE;
        this.coffeeTimer = 0;
        this.previousBudget = initialBudget;
    }

    public void triggerMadAnimation() {
        this.madTurnsRemaining = 2;
        System.out.println("[ANIMACJA] Szef " + this.getName() + " czerwienieje ze złości w gabinecie! (Szał na 2 tury)");
    }

    @Override
    public void act(GameBoard board, Simulation sim) {
        // === POPRAWIONY BLOK SZALU (SZEF TERAZ BIEGA WŚCIEKŁY) ===
        if (this.madTurnsRemaining > 0) {
            this.madTurnsRemaining--;
            System.out.println(" 😡 [SZAŁ] Szef " + this.getName() + " w amoku szuka winnych Fatal Errora! Pozostało tur: " + this.madTurnsRemaining);

            // W stanie szału szef ZAWSZE obiera za cel Juniorów (bo to oni psują)
            Cell juniorTarget = findWorker(sim, Junior.class, board);
            if (juniorTarget != null) {
                System.out.println("  -> [Amok] Szef namierzył Juniora i biegnie w jego stronę!");
                moveToTarget(juniorTarget, board);
            } else {
                moveRandomly(board, true);
            }

            // Po ruchu sprawdza, czy kogoś dopadł i może zwolnić
            interactWithEmployees(board, sim);
            this.previousBudget = sim.getBudget();
            return; // Wychodzimy, bo pomijamy standardową logikę (kawę, relaks itp.)
        }

        this.coffeeTimer++;
        Cell currentCell = board.getCell(getX(), getY());

        // FIX: Eliminacja transu kawowego Szefa
        if (currentCell != null && currentCell.getType().equalsIgnoreCase("coffee")) {
            if (this.coffeeTimer >= GameConfiguration.BOSS_COFFEE_INTERVAL) {
                this.coffeeTimer = 0;
                sim.recordCoffeeDrunk();
                System.out.println("Szef " + this.getName() + " wypił kawę na miejscu. Timer zresetowany!");
                moveRandomly(board, true);
            } else {
                // Jeśli wszedł na kawę z przypadku podczas patrolu LUB wypił i nie zdążył zejść, ucieka stąd!
                moveRandomly(board, true);
            }
            return;
        }

        if (this.coffeeTimer >= GameConfiguration.BOSS_COFFEE_INTERVAL) {
            Cell coffeeTarget = chooseTarget(board, sim);
            if (coffeeTarget != null) {
                moveToTarget(coffeeTarget, board);
            } else {
                moveRandomly(board, true);
            }
        }
        else {
            double chanceToMove = GameConfiguration.BOSS_CHANCE_TO_MOVE_NORMAL;

            if (sim.getBudget() < previousBudget) {
                chanceToMove = GameConfiguration.BOSS_CHANCE_TO_MOVE_PANIC;
                System.out.println("Szef " + this.getName() + " zauważył spadek budżetu! Intensywny patrol (" + String.format("%.0f", chanceToMove * 100) + "% szans).");
            }

            if (Math.random() < chanceToMove) {
                Cell targetWorkerCell = chooseTarget(board, sim);

                if (targetWorkerCell != null) {
                    moveToTarget(targetWorkerCell, board);
                } else {
                    moveRandomly(board, true);
                }
            } else {
                System.out.println("Szef " + this.getName() + " relaksuje się w gabinecie...");
            }
        }

        interactWithEmployees(board, sim);
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
            if (nextX != getX() && board.moveAgent(getX(), getY(), nextX, getY())) {
                setX(nextX);
            } else if (nextY != getY() && board.moveAgent(getX(), getY(), getX(), nextY)) {
                setY(nextY);
            } else {
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

                    // Jeśli to Junior, odpoczywa (RestingState) i kafelek to 'outside' -> ŁAPIEMY GO!
                    if (worker instanceof Junior && "RestingState".equalsIgnoreCase(state) && "outside".equalsIgnoreCase(neighborCell.getType())) {
                        System.out.println("!!! SZEF " + this.getName() + " PRZYŁAPAŁ JUNIORA " + worker.getName() + " NA PALENIU NA ZEWNĄTRZ !!!");
                        worker.markFired();
                        continue; // Przechodzimy do kolejnego sąsiada
                    }

                    // Standardowa blokada: Szef ignoruje inne formy odpoczynku (np. legalną kawę w kuchni)
                    if ("MovingToRestState".equalsIgnoreCase(state) ||
                            "RestingState".equalsIgnoreCase(state) ||
                            "coffee".equalsIgnoreCase(neighborCell.getType())) {
                        continue;
                    }

                    // Standardowe zwalnianie za złe wyniki
                    if (worker.shouldBeFired() || worker.hasTerribleMetrics()) {
                        if (worker.shouldBeFired()) {
                            System.out.println("!!! SZEF " + this.getName() + " PRZYŁAPAŁ PRACOWNIKA " + worker.getName() + " NA GORĄCYM UCZYNKU !!!");
                        } else {
                            System.out.println("!!! SZEF " + this.getName() + " ZWALNIA PRACOWNIKA " + worker.getName() + " ZA ZŁE WYNIKI (Wydajność: "
                                    + String.format("%.2f", worker.getEfficiency() * 100) + "%) !!!");
                            worker.markFired();
                        }
                    }
                    else {
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
        return null;
    }
}